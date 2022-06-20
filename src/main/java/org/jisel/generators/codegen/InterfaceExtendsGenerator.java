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
package org.jisel.generators.codegen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generates the "extends" clause of a sealed interface definition, along with the list of the parent interfaces
 */
public final class InterfaceExtendsGenerator implements ExtendsGenerator {
    @Override
    public void generateExtendsClauseFromPermitsMapAndProcessedProfile(ProcessingEnvironment processingEnvironment,
                                                                       StringBuilder sealedInterfaceContent,
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
                    generateCode(
                            sealedInterfaceContent,
                            superInterfacesList
                    );
                }
            }
        });
    }
}