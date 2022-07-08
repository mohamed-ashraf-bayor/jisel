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

import org.jisel.handlers.impl.SealForParentChildInheritanceHandler;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generators.StringGenerator.TMP_PLACEHOLDER;

/**
 * Exposes contract to fulfill by any class dedicated to building parent-children relations based on information provided in
 * the Map containing the sealed interfaces information to be generated
 */
public abstract sealed class AbstractSealedParentChildInheritanceHandler implements JiselAnnotationHandler permits SealForParentChildInheritanceHandler {

    /**
     * Reads information stored in the Map containing the sealed interfaces information to be generated, and populates another Map storing subtypes of the provided profiles
     *
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the {@link Element} instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface    Map containing information about the subtypes permitted by each one of the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a List of profiles names as the value.
     */
    public abstract void buildInheritanceRelations(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                   Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface);

    /**
     * Populates a Map storing subtypes of the provided profiles
     *
     * @param interfaceElement                           large interface {@link Element} instance
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the {@link Element} instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface    Map containing information about the subtypes permitted by each one of the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a List of profiles names as the value.
     */
    protected void buildSealedInterfacesPermitsMap(Element interfaceElement,
                                                   Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                   Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        sealedInterfacesPermitsByLargeInterface.get(interfaceElement).putAll(
                sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).keySet().stream()
                        .filter(profiles -> profiles.contains(COMMA_SEPARATOR))
                        .collect(toMap(profiles -> profiles, profiles -> asList(profiles.split(COMMA_SEPARATOR))))
        );
        // unique parent interface is present and permits all,
        // if there are other permits already existing then the profiles in those permits lists should be removed from parent interf permits list
        var parentInterfaceSimpleName = interfaceElement.getSimpleName().toString();
        var allPermittedProfiles = sealedInterfacesPermitsByLargeInterface.get(interfaceElement).values().stream().flatMap(Collection::stream).collect(toSet());
        sealedInterfacesPermitsByLargeInterface.get(interfaceElement).put(
                parentInterfaceSimpleName,
                new ArrayList<>( // making the constructed list mutable
                        sealedInterfacesToGenerateByLargeInterface.get(interfaceElement).keySet().stream()
                                .filter(profile -> !parentInterfaceSimpleName.equals(profile))
                                .filter(profile -> !allPermittedProfiles.contains(profile))
                                .toList()
                )
        );
        // if a profile from the List<String> is a key within the same map, then remove it from the list
        eliminateCyclicRelationships(sealedInterfacesPermitsByLargeInterface.get(interfaceElement));
    }

    private void eliminateCyclicRelationships(Map<String, List<String>> sealedInterfacesPermits) {
        var childProfilesListToRemoveByParentProfile = new HashMap<String, List<String>>();
        sealedInterfacesPermits.forEach((parentProfile, childProfilesList) -> {
            var childProfilesListToRemove = new ArrayList<String>();
            for (var childProfile : childProfilesList) {
                if (sealedInterfacesPermits.containsKey(childProfile)) {
                    childProfilesListToRemove.addAll(childProfilesList);
                    childProfilesListToRemove.retainAll(sealedInterfacesPermits.get(childProfile));
                    break;
                }
            }
            childProfilesListToRemoveByParentProfile.put(parentProfile, childProfilesListToRemove);
        });
        sealedInterfacesPermits.keySet().forEach(parentProfile ->
                sealedInterfacesPermits.get(parentProfile).removeAll(childProfilesListToRemoveByParentProfile.get(parentProfile)));
    }

    @Override
    public Map<Element, String> handleAnnotatedElements(Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        buildInheritanceRelations(sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        return Map.of();
    }
}