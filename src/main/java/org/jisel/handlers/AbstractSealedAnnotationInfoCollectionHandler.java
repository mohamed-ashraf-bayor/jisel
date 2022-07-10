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

import org.jisel.annotations.AddTo;
import org.jisel.annotations.SealFor;
import org.jisel.handlers.impl.SealForAnnotationInfoCollectionHandler;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.jisel.generators.StringGenerator.ANNOTATION_STRING_VALUE_REGEX;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;

/**
 * Exposes contract to fulfill by a class collecting necessary information from the annotated elements,
 * in order to populate the {@link Map} containing the sealed interfaces information to be generated
 */
public abstract sealed class AbstractSealedAnnotationInfoCollectionHandler implements JiselAnnotationHandler permits SealForAnnotationInfoCollectionHandler {

    /**
     * Populates the Map containing the sealed interfaces information to be generated information regarding the used annotations
     *
     * @param allAnnotatedElements                       {@link Set} of {@link Element} instances representing all classes
     *                                                   annotated with &#64;{@link AddTo} and all abstract methods annotated
     *                                                   with &#64;{@link SealFor}
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the {@link Element} instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a {@link Map} of profile name as the key and a Set of {@link Element} instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     */
    public abstract void populateSealedInterfacesMap(Set<Element> allAnnotatedElements,
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
    protected void createParentInterfacesBasedOnCommonMethods(Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByLargeInterface,
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
            for (int i = 0; i < totalProfiles - 1; i++) { // TODO try eliminating i
                var allProcessedCommonMethodsByConcatenatedProfiles = concatenateProfilesBasedOnCommonMethods(profilesList.get(i), profilesList, methodsSetsList);
                // remove all commonMethodElmnts 1 by 1 and in each profile
                allProcessedCommonMethodsByConcatenatedProfiles.values().stream()
                        .flatMap(Collection::stream)
                        .collect(toSet())
                        .forEach(method -> {
                            methodsSetsList.forEach(methodsSets -> methodsSets.remove(method));
                            annotatedMethodsByProfileByLargeInterface.get(interfaceElement)
                                    .forEach((profileName, methodsSets) -> methodsSets.remove(method));
                        });
                sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).putAll(allProcessedCommonMethodsByConcatenatedProfiles);
            }
        });
    }

    private Map<String, Set<Element>> concatenateProfilesBasedOnCommonMethods(String processProfileName,
                                                                              List<String> profilesList,
                                                                              List<Set<Element>> methodsSetsList) {
        var allProcessedCommonMethodsByConcatenatedProfiles = new HashMap<String, Set<Element>>();
        var totalProfiles = profilesList.size();
        var processProfileIndex = profilesList.indexOf(processProfileName);
        for (var methodElement : methodsSetsList.get(processProfileIndex)) {
            var concatenatedProfiles = new StringBuilder(processProfileName);
            var found = false;
            for (int j = processProfileIndex + 1; j < totalProfiles; j++) { // TODO try eliminating j and found
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

    /**
     * For a specified large interface abstract method annotated with #64;{@link SealFor}, constructs a Set storing
     * all the provided profiles names
     *
     * @param annotatedMethod {@link Element} instance representing the annotated method of the large interface
     * @return a Set storing all the provided profiles names
     */
    protected Set<String> buildSealForProvidedProfilesSet(Element annotatedMethod) {
        var providedProfilesSet = new HashSet<String>();
        annotatedMethod.getAnnotationMirrors().stream()
                .flatMap(annotationMirror -> annotationMirror.getElementValues().entrySet().stream())
                .map(entry -> entry.getValue().toString())
                .forEach(annotationRawValueAsString -> {
                    // sample values for annotationRawValueAsString:
                    // single value: "profile1name"
                    // multiple: @org.jisel.annotations.SealFor("profile2name"), @org.jisel.annotations.SealFor("profile3name"),...
                    var matcher = Pattern.compile(ANNOTATION_STRING_VALUE_REGEX).matcher(annotationRawValueAsString);
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

    @Override
    public Map<Element, String> handleAnnotatedElements(Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        populateSealedInterfacesMap(allAnnotatedElements, sealedInterfacesToGenerateByLargeInterface);
        return Map.of();
    }
}