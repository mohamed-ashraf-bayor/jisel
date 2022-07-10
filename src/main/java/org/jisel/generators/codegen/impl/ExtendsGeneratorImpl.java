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
package org.jisel.generators.codegen.impl;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.codegen.ExtendsGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.INF_SIGN;
import static org.jisel.generators.StringGenerator.JAVA_LANG_OBJECT;
import static org.jisel.generators.StringGenerator.SUP_SIGN;
import static org.jisel.generators.StringGenerator.removeDotClass;
import static org.jisel.generators.StringGenerator.sealedInterfaceNameConvention;
import static org.jisel.generators.StringGenerator.unSealedInterfaceNameConvention;

/**
 * Generates the "extends" clause of an interface declaration, along with the list of the parent interfaces
 */
public final class ExtendsGeneratorImpl implements ExtendsGenerator {

    private final ProcessingEnvironment processingEnvironment;

    /**
     * Injects needed instance of {@link ProcessingEnvironment}
     *
     * @param processingEnvironment {@link ProcessingEnvironment} instance needed for performing low-level operations on {@link Element} instances
     */
    public ExtendsGeneratorImpl(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    public void generateExtendsClauseFromPermitsMapAndProcessedProfile(StringBuilder sealedInterfaceContent,
                                                                       Map<String, List<String>> permitsMap,
                                                                       String processedProfile,
                                                                       Element largeInterfaceElement,
                                                                       boolean unSeal) {
        Optional.ofNullable(permitsMap).ifPresent(nonNullPermitsMap -> {
            var parentList = nonNullPermitsMap.entrySet().stream()
                    .filter(permitsMapEntry -> permitsMapEntry.getValue().contains(processedProfile))
                    .map(permitsMapEntry -> unSeal ? unSealedInterfaceNameConvention(permitsMapEntry.getKey(), largeInterfaceElement)
                            : sealedInterfaceNameConvention(permitsMapEntry.getKey(), largeInterfaceElement))
                    .toList();
            if (!parentList.isEmpty()) {
                generateCode(sealedInterfaceContent, parentList);
            } else {
                // only for largeInterface sealed interface generation, add interfaces it extends if any
                var superInterfacesList = processingEnvironment.getTypeUtils().directSupertypes(largeInterfaceElement.asType()).stream()
                        .map(Object::toString)
                        .filter(superType -> !superType.contains(JAVA_LANG_OBJECT))
                        .toList();
                if (largeInterfaceElement.getSimpleName().toString().equals(processedProfile) && !superInterfacesList.isEmpty()) {
                    generateCode(sealedInterfaceContent, superInterfacesList);
                }
            }
        });
    }

    @Override
    public void generateExtendsClauseFromSuperInterfacesList(StringBuilder sealedInterfaceContent,
                                                             List<String> superInterfaces,
                                                             Map<String, List<String>> superInterfacesGenerics) {
        var superInterfacesWithGenericsList = superInterfaces.stream()
                .map(supIntrf ->
                        removeDotClass(supIntrf) + (
                                superInterfacesGenerics.containsKey(supIntrf) && !superInterfacesGenerics.get(supIntrf).isEmpty()
                                        ? INF_SIGN + superInterfacesGenerics.get(supIntrf).stream().map(StringGenerator::removeDotClass).collect(joining(COMMA_SEPARATOR)) + SUP_SIGN
                                        : EMPTY_STRING
                        )
                )
                .toList();
        if (!superInterfacesWithGenericsList.isEmpty()) {
            generateCode(sealedInterfaceContent, superInterfacesWithGenericsList);
        }
    }
}