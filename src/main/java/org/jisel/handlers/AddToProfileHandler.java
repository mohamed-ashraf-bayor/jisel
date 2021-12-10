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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

public final class AddToProfileHandler implements JiselAnnotationHandler {

    @Override
    public Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                        Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByBloatedInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByBloatedInterface) {
        var annotatedClassesAndInterfaces = allAnnotatedElements.stream()
                .filter(element -> !element.getClass().isEnum())
                .filter(element -> ElementKind.CLASS.equals(element.getKind())
                        || ElementKind.INTERFACE.equals(element.getKind())
                        || ElementKind.RECORD.equals(element.getKind()))
                .collect(toSet());
        var statusReport = new HashMap<Element, String>();
        annotatedClassesAndInterfaces.forEach(annotatedClassOrInterface -> statusReport.put(
                annotatedClassOrInterface,
                processAnnotatedElement(processingEnv, annotatedClassOrInterface, sealedInterfacesToGenerateByBloatedInterface, sealedInterfacesPermitsByBloatedInterface)
        ));
        return statusReport;
    }

    private String processAnnotatedElement(final ProcessingEnvironment processingEnv,
                                           final Element annotatedClassOrInterface,
                                           final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByBloatedInterface,
                                           final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByBloatedInterface) {
        var statusReport = new StringBuilder();
        var addToProfileProvidedProfilesSet = buildProvidedProfilesSet(processingEnv, annotatedClassOrInterface);
        if (addToProfileProvidedProfilesSet.isEmpty()) {
            // do not process if no profiles are provided
            return statusReport.toString();
        }
        var providedSuperInterfacesSet = buildProvidedInterfacesSet(processingEnv, annotatedClassOrInterface);
        // browse sealedInterfacesToGenerate 1st and then add more info to sealedInterfacesPermits
        var found = false;
        for (var mapEntry : sealedInterfacesToGenerateByBloatedInterface.entrySet()) {
            var bloatedInterfaceElement = mapEntry.getKey();
            var annotatedMethodsByProfile = mapEntry.getValue();
            for (var profile : annotatedMethodsByProfile.keySet()) {
                for (var superInterfaceQualifiedName : providedSuperInterfacesSet) {
                    var sealedInterfaceName = sealedInterfaceNameConvention(profile, bloatedInterfaceElement);
                    if (superInterfaceQualifiedName.contains(sealedInterfaceName)) {
                        // add permits for each profile only if the profiles exist for the superinterface
                        var bloatedInterfaceProvidedProfilesList = annotatedMethodsByProfile.keySet().stream()
                                .filter(bloatedInterfaceProvidedProfile -> sealedInterfaceNameConvention(bloatedInterfaceProvidedProfile, bloatedInterfaceElement).equals(sealedInterfaceName))
                                .toList();
                        found = updateSealedInterfacesPermitsMapWithProvidedProfiles(
                                bloatedInterfaceProvidedProfilesList, addToProfileProvidedProfilesSet, annotatedClassOrInterface,
                                bloatedInterfaceElement, sealedInterfacesPermitsByBloatedInterface
                        );
                    }
                }
            }
        }
        if (!found) {
            statusReport.append(ADD_TO_PROFILE_REPORT_MSG);
        }
        return statusReport.toString();
    }

    private Set<String> buildProvidedInterfacesSet(final ProcessingEnvironment processingEnv, final Element annotatedClassOrInterface) {
        return processingEnv.getTypeUtils().directSupertypes(annotatedClassOrInterface.asType()).stream()
                .map(Object::toString)
                .filter(typeString -> !typeString.contains(JAVA_LANG_OBJECT))
                .collect(toSet());
    }

    private boolean updateSealedInterfacesPermitsMapWithProvidedProfiles(final List<String> bloatedInterfaceProvidedProfilesList,
                                                                         final Set<String> addToProfileProvidedProfilesSet,
                                                                         final Element annotatedClassOrInterface,
                                                                         final Element bloatedInterfaceElement,
                                                                         final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByBloatedInterface) {
        var found = false;
        for (var bloatedInterfaceProvidedProfile : bloatedInterfaceProvidedProfilesList) { // might contain SEPARATOR
            for (var addToProfileProvidedProfile : addToProfileProvidedProfilesSet) {
                if (asList(bloatedInterfaceProvidedProfile.split(COMMA_SEPARATOR)).contains(addToProfileProvidedProfile)) {
                    sealedInterfacesPermitsByBloatedInterface.get(bloatedInterfaceElement).merge(
                            bloatedInterfaceProvidedProfile,
                            asList(annotatedClassOrInterface.toString()),
                            (currentList, newList) -> concat(currentList.stream(), newList.stream()).toList()
                    );
                    found = true;
                } else {
                    found = false;
                }
            }
        }
        return found;
    }
}