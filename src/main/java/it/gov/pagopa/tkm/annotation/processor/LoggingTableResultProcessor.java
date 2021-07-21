package it.gov.pagopa.tkm.annotation.processor;

import com.google.auto.service.AutoService;
import it.gov.pagopa.tkm.annotation.LoggingTableResult;
import it.gov.pagopa.tkm.model.BaseResultDetails;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("it.gov.pagopa.tkm.annotation.LoggingTableResult")
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LoggingTableResultProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(LoggingTableResult.class)) {
            if (invalidReturnType(element)) {
                return false;
            }
        }
        return true;
    }

    private boolean invalidReturnType(Element element) {
        TypeMirror returnType = ((ExecutableElement) element).getReturnType();
        LoggingTableResult annotation = element.getAnnotation(LoggingTableResult.class);
        if (isInvalidType(returnType)) {
            Diagnostic.Kind kind = Diagnostic.Kind.ERROR;
            String warningMessageExtend = "The element is set not strict. Skip writing if is invalid";
            String defaultMessage = String.format("The method %s required a return of type < ? extendsBaseResultDetails > but found %s.", element, returnType);

            if (!annotation.strict()) {
                kind = Diagnostic.Kind.WARNING;
                defaultMessage += warningMessageExtend;
            }
            processingEnv.getMessager().printMessage(kind, defaultMessage, element);
            return true;
        }
        return false;
    }

    private boolean isInvalidType(TypeMirror returnType) {
        TypeElement typeElementBaseResult = processingEnv.getElementUtils().getTypeElement(BaseResultDetails.class.getTypeName());
        return !processingEnv.getTypeUtils().isSubtype(returnType, typeElementBaseResult.asType())
                || StringUtils.equals(returnType.toString(), "void");
    }
}