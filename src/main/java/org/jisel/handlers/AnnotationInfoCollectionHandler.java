package org.jisel.handlers;

import org.jisel.annotations.AddTo;
import org.jisel.annotations.SealFor;
import org.jisel.handlers.impl.SealForInfoCollectionHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;

/**
 * Exposes contract to fulfill by any class dedicated to collecting necessary information from the annotated elements,
 * in order to populate the {@link Map} containing the sealed interfaces information to be generated
 */
public sealed interface AnnotationInfoCollectionHandler
        extends JiselAnnotationHandler
        permits SealForInfoCollectionHandler {

    /**
     * Populates the Map containing the sealed interfaces information to be generated
     *
     * @param processingEnv                              {@link ProcessingEnvironment} object, needed to access low-level
     *                                                   information regarding the used annotations
     * @param allAnnotatedElements                       {@link Set} of {@link Element} instances representing all classes
     *                                                   annotated with &#64;{@link AddTo} and all abstract methods annotated
     *                                                   with &#64;{@link SealFor}
     * @param sealedInterfacesToGenerateByLargeInterface Map containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the {@link Element} instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a {@link Map} of profile name as the key and a Set of {@link Element} instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     */
    void populateSealedInterfacesMap(ProcessingEnvironment processingEnv,
                                     Set<Element> allAnnotatedElements,
                                     Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface);

    /**
     * Creates intermediate parent interfaces based on common methods of provided profiles, then stores the created intermediate
     * parent interfaces in the Map containing the sealed interfaces information to be generated
     *
     * @param annotatedMethodsByProfileByLargeInterface  {@link Set} of all annotated abstract methods for a specified profile
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the {@link Element} instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     */
    default void createParentInterfacesBasedOnCommonMethods(Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByLargeInterface,
                                                            Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        annotatedMethodsByProfileByLargeInterface.forEach((interfaceElement, annotatedMethodsByProfile) -> {
            var profilesList = new ArrayList<String>();
            var methodsSetsList = new ArrayList<Set<Element>>();
            annotatedMethodsByProfile.forEach((profileName, methodsSet) -> {
                profilesList.add(profileName);
                methodsSetsList.add(methodsSet);
            });
            var totalProfiles = profilesList.size();
            sealedInterfacesToGenerateByLargeInterface.putIfAbsent(interfaceElement, new HashMap<>());
            sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).putAll(annotatedMethodsByProfileByLargeInterface.get(interfaceElement));
            for (int i = 0; i < totalProfiles - 1; i++) {
                var allProcessedCommonMethodsByConcatenatedProfiles = concatenateProfilesBasedOnCommonMethods(profilesList.get(i), profilesList, methodsSetsList);
                // remove all commonMethodElmnts 1 by 1 and in each profile
                allProcessedCommonMethodsByConcatenatedProfiles.values().stream().flatMap(Collection::stream).collect(toSet()).forEach(method -> {
                    methodsSetsList.forEach(methodsSets -> methodsSets.remove(method));
                    annotatedMethodsByProfileByLargeInterface.get(interfaceElement).forEach((profileName, methodsSets) -> methodsSets.remove(method));
                });
                sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).putAll(allProcessedCommonMethodsByConcatenatedProfiles);
            }
        });
    }

    private Map<String, Set<Element>> concatenateProfilesBasedOnCommonMethods(String processProfileName, List<String> profilesList, List<Set<Element>> methodsSetsList) {
        var allProcessedCommonMethodsByConcatenatedProfiles = new HashMap<String, Set<Element>>();
        var totalProfiles = profilesList.size();
        var processProfileIndex = profilesList.indexOf(processProfileName);
        for (var methodElement : methodsSetsList.get(processProfileIndex)) {
            var concatenatedProfiles = new StringBuilder(processProfileName);
            var found = false;
            for (int j = processProfileIndex + 1; j < totalProfiles; j++) {
                if (methodsSetsList.get(j).contains(methodElement)) {
                    concatenatedProfiles.append(COMMA_SEPARATOR).append(profilesList.get(j));
                    found = true;
                }
            }
            if (found) {
                allProcessedCommonMethodsByConcatenatedProfiles.merge(
                        concatenatedProfiles.toString(),
                        new HashSet<>(Set.of(methodElement)),
                        (currentSet, newSet) -> concat(currentSet.stream(), newSet.stream()).collect(toSet())
                );
            }
        }
        return allProcessedCommonMethodsByConcatenatedProfiles;
    }

    @Override
    default Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                         Set<Element> allAnnotatedElements,
                                                         Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                         Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                         Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface) {
        populateSealedInterfacesMap(processingEnv, allAnnotatedElements, sealedInterfacesToGenerateByLargeInterface);
        return Map.of();
    }
}
