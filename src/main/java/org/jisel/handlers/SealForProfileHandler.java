/**
 * Copyright (c) 2021-2022 Mohamed Ashraf Bayor.
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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

public final class SealForProfileHandler implements JiselAnnotationHandler {

    private final AnnotationInfoCollectionHandler annotationInfoCollectionHandler;
    private final UniqueParentInterfaceHandler uniqueParentInterfaceHandler;
    private final ParentChildInheritanceHandler parentChildInheritanceHandler;

    public SealForProfileHandler() {
        this.annotationInfoCollectionHandler = new SealForProfileInfoCollectionHandler();
        this.uniqueParentInterfaceHandler = new SealForProfileUniqueParentInterfaceHandler();
        this.parentChildInheritanceHandler = new SealForProfileParentChildInheritanceHandler();
    }

    @Override
    public Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                        final Set<Element> allAnnotatedElements,
                                                        final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        annotationInfoCollectionHandler.populateSealedInterfacesMap(processingEnv, allAnnotatedElements, sealedInterfacesToGenerateByLargeInterface);
        var statusReport = uniqueParentInterfaceHandler.checkAndHandleUniqueParentInterface(sealedInterfacesToGenerateByLargeInterface);
        parentChildInheritanceHandler.buildInheritanceRelations(sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        return statusReport;
    }
}

final class SealForProfileInfoCollectionHandler implements AnnotationInfoCollectionHandler {

    @Override
    public void populateSealedInterfacesMap(final ProcessingEnvironment processingEnv,
                                            final Set<Element> allAnnotatedElements,
                                            final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate) {
        var annotatedMethodsByInterface = allAnnotatedElements.stream()
                .filter(element -> ElementKind.METHOD.equals(element.getKind()))
                .filter(element -> ElementKind.INTERFACE.equals(element.getEnclosingElement().getKind()))
                .collect(groupingBy(Element::getEnclosingElement, toSet()));
        if (annotatedMethodsByInterface.isEmpty()) {
            return;
        }
        // ...
        var annotatedMethodsByProfileByInterface = new HashMap<Element, Map<String, Set<Element>>>();
        annotatedMethodsByInterface.forEach(
                (interfaceElement, annotatedMethodsElements) -> annotatedMethodsElements.forEach(
                        annotatedMethod -> extractProfilesAndPopulateMaps(
                                interfaceElement,
                                buildSealForProfileProvidedProfilesSet(processingEnv, annotatedMethod),
                                annotatedMethod,
                                annotatedMethodsByProfileByInterface
                        )
                )
        );
        createParentInterfacesBasedOnCommonMethods(annotatedMethodsByProfileByInterface, sealedInterfacesToGenerate);
    }

    private void extractProfilesAndPopulateMaps(final Element interfaceElement,
                                                final Set<String> providedProfilesSet,
                                                final Element annotatedMethod,
                                                final Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByInterface) {
        providedProfilesSet.forEach(profile -> {
            if (annotatedMethodsByProfileByInterface.containsKey(interfaceElement)) {
                annotatedMethodsByProfileByInterface.get(interfaceElement).merge(
                        profile,
                        new HashSet<>(Set.of(annotatedMethod)),
                        (currentSet, newSet) -> concat(currentSet.stream(), newSet.stream()).collect(toSet())
                );
            } else {
                annotatedMethodsByProfileByInterface.put(interfaceElement, new HashMap<>(Map.of(profile, new HashSet<>(Set.of(annotatedMethod)))));
            }
        });
    }
}

final class SealForProfileParentChildInheritanceHandler implements ParentChildInheritanceHandler {

    @Override
    public void buildInheritanceRelations(final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                          final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        sealedInterfacesToGenerateByLargeInterface.keySet().forEach(interfaceElement -> {
            sealedInterfacesPermitsByLargeInterface.put(interfaceElement, new HashMap<>()); // start with initializing sealedInterfacesPermitsByLargeInterface with empty mutable maps
            // promote profiles with empty methods to parent level
            var allProfilesToRemove = new HashSet<String>();
            sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).keySet().forEach(concatenatedProfiles -> {
                var profilesArray = concatenatedProfiles.split(COMMA_SEPARATOR);
                if (profilesArray.length > 1) {
                    for (var profile : profilesArray) {
                        var profileMethodsOpt = Optional.ofNullable(sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).get(profile));
                        if (profileMethodsOpt.isPresent() && profileMethodsOpt.get().isEmpty()) {
                            sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).put(profile, sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).get(concatenatedProfiles));
                            sealedInterfacesPermitsByLargeInterface.get(interfaceElement).put(profile, Arrays.stream(profilesArray).filter(profileName -> !profile.equals(profileName)).toList());
                            allProfilesToRemove.add(concatenatedProfiles);
                            break;
                        }
                    }
                }
            });
            allProfilesToRemove.forEach(sealedInterfacesToGenerateByLargeInterface.get(interfaceElement)::remove);
            // and completing building sealedInterfacesPermitsByLargeInterface map
            buildSealedInterfacesPermitsMap(interfaceElement, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        });
    }
}

final class SealForProfileUniqueParentInterfaceHandler implements UniqueParentInterfaceHandler {

    @Override
    public Map<Element, String> checkAndHandleUniqueParentInterface(final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate) {
        var statusReport = new HashMap<Element, String>();
        Map<Element, Optional<String>> longestConcatenedProfilesStringOptByInterface = checkUniqueParentInterfacePresence(sealedInterfacesToGenerate);
        sealedInterfacesToGenerate.keySet().forEach(interfaceElement -> {
            var longestConcatenedProfilesStringOpt = longestConcatenedProfilesStringOptByInterface.get(interfaceElement);
            if (longestConcatenedProfilesStringOptByInterface.containsKey(interfaceElement) && longestConcatenedProfilesStringOpt.isPresent()) {
                sealedInterfacesToGenerate.get(interfaceElement).put(interfaceElement.getSimpleName().toString(), sealedInterfacesToGenerate.get(interfaceElement).get(longestConcatenedProfilesStringOpt.get()));
                sealedInterfacesToGenerate.get(interfaceElement).remove(longestConcatenedProfilesStringOpt.get());
            } else {
                statusReport.put(interfaceElement, SEAL_FOR_PROFILE_REPORT_MSG);
            }
        });
        return statusReport;
    }
}