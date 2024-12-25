/*
 * MIT License
 *
 * Copyright (c) 2023 mine_diver
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.mine_diver.unsafeevents.listener;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.mine_diver.unsafeevents.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

/**
 * High performance listener executor factory.
 *
 * <p>
 *     Used for avoiding executing listeners using slow reflection.
 * </p>
 *
 * @author mine_diver
 */
@UtilityClass
final class ListenerExecutorFactory {
    private final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Generates and defines a high performance executor.
     *
     * @param method the method to generate the executor for.
     * @param eventType the event type class that the listener is listening to.
     * @return the high performance listener executor class.
     * @param <EVENT> the event type.
     */
    private <EVENT extends Event> @NotNull Class<? extends Consumer<@NotNull EVENT>> generateExecutor(
            final @NotNull Method method,
            final @NotNull Class<EVENT> eventType
    ) {
        final @NotNull MethodHandles.Lookup lookup;
        val declaringClass = method.getDeclaringClass();
        if (Modifier.isPublic(method.getModifiers()))
            // we can use our own lookup
            // no need to invade the class's privacy
            lookup = LOOKUP;
        else {
            try {
                // need to make sure the class got
                // the opportunity to give up its
                // privileged lookup
                LOOKUP.ensureInitialized(declaringClass);
            } catch (IllegalAccessException ignored) {
                // the lookup could still have been
                // registered through other means
            }
            lookup = Listener.LOOKUPS.get(declaringClass);
        }
        if (lookup == null) throw new IllegalStateException("""
                Non-public method "%s" of class "%s" was attempted to be registered \
                as a listener of the event "%s", but there's no privileged lookup \
                registered for this method's class!\
                """
                .formatted(
                        method.getName(),
                        declaringClass.getName(),
                        eventType.getName()
                )
        );
        try {
            //noinspection unchecked
            return (Class<? extends Consumer<@NotNull EVENT>>) lookup
                    .defineHiddenClass(
                            generateExecutorClass(
                                    method,
                                    lookup.lookupClass().getName().replace('.', '/')
                                            + "$$UnsafeEvents$ListenerExecutor",
                                    eventType
                            ),
                            true,
                            MethodHandles.Lookup.ClassOption.NESTMATE
                    )
                    .lookupClass()
                    .asSubclass(Consumer.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates the executor class's bytecode.
     *
     * @param m the method to generate the executor for.
     * @param name the executor class name.
     * @param eventType the event type class that the listener is listening to.
     * @return the byte array containing the class's bytecode.
     */
    private byte @NotNull [] generateExecutorClass(
            final @NotNull Method m,
            final @NotNull String name,
            final @NotNull Class<? extends Event> eventType
    ) {
        val staticMethod = Modifier.isStatic(m.getModifiers());
        val writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", new String[] {Type.getInternalName(Consumer.class)});
        if (!staticMethod)
            writer.visitField(ACC_PUBLIC, "_", "Ljava/lang/Object;", null, null).visitEnd();
        // Generate constructor
        var methodGenerator = writer.visitMethod(ACC_PUBLIC, "<init>", staticMethod ? "()V" : "(Ljava/lang/Object;)V", null, null);
        methodGenerator.visitCode();
        methodGenerator.visitVarInsn(ALOAD, 0);
        methodGenerator.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // Invoke the super class (Object) constructor
        if (!staticMethod) {
            methodGenerator.visitVarInsn(ALOAD, 0);
            methodGenerator.visitVarInsn(ALOAD, 1);
            methodGenerator.visitFieldInsn(PUTFIELD, name, "_", "Ljava/lang/Object;");
        }
        methodGenerator.visitInsn(RETURN);
        methodGenerator.visitMaxs(-1, -1);
        methodGenerator.visitEnd();
        // Generate the execute method
        methodGenerator = writer.visitMethod(ACC_PUBLIC, "accept", "(Ljava/lang/Object;)V", null, null);
        methodGenerator.visitCode();
        if (!staticMethod) {
            methodGenerator.visitVarInsn(ALOAD, 0);
            methodGenerator.visitFieldInsn(GETFIELD, name, "_", "Ljava/lang/Object;");
            methodGenerator.visitTypeInsn(CHECKCAST, Type.getInternalName(m.getDeclaringClass()));
        }
        methodGenerator.visitVarInsn(ALOAD, 1);
        methodGenerator.visitTypeInsn(CHECKCAST, Type.getInternalName(eventType));
        methodGenerator.visitMethodInsn(staticMethod ? INVOKESTATIC : INVOKEVIRTUAL, Type.getInternalName(m.getDeclaringClass()), m.getName(), Type.getMethodDescriptor(m), m.getDeclaringClass().isInterface());
        if (m.getReturnType() != void.class)
            methodGenerator.visitInsn(POP);
        methodGenerator.visitInsn(RETURN);
        methodGenerator.visitMaxs(-1, -1);
        methodGenerator.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    /**
     * The executor cache. Helps to avoid creating too many unnecessary objects.
     */
    private final @NotNull ConcurrentMap<@NotNull Method, @NotNull Class<? extends Consumer<? extends @NotNull Event>>> cache = new ConcurrentHashMap<>();

    /**
     * Creates a high performance listener executor.
     *
     * @param target the listener's instance. If null, a static executor is generated.
     * @param method the method to generate the executor for.
     * @param eventType the event type class that the listener is listening to.
     * @return the high performance executor.
     * @param <EVENT> the event type.
     */
    <EVENT extends Event> @NotNull Consumer<@NotNull EVENT> create(
            final @Nullable Object target,
            final @NotNull Method method,
            final @NotNull Class<EVENT> eventType
    ) {
        //noinspection unchecked
        final @NotNull Class<? extends Consumer<@NotNull EVENT>> executorClass = (Class<? extends Consumer<@NotNull EVENT>>) cache.computeIfAbsent(method, method1 -> generateExecutor(method1, eventType));
        try {
            return Modifier.isStatic(method.getModifiers()) ? executorClass.getConstructor().newInstance() : executorClass.getConstructor(Object.class).newInstance(target);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Unable to initialize " + executorClass, e);
        }
    }
}