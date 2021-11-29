package org.jisel;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

// @Slf4j
@SupportedAnnotationTypes("com.bayor.jisel.annotation.SealFor")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JiselAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        for (TypeElement annotation : annotations) {

            if (!annotation.getSimpleName().toString().contains("SealFor"))
                continue;

            Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotation);

            System.out.println(">>>>>>>> annotatedElements >>>>>>>>>>>" + annotatedElements);

            List<? extends Element> annotatedClasses = annotatedElements.stream()
                    //.filter(element -> element.getClass().getClassLoader().isRecord())
                    .filter(element -> ElementKind.INTERFACE.equals(element.getKind()))
                    .toList();

            System.out.println(">>>>>>>> annotatedClasses >>>>>>>>>>>" + annotatedClasses);

            List<? extends Element> annotatedMethods = annotatedElements.stream()
                    .filter(element -> !element.getClass().isRecord())
                    .filter(element -> ElementKind.METHOD.equals(element.getKind()))
                    .toList();

            System.out.println(">>>>>>>> annotateMethods >>>>>>>>>>>" + annotatedMethods);
        }

        return true;
    }

    private void writeSealedInterfaceFile(String className, List<Element> gettersList, Map<String, String> getterMap, Set<Element> allAnnotatedElements) throws IOException {
        String recordClassString = buildSealedInterfaceContent(className, gettersList, getterMap, allAnnotatedElements);
        JavaFileObject recordClassFile = processingEnv.getFiler().createSourceFile(className + "Record");
        try (PrintWriter out = new PrintWriter(recordClassFile.openWriter())) {
            out.println(recordClassString);
        }
    }

    private String buildSealedInterfaceContent(String className, List<Element> gettersList, Map<String, String> getterMap, Set<Element> allAnnotatedElements) {

        StringBuilder recordClassContent = new StringBuilder();

        String packageName = null;

        int lastDot = className.lastIndexOf('.');

        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String recordClassName = className + "Record";
        String recordSimpleClassName = recordClassName.substring(lastDot + 1);

        if (packageName != null) {
            recordClassContent.append("package ");
            recordClassContent.append(packageName);
            recordClassContent.append(";");
            recordClassContent.append("\n\n");
        }

        recordClassContent.append("public record ");
        recordClassContent.append(recordSimpleClassName);
        recordClassContent.append("(");

//        // System.out.println("################################" + recordSimpleClassName);
//        buildRecordAttributesFromGettersList(recordClassContent, getterMap, gettersList, allAnnotatedElements);

        recordClassContent.append(") {\n\n");

       // buildRecordCustom1ArgConstructor(recordClassContent, simpleClassName, gettersList, allAnnotatedElements);

        recordClassContent.append("}");

        return recordClassContent.toString();
    }
}