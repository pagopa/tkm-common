package it.gov.pagopa.tkm.annotation.processor;

import it.gov.pagopa.tkm.annotation.LoggingTableResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class LoggingTableResultProcessorTest {
    @InjectMocks
    LoggingTableResultProcessor loggingTableResultProcessor;

    @Mock
    RoundEnvironment roundEnvironment;

    @Mock
    ExecutableElement element;

    @Mock
    Types type;

    @Mock
    TypeMirror typeMirror;

    @Mock
    ProcessingEnvironment processingEnvironment;

    @Mock
    ProcessingEnvironment processingEnv;

    @Mock
    Elements elements;

    @Mock
    Messager messager;

    @Mock
    LoggingTableResult loggingTableResult;

    @Mock
    TypeElement typeElement;

    @BeforeEach
    void init() {
        Set<? extends ExecutableElement> es = new HashSet<>(Collections.singletonList(element));
        Mockito.when(roundEnvironment.getElementsAnnotatedWith(LoggingTableResult.class)).thenReturn((Set) es);
        Mockito.when(element.getReturnType()).thenReturn(typeMirror);
        Mockito.when(element.getAnnotation(LoggingTableResult.class)).thenReturn(loggingTableResult);
        Mockito.when(processingEnv.getTypeUtils()).thenReturn(type);
        Mockito.when(processingEnv.getElementUtils()).thenReturn(elements);
        Mockito.when(elements.getTypeElement(Mockito.anyString())).thenReturn(typeElement);
        Mockito.when(type.isSubtype(Mockito.any(), Mockito.any())).thenReturn(true);
    }


    @Test
    void process_returnTypeError1() {
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(type.isSubtype(Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(loggingTableResult.strict()).thenReturn(true);
        boolean process = loggingTableResultProcessor.process(null, roundEnvironment);
        Assertions.assertFalse(process);
        Mockito.verify(messager).printMessage(Mockito.eq(Diagnostic.Kind.ERROR), Mockito.anyString(), Mockito.any());
    }

    @Test
    void process_returnTypeError2() {
        Mockito.when(loggingTableResult.strict()).thenReturn(true);
        Mockito.when(typeMirror.toString()).thenReturn("void");
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        boolean process = loggingTableResultProcessor.process(null, roundEnvironment);
        Assertions.assertFalse(process);
        Mockito.verify(messager).printMessage(Mockito.eq(Diagnostic.Kind.ERROR), Mockito.anyString(), Mockito.any());

    }
    @Test
    void process_returnTypeWarning() {
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(type.isSubtype(Mockito.any(), Mockito.any())).thenReturn(false);
        boolean process = loggingTableResultProcessor.process(null, roundEnvironment);
        Assertions.assertFalse(process);
        Mockito.verify(messager).printMessage(Mockito.eq(Diagnostic.Kind.WARNING), Mockito.anyString(), Mockito.any());
    }

}