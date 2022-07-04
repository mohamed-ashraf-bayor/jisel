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
package org.jisel.generators.contentgen;

import javax.lang.model.element.Element;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL_TRANSFORMED;

public sealed interface SourceContentGenerator permits AbstractSealedSourceContentGenerator {

    BinaryOperator<String> DETACHED_INTERFACE_NAME_OP = (profile, rename) -> rename.isBlank() ? profile : rename;

    BiFunction<String, Element, String> DETACHED_TOP_LEVEL_INTERFACE_NAME_FUNC = (profile, largeInterfaceElement) ->
            JISEL_KEYWORD_TOPLEVEL.equals(profile) || JISEL_KEYWORD_TOPLEVEL_TRANSFORMED.equals(profile)
                    ? largeInterfaceElement.getSimpleName().toString()
                    : profile;

    /**
     * Generates the string content of a class or interface
     *
     * @param largeInterfaceElement      {@link Element} instance of the large interface being segregated
     * @param unSeal                     if 'true', indicates that additionally to generating the sealed interfaces' hierarchy,
     *                                   also generate the classic (non-sealed) interfaces hierarchy.<br>
     *                                   If 'false', only generate the sealed interfaces' hierarchy
     * @param sealedInterfaceToGenerate  {@link Map.Entry} instance containing information about the sealed interface to generate
     *                                   (profile as key and value is a Set of abstract methods {@link Element} instances)
     * @param sealedInterfacesPermitsMap {@link Map} containing information about the subtypes permitted by each one of the
     *                                   sealed interfaces to be generated
     * @return the requested class or interface string content
     */
    String generateSourceContent(Element largeInterfaceElement,
                                 boolean unSeal,
                                 Map.Entry<String, Set<Element>> sealedInterfaceToGenerate,
                                 Map<String, List<String>> sealedInterfacesPermitsMap);

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
        if (JISEL_KEYWORD_TOPLEVEL.equals(profile) || JISEL_KEYWORD_TOPLEVEL_TRANSFORMED.equals(profile) || largeInterfaceElement.getSimpleName().toString().equals(profile)) {
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