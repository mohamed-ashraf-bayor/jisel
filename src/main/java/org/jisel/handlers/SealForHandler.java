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

import org.jisel.annotations.SealFor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.ArrayList;
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

/**
 * Handles all elements annotated with &#64;{@link SealFor}
 */
public final class SealForHandler implements JiselAnnotationHandler {

    private final AnnotationInfoCollectionHandler annotationInfoCollectionHandler;
    private final ParentChildInheritanceHandler parentChildInheritanceHandler;

    /**
     * SealForProfileHandler constructor. Instantiates needed instances of {@link SealForInfoCollectionHandler}
     * and {@link SealForParentChildInheritanceHandler}
     */
    public SealForHandler() {
        this.annotationInfoCollectionHandler = new SealForInfoCollectionHandler();
        this.parentChildInheritanceHandler = new SealForParentChildInheritanceHandler();
    }

    @Override
    public Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                        final Set<Element> allAnnotatedElements,
                                                        final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var statusReport = new HashMap<Element, String>();
        var allAnnotatedElementsToProcess = allAnnotatedElements.stream()
                .filter(element -> ElementKind.METHOD.equals(element.getKind()))
                .filter(element -> ElementKind.INTERFACE.equals(element.getEnclosingElement().getKind()))
                // only where @TopLevel is used
                .filter(element -> sealedInterfacesToGenerateByLargeInterface.containsKey(element.getEnclosingElement()))
                // only if the same method is NOT also annotated with @TopLevel
                .filter(element -> !sealedInterfacesToGenerateByLargeInterface.get(element.getEnclosingElement()).get(element.getEnclosingElement().getSimpleName().toString()).contains(element))
                .collect(toSet());
        // statusReport - only add the name of the processed large interfaces with no description
        allAnnotatedElementsToProcess.forEach(element -> statusReport.put(element.getEnclosingElement(), EMPTY_STRING));
        annotationInfoCollectionHandler.populateSealedInterfacesMap(processingEnv, allAnnotatedElementsToProcess, sealedInterfacesToGenerateByLargeInterface);
        parentChildInheritanceHandler.buildInheritanceRelations(sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        return statusReport;
    }
}

/**
 * Collects necessary information from the annotated elements, in order to populate the Map containing the sealed
 * interfaces information to be generated
 */
final class SealForInfoCollectionHandler implements AnnotationInfoCollectionHandler {

    @Override
    public void populateSealedInterfacesMap(final ProcessingEnvironment processingEnv,
                                            final Set<Element> allAnnotatedElements,
                                            final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
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

    private void extractProfilesAndPopulateMaps(final Element interfaceElement,
                                                final Set<String> providedProfilesSet,
                                                final Element annotatedMethod,
                                                final Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByInterface) {
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

/**
 * Builds parent-children relationships based on information provided in the Map containing the sealed interfaces information to be generated
 */
final class SealForParentChildInheritanceHandler implements ParentChildInheritanceHandler {
    @Override
    public void buildInheritanceRelations(final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                          final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        sealedInterfacesToGenerateByLargeInterface.keySet().forEach(interfaceElement -> {
            sealedInterfacesPermitsByLargeInterface.put(interfaceElement, new HashMap<>()); // start with initializing sealedInterfacesPermitsByLargeInterface with empty mutable maps
            // promote profiles with empty methods to parent level
            var allProfilesToRemove = new HashSet<String>();
            sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).keySet().forEach(concatenatedProfiles -> {
                var profilesArray = concatenatedProfiles.split(COMMA_SEPARATOR);
                if (profilesArray.length > 1) { // only concatenated profiles are processed
                    for (var profile : profilesArray) {
                        var profileMethodsOpt = Optional.ofNullable(sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).get(profile));
                        if (profileMethodsOpt.isPresent() && profileMethodsOpt.get().isEmpty()) {
                            sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).put(
                                    profile,
                                    sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).get(concatenatedProfiles)
                            );
                            sealedInterfacesPermitsByLargeInterface.get(interfaceElement).put(
                                    profile,
                                    new ArrayList<>(Arrays.stream(profilesArray).filter(profileName -> !profile.equals(profileName)).toList())
                            );
                            allProfilesToRemove.add(concatenatedProfiles);
                            break;
                        }
                    }
                }
            });
            allProfilesToRemove.forEach(sealedInterfacesToGenerateByLargeInterface.get(interfaceElement)::remove); // only concatenated profiles are removed
            // and complete populating the sealedInterfacesPermitsByLargeInterface map
            buildSealedInterfacesPermitsMap(interfaceElement, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        });
    }
}