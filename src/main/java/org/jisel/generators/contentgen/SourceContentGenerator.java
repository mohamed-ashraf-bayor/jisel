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
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL_REPLACEMENT;

/**
 * Exposes contract of a class generating the string content of a class or interface
 */
public sealed interface SourceContentGenerator permits AbstractSealedSourceContentGenerator {

    /**
     * Function returning the name of the detached interface being generated, based on the provided 'profile' and 'rename' attributes values<br>
     * If 'rename' is empty (blank), interface name = 'profile', otherwise use the provided 'rename' value
     */
    BinaryOperator<String> DETACHED_INTERFACE_NAME_FUNC = (profile, rename) -> rename.isBlank() ? profile : rename;

    /**
     * Function returning the name of the detached interface being generated if the provided profile is "(toplevel)"
     */
    BiFunction<String, Element, String> DETACHED_TOP_LEVEL_INTERFACE_NAME_FUNC = (profile, largeInterfaceElement) ->
            JISEL_KEYWORD_TOPLEVEL.equalsIgnoreCase(profile) || JISEL_KEYWORD_TOPLEVEL_REPLACEMENT.equalsIgnoreCase(profile)
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
     * Finds all abstract methods of the interface generated for the provided profile.<br>
     * All parent and super-parent methods are also included
     *
     * @param profile                                    name of the profile. Must be one of the profiles defined with the
     *                                                   &#64;{@link org.jisel.annotations.SealFor} annotation
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the {@link Element} instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface    {@link Map} containing information about the subtypes permitted by each one of the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a List of profiles names as the value.
     * @param largeInterfaceElement                      {@link Element} instance of the large interface being segregated
     * @return a {@link Set} of method {@link Element} instances
     */
    static Set<Element> findAllAbstractMethodsForProfile(String profile,
                                                         Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                         Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                         Element largeInterfaceElement) {
        var methodsElementSet = new HashSet<Element>();
        if (JISEL_KEYWORD_TOPLEVEL.equalsIgnoreCase(profile)
                || JISEL_KEYWORD_TOPLEVEL_REPLACEMENT.equalsIgnoreCase(profile)
                || largeInterfaceElement.getSimpleName().toString().equals(profile)) {
            methodsElementSet.addAll(
                    sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(largeInterfaceElement.getSimpleName().toString())
            );
        } else {
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(profile));
            addMethodsFromParentProfiles(
                    profile,
                    methodsElementSet,
                    largeInterfaceElement,
                    sealedInterfacesToGenerateByLargeInterface,
                    sealedInterfacesPermitsByLargeInterface
            );
        }
        return methodsElementSet;
    }

    // recursive search for abstract methods for the provided profile
    private static void addMethodsFromParentProfiles(String processedProfile,
                                                     Set<Element> methodsElementSet,
                                                     Element largeInterfaceElement,
                                                     Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                     Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        Function<String, Optional<Map.Entry<String, List<String>>>> findProfileAsChild = profile ->
                sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement).entrySet().stream()
                        .filter(mapEntry -> mapEntry.getValue().contains(profile))
                        .findFirst();
        var profileOpt = findProfileAsChild.apply(processedProfile);
        if (profileOpt.isPresent()) {
            var parentProfile = profileOpt.get().getKey();
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(parentProfile));
            addMethodsFromParentProfiles(
                    parentProfile,
                    methodsElementSet,
                    largeInterfaceElement,
                    sealedInterfacesToGenerateByLargeInterface,
                    sealedInterfacesPermitsByLargeInterface
            );
        }
    }
}