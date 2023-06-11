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

package net.mine_diver.unsafeevents.transform;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.util.Util;
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

    private final String CLASS_CANCELABLE_DESC = "Lnet/mine_diver/unsafeevents/event/Cancelable;";

    private final int METHOD_ISCANCELABLE_ACCESS = Modifier.PUBLIC;
    private final String METHOD_ISCANCELABLE_NAME = "isCancelable";
    private final String METHOD_ISCANCELABLE_DESC = Type.getMethodDescriptor(Type.BOOLEAN_TYPE);
    private final MethodNode METHOD_ISCANCELABLE = Util.make(
            new MethodNode(METHOD_ISCANCELABLE_ACCESS, METHOD_ISCANCELABLE_NAME, METHOD_ISCANCELABLE_DESC, null, null),
            methodNode -> {
                methodNode.instructions.add(new InsnNode(ICONST_1));
                methodNode.instructions.add(new InsnNode(IRETURN));
            }
    );

    public boolean handles(final @NotNull String name) {
        return !name.equals("net.mine_diver.unsafeevents.Event");
    }

    public boolean transform(final @NotNull ClassLoader classLoader, final @NotNull ClassNode eventNode) {
        final Class<?> superClass;
        try {
            superClass = classLoader.loadClass(eventNode.superName.replace('/', '.'));
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (Event.class.isAssignableFrom(superClass) && !Modifier.isAbstract(eventNode.access)) {
            var transformed = false;
            if (
                    eventNode.methods
                            .stream()
                            .noneMatch(methodNode ->
                                    (Modifier.isProtected(methodNode.access) || Modifier.isPublic(methodNode.access)) &&
                                            METHOD_GETEVENTID_NAME.equals(methodNode.name) &&
                                            METHOD_GETEVENTID_DESC.equals(methodNode.desc)
                            )
            ) {
                addEventIdField(eventNode);
                addGetEventIdMethod(eventNode);
                transformed = true;
            }
            if (
                    eventNode.methods
                            .stream()
                            .noneMatch(methodNode ->
                                    Modifier.isPublic(methodNode.access) &&
                                            METHOD_ISCANCELABLE_NAME.equals(methodNode.name) &&
                                            METHOD_ISCANCELABLE_DESC.equals(methodNode.desc)
                            ) &&
                            eventNode.visibleAnnotations != null &&
                            eventNode.visibleAnnotations
                                    .stream()
                                    .anyMatch(node -> CLASS_CANCELABLE_DESC.equals(node.desc))
            ) {
                addIsCancelable(eventNode);
                transformed = true;
            }
            return transformed;
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
                        METHOD_CLINIT_ACCESS == methodNode.access &&
                                METHOD_CLINIT_NAME.equals(methodNode.name) &&
                                METHOD_CLINIT_DESC.equals(methodNode.desc)
                )
                .findAny()
                .orElseGet(() -> {
                    val clinit = new MethodNode(METHOD_CLINIT_ACCESS, METHOD_CLINIT_NAME, METHOD_CLINIT_DESC, null, null);
                    clinit.instructions.add(new InsnNode(RETURN));
                    eventNode.methods.add(clinit);
                    return clinit;
                })
                .instructions.insert(fieldInit);
    }

    private void addGetEventIdMethod(final ClassNode eventNode) {
        val method = new MethodNode(METHOD_GETEVENTID_ACCESS, METHOD_GETEVENTID_NAME, METHOD_GETEVENTID_DESC, null, null);
        method.instructions.add(new FieldInsnNode(GETSTATIC, eventNode.name, FIELD_EVENTID_NAME, FIELD_EVENTID_DESC));
        method.instructions.add(new InsnNode(IRETURN));
        method.accept(eventNode);
    }

    private void addIsCancelable(final ClassNode eventNode) {
        METHOD_ISCANCELABLE.accept(eventNode);
    }
}
