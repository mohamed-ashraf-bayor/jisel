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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

/**
 * Handles all elements annotated with &#64;{@link AddTo}
 */
public final class AddToHandler implements JiselAnnotationHandler {

    @Override
    public Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                        Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var annotatedClassesAndInterfaces = allAnnotatedElements.stream()
                .filter(element -> !element.getClass().isEnum())
                .filter(element -> ElementKind.CLASS.equals(element.getKind())
                        || ElementKind.INTERFACE.equals(element.getKind())
                        || ElementKind.RECORD.equals(element.getKind()))
                .collect(toSet());
        var statusReport = new HashMap<Element, String>();
        annotatedClassesAndInterfaces.forEach(annotatedClassOrInterface -> statusReport.put(
                annotatedClassOrInterface,
                processAnnotatedElement(processingEnv, annotatedClassOrInterface, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface)
        ));
        return statusReport;
    }

    private String processAnnotatedElement(ProcessingEnvironment processingEnv,
                                           Element annotatedClassOrInterface,
                                           Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                           Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var statusReport = new StringBuilder();
        var addToProfileProvidedProfilesMap = buildAddToProvidedProfilesMap(processingEnv, annotatedClassOrInterface);
        addToProfileProvidedProfilesMap.putAll(buildAddToProfileProvidedProfilesMap(processingEnv, annotatedClassOrInterface)); // remove line when @AddToProfile(s) removed
        if (addToProfileProvidedProfilesMap.isEmpty()) {
            // do not process if no profiles are provided
            return statusReport.toString();
        }
        var profileFound = false;
        var providedLargeInterfaceTypeNotFound = false;
        for (var mapEntrySet : addToProfileProvidedProfilesMap.entrySet()) {
            var providedLargeInterfaceQualifiedName = mapEntrySet.getKey();
            var providedProfilesForProvidedLargeInterface = mapEntrySet.getValue();
            // 1st check if the provided superinterf type exists
            var providedLargeInterfaceTypeOpt = Optional.ofNullable(processingEnv.getElementUtils().getTypeElement(providedLargeInterfaceQualifiedName));
            if (providedLargeInterfaceTypeOpt.isPresent()) {
                var providedLargeInterfaceElement = processingEnv.getTypeUtils().asElement(processingEnv.getElementUtils().getTypeElement(providedLargeInterfaceQualifiedName).asType());
                var annotatedMethodsByProfile = sealedInterfacesToGenerateByLargeInterface.get(providedLargeInterfaceElement);
                profileFound = Optional.ofNullable(annotatedMethodsByProfile).isPresent() && updateSealedInterfacesPermitsMapWithProvidedProfiles(
                        annotatedMethodsByProfile.keySet(),
                        providedLargeInterfaceElement,
                        annotatedClassOrInterface,
                        providedProfilesForProvidedLargeInterface,
                        sealedInterfacesPermitsByLargeInterface
                );
            } else {
                providedLargeInterfaceTypeNotFound = true;
            }
        }
        if (!profileFound || providedLargeInterfaceTypeNotFound) {
            statusReport.append(ADD_TO_REPORT_MSG);
        }
        return statusReport.toString();
    }

    private boolean updateSealedInterfacesPermitsMapWithProvidedProfiles(Set<String> largeInterfaceProfilesSet,
                                                                         Element providedLargeInterfaceElement,
                                                                         Element annotatedClassOrInterface,
                                                                         Set<String> providedProfilesForProvidedLargeInterface,
                                                                         Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var notFoundProfiles = new HashSet<String>();
        for (var providedProfile : providedProfilesForProvidedLargeInterface) {
            if (providedProfile.isBlank()) {
                // only for provided empty profiles, add to the largeInterface sealed profile permits list
                sealedInterfacesPermitsByLargeInterface.get(providedLargeInterfaceElement).merge(
                        providedLargeInterfaceElement.getSimpleName().toString(),
                        asList(annotatedClassOrInterface.toString()),
                        (currentList, newList) -> concat(currentList.stream(), newList.stream()).toList()
                );
                continue;
            }
            var foundProvidedProfile = false;
            for (var profile : largeInterfaceProfilesSet) {
                if (profile.equals(providedLargeInterfaceElement.getSimpleName().toString())) {
                    continue;
                }
                if (sealedInterfaceNameConvention(providedProfile, providedLargeInterfaceElement).equals(sealedInterfaceNameConvention(profile, providedLargeInterfaceElement))) {
                    sealedInterfacesPermitsByLargeInterface.get(providedLargeInterfaceElement).merge(
                            providedProfile,
                            asList(annotatedClassOrInterface.toString()),
                            (currentList, newList) -> concat(currentList.stream(), newList.stream()).toList()
                    );
                    foundProvidedProfile = true;
                }
            }
            if (!foundProvidedProfile) {
                notFoundProfiles.add(providedProfile);
            }
        }
        return notFoundProfiles.isEmpty();
    }
}