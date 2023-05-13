package net.mine_diver.unsafeevents.listener;

import lombok.experimental.UtilityClass;
import net.mine_diver.unsafeevents.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static net.mine_diver.unsafeevents.util.UnsafeProvider.IMPL_LOOKUP;
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
    /**
     * Generates and defines a high performance executor.
     *
     * @param method the method to generate the executor for.
     * @param eventType the event type class that the listener is listening to.
     * @return the high performance listener executor class.
     * @param <T> the event type.
     */
    private <T extends Event> @NotNull Class<? extends Consumer<@NotNull T>> generateExecutor(
            final @NotNull Method method,
            final @NotNull Class<T> eventType
    ) {
        try {
            //noinspection unchecked
            return (Class<? extends Consumer<@NotNull T>>)
                    MethodHandles.privateLookupIn(method.getDeclaringClass(), IMPL_LOOKUP).defineHiddenClass(
                            generateExecutorClass(
                                    method,
                                    method.getDeclaringClass().getName().replace('.', '/') + "$$UnsafeEvents$ListenerExecutor",
                                    eventType
                            ),
                            true, MethodHandles.Lookup.ClassOption.NESTMATE
                    ).lookupClass().asSubclass(Consumer.class);
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
        final boolean staticMethod = Modifier.isStatic(m.getModifiers());
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", new String[] {Type.getInternalName(Consumer.class)});
        if (!staticMethod)
            writer.visitField(ACC_PUBLIC, "_", "Ljava/lang/Object;", null, null).visitEnd();
        // Generate constructor
        MethodVisitor methodGenerator = writer.visitMethod(ACC_PUBLIC, "<init>", staticMethod ? "()V" : "(Ljava/lang/Object;)V", null, null);
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
    @NotNull
    private final ConcurrentMap<Method, Class<? extends Consumer<? extends Event>>> cache = new ConcurrentHashMap<>();

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
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Unable to initialize " + executorClass, e);
        }
    }
}