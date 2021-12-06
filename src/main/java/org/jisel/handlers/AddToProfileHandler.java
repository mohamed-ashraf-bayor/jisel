package org.jisel.handlers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class AddToProfileHandler implements JiselAnnotationHandler {

    @Override
    public Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                             Set<Element> allAnnotatedElements,
                                                             Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                             Map<Element, Map<String, List<String>>> sealedInterfacesPermits) {
        // handle child interfaces as well not just classes
        return Map.of();
    }

    private void processAnnotatedChildrenClassesOrInterfaces(final Set<? extends Element> allAnnotatedElements) {
        var annotatedClasses = allAnnotatedElements.stream()
                .filter(element -> !element.getClass().isEnum())
                .filter(element -> ElementKind.CLASS.equals(element.getKind()) || ElementKind.INTERFACE.equals(element.getKind()) || ElementKind.RECORD.equals(element.getKind()))
                //.map(element -> processingEnv.getTypeUtils().asElement(element.asType()))
                .collect(Collectors.toSet());
        //            List<? extends Element> annotatedClasses = annotatedElements.stream()
//                    .filter(element -> ElementKind.INTERFACE.equals(element.getKind()) || ElementKind.INTERFACE.equals(element.getKind()) || ElementKind.RECORD.equals(element.getKind()))
//                    //TODO here accept both classes and interfaces to use @AddToProfile
//                    .toList();
        annotatedClasses.forEach(annotatedClass -> processAnnotatedElement(annotatedClass, allAnnotatedElements));
    }

    private void processAnnotatedElement(final Element annotatedElement, final Set<? extends Element> allAnnotatedElements) {
//        processingEnv.getElementUtils().getAllAnnotationMirrors(annotatedElement).forEach(annotationMirror -> {
//            annotationMirror.getElementValues().entrySet().forEach(k -> System.out.println("#####k: " + k.getKey() + ", #####v: " + k.getValue().toString()));
//        });
//        var gettersList = processingEnv.getElementUtils().getAllMembers((TypeElement) processingEnv.getTypeUtils().asElement(annotatedElement.asType())).stream()
//                .filter(element -> element.getSimpleName().toString().startsWith("get") || element.getSimpleName().toString().startsWith("is"))
//                .filter(element -> !element.getSimpleName().toString().startsWith("getClass"))
//                .toList();
//        var qualifiedClassName = ((TypeElement) processingEnv.getTypeUtils().asElement(annotatedElement.asType())).getQualifiedName().toString();
//        var gettersMap = gettersList.stream().collect(Collectors.toMap(getter -> getter.getSimpleName().toString(), getter -> ((ExecutableType) getter.asType()).getReturnType().toString()));
//        try {
//            new RecordSourceFileGenerator(processingEnv, allAnnotatedElements).writeRecordSourceFile(qualifiedClassName, gettersList, gettersMap);
//            log.info(() -> "\t> Successfully generated " + qualifiedClassName + "Record");
//        } catch (FilerException e) {
//            // Skipped generating " + qualifiedClassName + "Record - file already exists"
//        } catch (IOException e) {
//            log.log(Level.SEVERE, format("Error generating %sRecord", qualifiedClassName), e);
//        }
    }
}