/**
 * Copyright (c) 2022 Mohamed Ashraf Bayor
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.jisel.handlers;

import org.jisel.generator.StringGenerator;

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
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.jisel.generator.StringGenerator.removeSeparator;

/**
 * Interface exposing contract to fulfill by any class handling the elements annotated with &#64;SealForProfile and &#64;AddToProfile annotations
 */
public sealed interface JiselAnnotationHandler extends StringGenerator permits SealForProfileHandler, AddToProfileHandler,
        AnnotationInfoCollectionHandler, UniqueParentInterfaceHandler, ParentChildInheritanceHandler {

    /**
     * Reads values of all attributes provided through the use of &#64;SealForProfile and &#64;AddToProfile annotations and
     * populates the provided Map arguments
     *
     * @param processingEnv                              needed to access low-level information regarding the used annotations
     * @param allAnnotatedElements                       Set of Element instances representing all classes annotated with &#64;AddToProfile and
     *                                                   all abstract methods annotated with &#64;SealForProfile
     * @param sealedInterfacesToGenerateByLargeInterface Map containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface    Map containing information about the subtypes permitted by each one of the sealed interfaces to begenerated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a List of profiles names as the value.
     * @return a status report as a string value for each one of the large interfaces to be segregated
     */
    Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                 Set<Element> allAnnotatedElements,
                                                 Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                 Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface);

    /**
     * For a specified class or interface annotated with &#64;AddToProfile, constructs a Map storing a Set of all the provided
     * profiles names (as the Map value) for each one of the large interfaces names (as the Map key) provided through &#64;AddToProfile.
     *
     * @param processingEnv             needed to access low-level information regarding the used annotations
     * @param annotatedClassOrInterface Element instance representing the annotated class or interface
     * @return a Map storing a Set of all the provided profiles names (as the Map value) for each one of the large interfaces names (as the Map key)
     */
    default Map<String, Set<String>> buildAddToProfileProvidedProfilesMap(final ProcessingEnvironment processingEnv, final Element annotatedClassOrInterface) {
        var providedProfilesMap = new HashMap<String, Set<String>>();
        var annotationRawValueAsString = processingEnv.getElementUtils().getAllAnnotationMirrors(annotatedClassOrInterface).stream()
                .map(Object::toString)
                .collect(joining(COMMA_SEPARATOR));
        // sample values for annotationRawValueAsString:
        // @org.jisel.AddToProfiles(profiles={"Student", "Worker"}, largeInterface="com.bayor.jisel.annotation.client.data.Sociable"),@org.jisel.AddToProfile(largeInterface="com.bayor.jisel.annotation.client.data.Sociable")
        // process addToProfile
        var addToProfileMatcher = Pattern.compile(ADD_TO_PROFILE_REGEX).matcher(annotationRawValueAsString);
        while (addToProfileMatcher.find()) {
            var attributesWithValues = addToProfileMatcher.group(1).strip(); // profile="ActiveWorker", largeInterface="com.bayor.jisel.annotation.client.data.Sociable"
            var profilesSet = new HashSet<String>();
            var profileAttributeMatcher = Pattern.compile(PROFILE_ATTRIBUTE_REGEX).matcher(attributesWithValues);
            if (profileAttributeMatcher.find()) { // get only the first occurence
                profilesSet.add(profileAttributeMatcher.group(1).strip());
            }
            updateProvidedProfilesMapBasedOnProfilesSet(providedProfilesMap, profilesSet, attributesWithValues);
        }
        // process addToProfiles
        var addToProfilesMatcher = Pattern.compile(ADD_TO_PROFILES_REGEX).matcher(annotationRawValueAsString);
        while (addToProfilesMatcher.find()) {
            var attributesWithValues = addToProfilesMatcher.group(1).strip();
            // sample values for annotationRawValueAsString:
            // profiles={"Student", "Worker"}, largeInterface="com.bayor.jisel.annotation.client.data.Sociable")
            var commaSeparatedProfiles = attributesWithValues.substring(attributesWithValues.indexOf(OPENING_BRACKET) + 1, attributesWithValues.indexOf(CLOSING_BRACKET));
            var profilesSet = new HashSet<String>();
            var profilesNamesMatcher = Pattern.compile(ANNOTATION_VALUES_REGEX).matcher(commaSeparatedProfiles);
            while (profilesNamesMatcher.find()) {
                profilesSet.add(profilesNamesMatcher.group(1).strip());
            }
            updateProvidedProfilesMapBasedOnProfilesSet(providedProfilesMap, profilesSet, attributesWithValues);
        }
        return providedProfilesMap;
    }

    private void updateProvidedProfilesMapBasedOnProfilesSet(final Map<String, Set<String>> providedProfilesMap, final Set<String> profilesSet, final String attributesWithValues) {
        var largeInterface = EMPTY_STRING;
        var largeInterfaceAttributeMatcher = Pattern.compile(LARGE_INTERFACE_ATTRIBUTE_REGEX).matcher(attributesWithValues);
        if (largeInterfaceAttributeMatcher.find()) { // get only the first occurence
            largeInterface = largeInterfaceAttributeMatcher.group(1).strip();
        }
        providedProfilesMap.merge(largeInterface, profilesSet.isEmpty() ? new HashSet<>(Set.of(EMPTY_STRING)) : profilesSet, (currentSet, newSet) -> concat(currentSet.stream(), newSet.stream()).collect(toSet()));
    }

    /**
     * For a specified large interface abstract method annotated with &#64;SealForProfile, constructs a Set storing all the provided profiles names
     *
     * @param processingEnv   needed to access low-level information regarding the used annotations
     * @param annotatedMethod Element instance representing the annotated method of the large interface
     * @return a Set storing all the provided profiles names
     */
    default Set<String> buildSealForProfileProvidedProfilesSet(final ProcessingEnvironment processingEnv, final Element annotatedMethod) {
        var providedProfilesSet = new HashSet<String>();
        processingEnv.getElementUtils().getAllAnnotationMirrors(annotatedMethod).stream()
                .flatMap(annotationMirror -> annotationMirror.getElementValues().entrySet().stream())
                .map(entry -> entry.getValue().toString())
                .forEach(annotationRawValueAsString -> {
                    // sample values for annotationRawValueAsString:
                    // single value: "profile1name"
                    // multiple: {@org.jisel.SealForProfile("profile2name"), @org.jisel.SealForProfile("profile3name"),...}
                    var matcher = Pattern.compile(ANNOTATION_VALUES_REGEX).matcher(annotationRawValueAsString);
                    while (matcher.find()) {
                        var profile = matcher.group(1).strip();
                        if (profile.isBlank()) { // blank profiles ignored
                            continue;
                        }
                        providedProfilesSet.add(profile);
                    }
                });
        return providedProfilesSet;
    }
}

/**
 * Interface exposing contract to fulfill by any class dedicated to collecting necessary information from the annotated elements,
 * in order to populate the Map containing the sealed interfaces information to be generated
 */
sealed interface AnnotationInfoCollectionHandler extends JiselAnnotationHandler permits SealForProfileInfoCollectionHandler {

    /**
     * Populates the Map containing the sealed interfaces information to be generated
     *
     * @param processingEnv                              needed to access low-level information regarding the used annotations
     * @param allAnnotatedElements                       Set of Element instances representing all classes annotated with &#64;AddToProfile and
     *                                                   all abstract methods annotated with &#64;SealForProfile
     * @param sealedInterfacesToGenerateByLargeInterface Map containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     */
    void populateSealedInterfacesMap(ProcessingEnvironment processingEnv,
                                     Set<Element> allAnnotatedElements,
                                     Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface);

    default void createParentInterfacesBasedOnCommonMethods(final Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByLargeInterface,
                                                            final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        annotatedMethodsByProfileByLargeInterface.forEach((interfaceElement, annotatedMethodsByProfile) -> {
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
                    annotatedMethodsByProfileByLargeInterface.get(interfaceElement).forEach((profileName, methodsSets) -> methodsSets.remove(method));
                });
                //
                if (!sealedInterfacesToGenerateByLargeInterface.containsKey(interfaceElement)) {
                    sealedInterfacesToGenerateByLargeInterface.put(interfaceElement, new HashMap<>());
                }
                sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).putAll(allProcessedCommonMethodsByConcatenatedProfiles);
                sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).putAll(annotatedMethodsByProfileByLargeInterface.get(interfaceElement));
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
    default Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                         final Set<Element> allAnnotatedElements,
                                                         final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                         final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        populateSealedInterfacesMap(processingEnv, allAnnotatedElements, sealedInterfacesToGenerateByLargeInterface);
        return Map.of(); // returning empty map instead of null
    }
}

sealed interface ParentChildInheritanceHandler extends JiselAnnotationHandler permits SealForProfileParentChildInheritanceHandler {

    void buildInheritanceRelations(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                   Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                   Map<Element, String> uniqueParentInterfaceStatusReport);

    default void buildSealedInterfacesPermitsMap(final Element interfaceElement,
                                                 final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                 final Map<Element, Map<String, List<String>>> sealedInterfacesPermits,
                                                 final Map<Element, String> uniqueParentInterfaceStatusReport) {
        sealedInterfacesPermits.get(interfaceElement).putAll(sealedInterfacesToGenerate.get(interfaceElement).keySet().stream()
                .filter(profiles -> profiles.contains(COMMA_SEPARATOR))
                .collect(toMap(profiles -> profiles, profiles -> asList(profiles.split(COMMA_SEPARATOR)))));
        // if uniq parnt intrf prsnt, parent interf permits all, if there are other permits already existing then the profiles in those permits lists should be removed from parent interf permits list
        if (Optional.ofNullable(uniqueParentInterfaceStatusReport).isPresent() && !uniqueParentInterfaceStatusReport.containsKey(interfaceElement)) {
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
    }

    @Override
    default Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                         final Set<Element> allAnnotatedElements,
                                                         final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                         final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        buildInheritanceRelations(sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface, null);
        return Map.of();
    }
}

sealed interface UniqueParentInterfaceHandler extends JiselAnnotationHandler permits SealForProfileUniqueParentInterfaceHandler {

    default Map<Element, Optional<String>> checkUniqueParentInterfacePresence(final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate) {

        var providedProfilesListByInterface = new HashMap<Element, List<String>>();
        sealedInterfacesToGenerate.forEach(
                (interfaceElement, annotatedMethodsByProfile) -> annotatedMethodsByProfile.keySet().stream()
                        .filter(concatenatedProfiles -> !concatenatedProfiles.contains(COMMA_SEPARATOR)).forEach(
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

    Map<Element, String> checkAndHandleUniqueParentInterface(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate);

    @Override
    default Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                         final Set<Element> allAnnotatedElements,
                                                         final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                         final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        return checkAndHandleUniqueParentInterface(sealedInterfacesToGenerateByLargeInterface);
    }
}