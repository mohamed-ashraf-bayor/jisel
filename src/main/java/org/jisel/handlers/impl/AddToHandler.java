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
package org.jisel.handlers.impl;

import org.jisel.annotations.AddTo;
import org.jisel.handlers.AbstractSealedAddToHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.jisel.generators.StringGenerator.ADD_TO_REPORT_PROFILES_NOT_FOUND_MSG;
import static org.jisel.generators.StringGenerator.sealedInterfaceNameConvention;

/**
 * Handles all elements annotated with &#64;{@link AddTo}
 */
public final class AddToHandler extends AbstractSealedAddToHandler {

    /**
     * Passes through the received {@link ProcessingEnvironment} instance to the super constructor
     *
     * @param processingEnvironment {@link ProcessingEnvironment} instance needed for performing low-level operations on {@link Element} instances
     */
    public AddToHandler(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public Map<Element, String> handleAnnotatedElements(Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var statusReport = new HashMap<Element, String>();
        var annotatedClassesAndInterfaces = allAnnotatedElements.stream()
                .filter(element -> !element.getClass().isEnum())
                .filter(element -> ElementKind.CLASS.equals(element.getKind())
                        || ElementKind.INTERFACE.equals(element.getKind())
                        || ElementKind.RECORD.equals(element.getKind()))
                .collect(toSet());
        annotatedClassesAndInterfaces.forEach(annotatedClassOrInterface ->
                statusReport.put(
                        annotatedClassOrInterface,
                        processAnnotatedElement(annotatedClassOrInterface, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface)
                ));
        return statusReport;
    }

    private String processAnnotatedElement(Element annotatedClassOrInterface,
                                           Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                           Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var statusReport = new StringBuilder();
        var addToProfileProvidedProfilesMap = buildAddToProvidedProfilesMap(annotatedClassOrInterface);
        if (addToProfileProvidedProfilesMap.isEmpty()) {
            // do not process if no profiles and no largeInterfaces are provided
            return statusReport.toString();
        }
        var profileFound = false;
        var providedLargeInterfaceTypeNotFound = false;
        for (var mapEntry : addToProfileProvidedProfilesMap.entrySet()) {
            var providedLargeInterfaceQualifiedName = mapEntry.getKey();
            var providedProfilesForProvidedLargeInterface = mapEntry.getValue();
            // 1st check if the provided superinterf type exists
            var providedLargeInterfaceTypeOpt = Optional.ofNullable(processingEnvironment.getElementUtils().getTypeElement(providedLargeInterfaceQualifiedName));
            if (providedLargeInterfaceTypeOpt.isPresent()) {
                var providedLargeInterfaceElement = processingEnvironment.getTypeUtils().asElement(
                        processingEnvironment.getElementUtils().getTypeElement(providedLargeInterfaceQualifiedName).asType()
                );
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
            statusReport.append(ADD_TO_REPORT_PROFILES_NOT_FOUND_MSG);
        }
        return statusReport.toString();
    }

    private boolean updateSealedInterfacesPermitsMapWithProvidedProfiles(Set<String> largeInterfaceProfilesSet,
                                                                         Element providedLargeInterfaceElement,
                                                                         Element annotatedClassOrInterface,
                                                                         Set<String> providedProfilesForProvidedLargeInterface,
                                                                         Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        Consumer<String> updateSealedInterfacesPermitsMapConsumer = profile ->
                sealedInterfacesPermitsByLargeInterface.get(providedLargeInterfaceElement).merge(
                        profile,
                        asList(annotatedClassOrInterface.toString()),
                        (currentList, newList) -> concat(currentList.stream(), newList.stream()).toList()
                );
        var notFoundProfiles = new HashSet<String>();
        for (var providedProfile : providedProfilesForProvidedLargeInterface) {
            if (providedProfile.isBlank()) {
                // for provided empty profiles, add to the largeInterface sealed profile permits list
                updateSealedInterfacesPermitsMapConsumer.accept(providedLargeInterfaceElement.getSimpleName().toString());
                continue;
            }
            var foundProvidedProfile = false;
            for (var profile : largeInterfaceProfilesSet) {
                if (profile.equals(providedLargeInterfaceElement.getSimpleName().toString())) {
                    continue;
                }
                if (sealedInterfaceNameConvention(providedProfile, providedLargeInterfaceElement)
                        .equals(sealedInterfaceNameConvention(profile, providedLargeInterfaceElement))) {
                    updateSealedInterfacesPermitsMapConsumer.accept(providedProfile);
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