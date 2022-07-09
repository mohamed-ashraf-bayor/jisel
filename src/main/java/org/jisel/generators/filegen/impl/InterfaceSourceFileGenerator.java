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
package org.jisel.generators.filegen.impl;

import org.jisel.generators.filegen.AbstractSealedSourceFileGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jisel.generators.StringGenerator.DETACHED;
import static org.jisel.generators.StringGenerator.DOT;
import static org.jisel.generators.StringGenerator.SEALED_PREFIX;
import static org.jisel.generators.StringGenerator.sealedInterfaceNameConvention;

/**
 * Creates the content of an interface and writes it to the filesystem.<br>
 * The sealed interface generation process also includes the final class and report generation
 */
public final class InterfaceSourceFileGenerator extends AbstractSealedSourceFileGenerator {

    /**
     * InterfaceSourceFileGenerator constructor. Injects needed instance of {@link ProcessingEnvironment}
     *
     * @param processingEnvironment
     */
    public InterfaceSourceFileGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public List<String> createSourceFiles(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                          Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                          Map<Element, Boolean> unSealValueByLargeInterface,
                                          Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface) throws IOException {
        var allGeneratedFiles = new ArrayList<String>();
        for (var sealedInterfacesToGenerateMapEntry : sealedInterfacesToGenerateByLargeInterface.entrySet()) {
            var largeInterfaceElement = sealedInterfacesToGenerateMapEntry.getKey();
            var unSeal = unSealValueByLargeInterface.getOrDefault(largeInterfaceElement, false).booleanValue();
            var generatedFilesForLargeInterface = new ArrayList<String>();
            for (var mapEntry : sealedInterfacesToGenerateMapEntry.getValue().entrySet()) {
                var profile = mapEntry.getKey();
                var generatedSealedInterfaceName = sealedInterfaceNameConvention(profile, largeInterfaceElement);
                var generatedUnSealedInterfaceName = generatedSealedInterfaceName.substring(SEALED_PREFIX.length());
                generatedFilesForLargeInterface.add(
                        // 3rd arg is a Map made of only 1 instance of Map.Entry<String, Set<Element>>
                        createSealedInterfaceSourceFile(
                                largeInterfaceElement,
                                mapEntry,
                                sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement),
                                generatedSealedInterfaceName
                        )
                );
                if (unSeal) {
                    generatedFilesForLargeInterface.add(
                            createUnSealedInterfaceSourceFile(
                                    largeInterfaceElement,
                                    mapEntry,
                                    sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement),
                                    generatedUnSealedInterfaceName
                            )
                    );
                }

            }
            generatedFilesForLargeInterface.add(
                    createFinalClassFile(
                            largeInterfaceElement,
                            sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement)
                    )
            );
            if (detachedInterfacesToGenerateByLargeInterface.containsKey(largeInterfaceElement)) {
                generatedFilesForLargeInterface.addAll(
                        createDetachedInterfacesSourceFiles(
                                largeInterfaceElement,
                                detachedInterfacesToGenerateByLargeInterface,
                                sealedInterfacesToGenerateByLargeInterface,
                                sealedInterfacesPermitsByLargeInterface
                        )
                );
            }
            generatedFilesForLargeInterface.add(
                    createJiselReportFileForLargeInterface(
                            largeInterfaceElement,
                            unSeal,
                            sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement),
                            sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement),
                            generatedFilesForLargeInterface.stream()
                                    .filter(qualifiedName -> qualifiedName.contains(DETACHED.toLowerCase() + DOT))
                                    .toList()
                    )
            );
            allGeneratedFiles.addAll(generatedFilesForLargeInterface);
        }
        return allGeneratedFiles;
    }
}