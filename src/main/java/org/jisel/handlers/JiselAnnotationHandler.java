package org.jisel.handlers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

public sealed interface JiselAnnotationHandler permits SealForProfileHandler, AddToProfileHandler, AnnotationInfoCollectionHandler, UniqueParentInterfaceHandler, ParentChildInheritanceHandler {

    String SEPARATOR = ",";

    Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                 Set<Element> allAnnotatedElements,
                                                 Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                 Map<Element, Map<String, List<String>>> sealedInterfacesPermits);
}

sealed interface AnnotationInfoCollectionHandler extends JiselAnnotationHandler permits SealForProfileInfoCollectionHandler {

    void populateSealedInterfacesMap(ProcessingEnvironment processingEnv,
                                     Set<Element> allAnnotatedElements,
                                     Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate);

    default void createParentInterfacesBasedOnCommonMethods(final Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByInterface,
                                                            final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate) {
        annotatedMethodsByProfileByInterface.forEach((interfaceElement, annotatedMethodsByProfile) -> {
            var profilesList = new ArrayList<String>();
            var methodsSetsList = new ArrayList<Set<Element>>();
            annotatedMethodsByProfile.forEach((profileName, methodsSet) -> {
                profilesList.add(profileName);
                methodsSetsList.add(methodsSet);
            });
            var totalProfiles = profilesList.size();
            for (int i = 0; i < totalProfiles - 1; i++) {
                var allProcessedCommonMethodsByConcatenatedProfiles = concatenateProfilesBasedOnCommonMethods(profilesList.get(i), profilesList, methodsSetsList);
                // remove all commonMethodElmnts 1 by 1 and in each profile
                allProcessedCommonMethodsByConcatenatedProfiles.values().stream().flatMap(Collection::stream).collect(toSet()).forEach(method -> {
                    methodsSetsList.forEach(methodsSets -> methodsSets.remove(method));
                    annotatedMethodsByProfileByInterface.get(interfaceElement).forEach((profileName, methodsSets) -> methodsSets.remove(method));
                });
                //
                if (!sealedInterfacesToGenerate.containsKey(interfaceElement)) {
                    sealedInterfacesToGenerate.put(interfaceElement, new HashMap<>());
                }
                sealedInterfacesToGenerate.get(interfaceElement).putAll(allProcessedCommonMethodsByConcatenatedProfiles);
                sealedInterfacesToGenerate.get(interfaceElement).putAll(annotatedMethodsByProfileByInterface.get(interfaceElement));
            }
        });
    }

    private Map<String, Set<Element>> concatenateProfilesBasedOnCommonMethods(final String processProfileName, final List<String> profilesList, List<Set<Element>> methodsSetsList) {
        var allProcessedCommonMethodsByConcatenatedProfiles = new HashMap<String, Set<Element>>();
        var totalProfiles = profilesList.size();
        var processProfileIndex = profilesList.indexOf(processProfileName);
        for (var methodElement : methodsSetsList.get(processProfileIndex)) {
            var concatenatedProfiles = new StringBuilder(processProfileName);
            var found = false;
            for (int j = processProfileIndex + 1; j < totalProfiles; j++) {
                if (methodsSetsList.get(j).contains(methodElement)) {
                    concatenatedProfiles.append(SEPARATOR).append(profilesList.get(j));
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
    default Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                         final Set<Element> allAnnotatedElements,
                                                         final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                         final Map<Element, Map<String, List<String>>> sealedInterfacesPermits) {
        populateSealedInterfacesMap(processingEnv, allAnnotatedElements, sealedInterfacesToGenerate);
        return Map.of(); // return empty map instead of null
    }
}

sealed interface ParentChildInheritanceHandler extends JiselAnnotationHandler permits SealForProfileParentChildInheritanceHandler {

    void buildInheritanceRelations(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                   Map<Element, Map<String, List<String>>> sealedInterfacesPermits);

    default void buildSealedInterfacesPermitsMap(final Element interfaceElement,
                                                 final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                 final Map<Element, Map<String, List<String>>> sealedInterfacesPermits) {
        sealedInterfacesPermits.get(interfaceElement).putAll(sealedInterfacesToGenerate.get(interfaceElement).keySet().stream()
                .filter(profiles -> profiles.contains(SEPARATOR))
                .collect(toMap(profiles -> profiles, profiles -> asList(profiles.split(SEPARATOR)))));
        // parent interf permits all, if there are other permits already existing then the profiles in those permits lists should be removed from parent interf permits list
        var parentInterfaceSimpleName = interfaceElement.getSimpleName().toString();
        var allPermittedProfiles = sealedInterfacesPermits.get(interfaceElement).values().stream().flatMap(Collection::stream).collect(toSet());
        sealedInterfacesPermits.get(interfaceElement).put(
                parentInterfaceSimpleName,
                sealedInterfacesToGenerate.get(interfaceElement).keySet().stream()
                        .filter(profile -> !parentInterfaceSimpleName.equals(profile))
                        .filter(profile -> !allPermittedProfiles.contains(profile))
                        .toList()
        );
    }

    @Override
    default Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                         final Set<Element> allAnnotatedElements,
                                                         final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                         final Map<Element, Map<String, List<String>>> sealedInterfacesPermits) {
        buildInheritanceRelations(sealedInterfacesToGenerate, sealedInterfacesPermits);
        return Map.of();
    }
}

sealed interface UniqueParentInterfaceHandler extends JiselAnnotationHandler permits SealForProfileUniqueParentInterfaceHandler {

    String REPORT_MSG = "More than 1 Parent Sealed Interfaces will be generated. Check your profiles mapping";

    default Map<Element, Optional<String>> checkUniqueParentInterfacePresence(final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate) {

        var providedProfilesListByInterface = new HashMap<Element, List<String>>();
        sealedInterfacesToGenerate.forEach(
                (interfaceElement, annotatedMethodsByProfile) -> annotatedMethodsByProfile.keySet().stream()
                        .filter(concatenatedProfiles -> !concatenatedProfiles.contains(SEPARATOR)).forEach(
                                profileName -> providedProfilesListByInterface.merge(
                                        interfaceElement,
                                        asList(profileName),
                                        (currentList, newList) -> concat(currentList.stream(), newList.stream()).toList())
                        )
        );

        var uniqueParentInterfaceByInterface = new HashMap<Element, Optional<String>>();

        providedProfilesListByInterface.forEach((interfaceElement, profilesList) -> {
            // if only 1 key of sealedInterfacesToGenerate map contains all of the profiles names and its total length = all profiles names lenghts combined -> use fat interface name for that key and move on
            var totalProfilesNamesLengths = profilesList.stream().mapToInt(String::length).sum();
            var longestConcatenedProfilesStringOpt = sealedInterfacesToGenerate.get(interfaceElement).keySet().stream()
                    .sorted(Comparator.comparingInt(String::length).reversed()).findFirst();
            if (longestConcatenedProfilesStringOpt.isPresent()) {
                var foundUniqueParentInterface = false;
                for (var profileName : profilesList) {
                    foundUniqueParentInterface = removeSeparator(longestConcatenedProfilesStringOpt.get()).contains(profileName);
                }
                uniqueParentInterfaceByInterface.put(
                        interfaceElement,
                        foundUniqueParentInterface && removeSeparator(longestConcatenedProfilesStringOpt.get()).length() == totalProfilesNamesLengths
                                ? Optional.of(longestConcatenedProfilesStringOpt.get())
                                : Optional.empty()
                );
            }
        });

        return uniqueParentInterfaceByInterface;
    }

    private String removeSeparator(final String text) {
        return asList(text.split(SEPARATOR)).stream().collect(joining());
    }

    Map<Element, String> checkAndHandleUniqueParentInterface(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate);

    @Override
    default Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                         final Set<Element> allAnnotatedElements,
                                                         final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                         final Map<Element, Map<String, List<String>>> sealedInterfacesPermits) {
        return checkAndHandleUniqueParentInterface(sealedInterfacesToGenerate);
    }
}