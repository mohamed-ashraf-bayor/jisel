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

import org.jisel.handlers.impl.TopLevelHandler;
import org.jisel.handlers.impl.UnSealHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL_TRANSFORMED;

/**
 * Exposes contract to fulfill by any class handling all elements annotated with Jisel annotations
 */
public sealed interface JiselAnnotationHandler
        permits AbstractSealedSealForHandler, AbstractSealedAddToHandler, TopLevelHandler, UnSealHandler, AbstractSealedDetachHandler,
        AbstractSealedAnnotationInfoCollectionHandler, AbstractSealedParentChildInheritanceHandler {

    /**
     * Reads values of all attributes provided through the use of Jisel annotations and populates the provided Map arguments
     *
     * @param allAnnotatedElements                       {@link Set} of {@link Element} instances representing all classes annotated with Jisel annotations
     * @param sealedInterfacesToGenerateByLargeInterface Map containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface    Map containing information about the subtypes permitted by each one of the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a List of profiles names as the value.
     *                                                   // @param detachedInterfacesToGenerateByLargeInterface // TODO map content: Set<Element> methodsSet | String rename() | Class<?>[] superInterfaces | Class<?>[] firstSuperInterfaceGenerics | Class<?>[] secondSuperInterfaceGenerics | Class<?>[] thirdSuperInterfaceGenerics | String[] applyAnnotations
     * @return a status report as a string value for each one of the large interfaces to be segregated
     */
    Map<Element, String> handleAnnotatedElements(Set<Element> allAnnotatedElements,
                                                 Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                 Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface);

    /**
     * // TODO jdoc
     *
     * @param profile
     * @param sealedInterfacesToGenerateByLargeInterface
     * @param sealedInterfacesPermitsByLargeInterface
     * @param largeInterfaceElement
     * @return
     */
    static Set<Element> findAllAbstractMethodsForProfile(String profile,
                                                         Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                         Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                         Element largeInterfaceElement) {
        var methodsElementSet = new HashSet<Element>();
        if (profile.equals(JISEL_KEYWORD_TOPLEVEL) || profile.equals(JISEL_KEYWORD_TOPLEVEL_TRANSFORMED)) {
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(largeInterfaceElement.getSimpleName().toString()));
        } else {
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(profile));
            addMethodsFromParentProfiles(profile, methodsElementSet, largeInterfaceElement, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        }
        return methodsElementSet;
    }

    /**
     * TODO jdoc...
     *
     * @param processedProfile
     * @param methodsElementSet
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateByLargeInterface
     * @param sealedInterfacesPermitsByLargeInterface
     */
    private static void addMethodsFromParentProfiles(String processedProfile,
                                                     Set<Element> methodsElementSet,
                                                     Element largeInterfaceElement,
                                                     Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                     Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        Function<String, Optional<Map.Entry<String, List<String>>>> findProfileAsChild = profile -> sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement).entrySet().stream()
                .filter(mapEntry -> mapEntry.getValue().contains(profile))
                .findFirst();
        var profileOpt = findProfileAsChild.apply(processedProfile);
        if (profileOpt.isPresent()) {
            var parentProfile = profileOpt.get().getKey();
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(parentProfile));
            addMethodsFromParentProfiles(parentProfile, methodsElementSet, largeInterfaceElement, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        }
    }
}