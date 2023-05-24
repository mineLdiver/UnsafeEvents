package net.mine_diver.unsafeevents.ap;

import net.mine_diver.unsafeevents.listener.EventListener;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("net.mine_diver.unsafeevents.listener.EventListener")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EventListenerVerifier extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(EventListener.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                // The @EventListener annotation can only be applied to methods
                continue;
            }
            ExecutableElement method = (ExecutableElement) element;
            if (method.getParameters().size() != 1) {
                // The @EventListener method should take exactly one parameter
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@EventListener method should take exactly one parameter", method);
            }
            if (!method.getReturnType().toString().equals("void")) {
                // The @EventListener method should return void
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@EventListener method should return void",
                        method);
            }
        }
        return true;
    }
}
