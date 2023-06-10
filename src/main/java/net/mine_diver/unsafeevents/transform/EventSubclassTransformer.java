package net.mine_diver.unsafeevents.transform;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.mine_diver.unsafeevents.Event;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author mine_diver
 */
@UtilityClass
public class EventSubclassTransformer {
    private final int FIELD_EVENTID_ACCESS = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
    private final String FIELD_EVENTID_NAME = "0$UNSAFEEVENTS$EVENT_ID";
    private final String FIELD_EVENTID_DESC = Type.INT_TYPE.getDescriptor();

    private final String METHOD_NEXTID_NAME = "nextID";
    private final String METHOD_NEXTID_DESC = Type.getMethodDescriptor(Type.INT_TYPE);

    private final int METHOD_CLINIT_ACCESS = Modifier.STATIC;
    private final String METHOD_CLINIT_NAME = "<clinit>";
    private final String METHOD_CLINIT_DESC = Type.getMethodDescriptor(Type.VOID_TYPE);

    private final int METHOD_GETEVENTID_ACCESS = Modifier.PROTECTED;
    private final String METHOD_GETEVENTID_NAME = "getEventID";
    private final String METHOD_GETEVENTID_DESC = Type.getMethodDescriptor(Type.INT_TYPE);

    public boolean transform(final @NotNull ClassLoader classLoader, final @NotNull ClassNode eventNode) {
        final Class<?> superClass;
        try {
            superClass = classLoader.loadClass(eventNode.superName.replace('/', '.'));
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (Event.class.isAssignableFrom(superClass) && !Modifier.isAbstract(eventNode.access)) {
            val noGetEventId = eventNode.methods
                    .stream()
                    .noneMatch(methodNode ->
                            (Modifier.isProtected(methodNode.access) || Modifier.isPublic(methodNode.access)) &&
                                    methodNode.desc.equals(METHOD_GETEVENTID_DESC) &&
                                    methodNode.name.equals(METHOD_GETEVENTID_NAME)
                    );
            if (noGetEventId) {
                addEventIdField(eventNode);
                addGetEventIdMethod(eventNode);
            }
            return noGetEventId;
        }
        return false;
    }

    private void addEventIdField(final ClassNode eventNode) {
        val field = new FieldNode(FIELD_EVENTID_ACCESS, FIELD_EVENTID_NAME, FIELD_EVENTID_DESC, null, null);
        eventNode.fields.add(field);
        val fieldInit = new InsnList();
        fieldInit.add(new MethodInsnNode(INVOKESTATIC, eventNode.name, METHOD_NEXTID_NAME, METHOD_NEXTID_DESC));
        fieldInit.add(new FieldInsnNode(PUTSTATIC, eventNode.name, FIELD_EVENTID_NAME, FIELD_EVENTID_DESC));
        eventNode.methods
                .stream()
                .filter(methodNode ->
                        methodNode.access == METHOD_CLINIT_ACCESS &&
                                methodNode.name.equals(METHOD_CLINIT_NAME) &&
                                methodNode.desc.equals(METHOD_CLINIT_DESC)
                )
                .findAny()
                .orElseGet(() -> {
                    val clinit = new MethodNode(METHOD_CLINIT_ACCESS, METHOD_CLINIT_NAME, METHOD_CLINIT_DESC, null, null);
                    eventNode.methods.add(clinit);
                    return clinit;
                })
                .instructions.insert(fieldInit);
    }

    private void addGetEventIdMethod(final ClassNode eventNode) {
        val method = new MethodNode(METHOD_GETEVENTID_ACCESS, METHOD_GETEVENTID_NAME, METHOD_GETEVENTID_DESC, null, null);
        method.instructions.add(new FieldInsnNode(GETSTATIC, eventNode.name, FIELD_EVENTID_NAME, FIELD_EVENTID_DESC));
        method.instructions.add(new InsnNode(IRETURN));
        eventNode.methods.add(method);
    }
}
