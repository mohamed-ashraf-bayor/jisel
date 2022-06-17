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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.jisel.generators.StringGenerator.removeDoubleSpaceOccurrences;

/**
 * Generates the content of a sealed interface
 */
public final class SealedInterfaceContentGenerator extends SealedAbstractContentGenerator {

    /**
     * Generates the content of a sealed interface
     *
     * @param processingEnvironment         {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param sealedInterfacesToGenerateMap {@link java.util.Map} instance containing information about the sealed interface to be generated.
     *                                      In this specific case, though a Map, only a single entry is expected as content.
     * @param largeInterfaceElement         {@link Element} instance of the large interface being segregated
     * @param sealedInterfacesPermitsMap    Map containing information about the subtypes permitted by each one of the sealed interfaces to be generated
     * @return the string content of the sealed interface to generate
     */
    public String generateSealedInterfaceContent(ProcessingEnvironment processingEnvironment,
                                                 Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                 Element largeInterfaceElement,
                                                 Map<String, List<String>> sealedInterfacesPermitsMap) {
        var sealedInterfaceContent = new StringBuilder();
        // package name
        generatePackageName(largeInterfaceElement).ifPresent(name -> sealedInterfaceContent.append(format("%s %s;%n%n", PACKAGE, name)));
        // javaxgenerated
        javaxGeneratedGenerator.generateCode(sealedInterfaceContent, List.of());
        // public sealed interface
        var sealedInterfacesToGenerateMapEntry = (Map.Entry<String, Set<Element>>) sealedInterfacesToGenerateMap.entrySet().toArray()[0]; // only 1 entry expected within the map
        var profile = sealedInterfacesToGenerateMapEntry.getKey();
        sealedInterfaceContent.append(format(
                "%s %s ",
                PUBLIC_SEALED_INTERFACE,
                sealedInterfaceNameConvention(profile, largeInterfaceElement)
        ));
        // list of extends
        extendsGenerator.generateExtendsClauseFromPermitsMapAndProcessedProfile(processingEnvironment, sealedInterfaceContent, sealedInterfacesPermitsMap, profile, largeInterfaceElement);
        // list of permits
        permitsGenerator.generatePermitsClauseFromPermitsMapAndProcessedProfile(sealedInterfaceContent, sealedInterfacesPermitsMap, profile, largeInterfaceElement);
        // opening bracket after permits list
        sealedInterfaceContent.append(format(" %s%n ", OPENING_CURLY_BRACE));
        // list of methods
        methodsGenerator.generateAbstractMethodsFromElementsSet(sealedInterfaceContent, sealedInterfacesToGenerateMapEntry.getValue());
        // closing bracket
        sealedInterfaceContent.append(CLOSING_CURLY_BRACE);
        //
        return removeDoubleSpaceOccurrences(sealedInterfaceContent.toString());
    }
}