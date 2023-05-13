package net.mine_diver.unsafeevents;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

/**
 * High performance listener registry factory.
 *
 * <p>
 *     Used for avoiding slowly iterating over listeners using for loop.
 * </p>
 *
 * @author mine_diver
 */
final class ListenerRegistryFactory {
    /**
     * The high performance registry class name.
     */
    private final @NotNull String className;

    /**
     * The private lookup of the {@link EventBus} owning this factory.
     *
     * <p>
     *     Used for defining dynamic hidden classes as event bus's nested classes.
     * </p>
     */
    private final @NotNull MethodHandles.Lookup eventBusLookup;

    ListenerRegistryFactory(final @NotNull EventBus eventBus, final @NotNull MethodHandles.Lookup eventBusLookup) {
        this.className = eventBus.getClass().getName().replace('.', '/') + "$$ListenerRegistry";
        this.eventBusLookup = eventBusLookup;
    }

    /**
     * Generates and defines a high performance executor.
     *
     * @param registrySize the registry size.
     * @return the high performance listener registry class.
     */
    private <EVENT extends Event> @NotNull Class<? extends Consumer<@NotNull EVENT>> generateExecutor(final int registrySize) {
        try {
            //noinspection unchecked
            return (Class<? extends Consumer<@NotNull EVENT>>)
                    eventBusLookup.defineHiddenClass(
                            generateExecutorClass(className, registrySize),
                            true, MethodHandles.Lookup.ClassOption.NESTMATE
                    ).lookupClass().asSubclass(Consumer.class);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates the registry class's bytecode.
     *
     * @param name the registry's class name.
     * @param registrySize the registry size.
     * @return the byte array containing the class's bytecode.
     */
    private static byte @NotNull [] generateExecutorClass(final @NotNull String name, final int registrySize) {
        val writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", new String[] { Type.getInternalName(Consumer.class) });
        // Generate fields
        for (int i = 0; i < registrySize; i++)
            writer.visitField(ACC_PRIVATE, String.valueOf(i), Type.getType(Consumer.class).getDescriptor(), null, null).visitEnd();
        // Generate constructor
        val corDesc = new StringBuilder();
        for (int i = 0; i < registrySize; i++)
            corDesc.append(Type.getType(Consumer.class).getDescriptor());
        MethodVisitor methodGenerator = writer.visitMethod(ACC_PUBLIC, "<init>", "(" + corDesc + ")V", null, null);
        methodGenerator.visitCode();
        methodGenerator.visitVarInsn(ALOAD, 0);
        methodGenerator.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        for (int i = 0; i < registrySize; i++) {
            methodGenerator.visitVarInsn(ALOAD, 0);
            methodGenerator.visitVarInsn(ALOAD, i + 1);
            methodGenerator.visitFieldInsn(PUTFIELD, name, String.valueOf(i), Type.getType(Consumer.class).getDescriptor());
        }
        methodGenerator.visitInsn(RETURN);
        methodGenerator.visitMaxs(-1, -1);
        methodGenerator.visitEnd();
        // Generate the execute method
        methodGenerator = writer.visitMethod(ACC_PUBLIC, "accept", "(Ljava/lang/Object;)V", null, null);
        methodGenerator.visitCode();
        for (int i = 0; i < registrySize; i++) {
            methodGenerator.visitVarInsn(ALOAD, 0);
            methodGenerator.visitFieldInsn(GETFIELD, name, String.valueOf(i), Type.getType(Consumer.class).getDescriptor());
            methodGenerator.visitVarInsn(ALOAD, 1);
            methodGenerator.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Consumer.class), "accept", "(Ljava/lang/Object;)V", true);
        }
        methodGenerator.visitInsn(RETURN);
        methodGenerator.visitMaxs(-1, -1);
        methodGenerator.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    /**
     * Creates a high performance listener registry.
     *
     * @param listeners the listeners to add to the registry.
     * @return the high performance registry.
     * @param <EVENT> the event type.
     */
    <EVENT extends Event> @NotNull Consumer<@NotNull EVENT> create(
            final @NotNull Consumer<@NotNull EVENT> @NotNull [] listeners
    ) {
        val executorClass = this.<EVENT>generateExecutor(listeners.length);
        try {
            return executorClass.getConstructor(Arrays.stream(listeners).map(listener -> Consumer.class).toArray(Class[]::new)).newInstance((Object[]) listeners);
        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Unable to initialize " + executorClass, e);
        }
    }
}
