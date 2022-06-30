package org.jisel.handlers.impl;

import org.jisel.handlers.AnnotationInfoCollectionHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

/**
 * Collects necessary information from the annotated elements, in order to populate the Map containing the sealed
 * interfaces information to be generated
 */
public final class SealForInfoCollectionHandler implements AnnotationInfoCollectionHandler {

    @Override
    public void populateSealedInterfacesMap(ProcessingEnvironment processingEnv,
                                            Set<Element> allAnnotatedElements,
                                            Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        var annotatedMethodsByInterface = allAnnotatedElements.stream()
                .collect(groupingBy(Element::getEnclosingElement, toSet()));
        var annotatedMethodsByProfileByInterface = new HashMap<Element, Map<String, Set<Element>>>();
        annotatedMethodsByInterface.forEach(
                (interfaceElement, annotatedMethodsElements) -> annotatedMethodsElements.forEach(
                        annotatedMethod -> extractProfilesAndPopulateMaps(
                                interfaceElement,
                                buildSealForProvidedProfilesSet(processingEnv, annotatedMethod),
                                annotatedMethod,
                                annotatedMethodsByProfileByInterface
                        )
                )
        );
        createParentInterfacesBasedOnCommonMethods(annotatedMethodsByProfileByInterface, sealedInterfacesToGenerateByLargeInterface);
    }

    private void extractProfilesAndPopulateMaps(Element interfaceElement,
                                                Set<String> providedProfilesSet,
                                                Element annotatedMethod,
                                                Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByInterface) {
        providedProfilesSet.forEach(profile -> {
            annotatedMethodsByProfileByInterface.putIfAbsent(interfaceElement, new HashMap<>(Map.of(profile, new HashSet<>(Set.of(annotatedMethod)))));
            annotatedMethodsByProfileByInterface.get(interfaceElement).merge(
                    profile,
                    new HashSet<>(Set.of(annotatedMethod)),
                    (currentSet, newSet) -> concat(currentSet.stream(), newSet.stream()).collect(toSet())
            );
        });
    }
}
