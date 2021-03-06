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

import org.jisel.handlers.AbstractSealedParentChildInheritanceHandler;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;

/**
 * Builds parent-children relationships based on information provided in the Map containing the sealed interfaces information to be generated
 */
public final class SealForParentChildInheritanceHandler extends AbstractSealedParentChildInheritanceHandler {
    @Override
    public void buildInheritanceRelations(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                          Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
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