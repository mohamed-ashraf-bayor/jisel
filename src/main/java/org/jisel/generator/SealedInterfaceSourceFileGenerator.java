/**
 * Copyright (c) 2021-2022 Mohamed Ashraf Bayor.
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
package org.jisel.generator;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SealedInterfaceSourceFileGenerator implements StringGenerator {

    private final SealedInterfaceContentGenerator sealedInterfaceContentGenerator;
    private final FinalClassGenerator finalClassGenerator;
    private final ReportGenerator reportGenerator;

    public SealedInterfaceSourceFileGenerator() {
        this.sealedInterfaceContentGenerator = new SealedInterfaceContentGenerator();
        this.finalClassGenerator = new FinalClassGenerator();
        this.reportGenerator = new ReportGenerator();
    }

    public List<String> createSourceFiles(final ProcessingEnvironment processingEnvironment,
                                          final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                          final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) throws IOException {
        var generatedFiles = new ArrayList<String>();
        for (var sealedInterfacesToGenerateMapEntrySet : sealedInterfacesToGenerateByLargeInterface.entrySet()) {
            var largeInterfaceElement = sealedInterfacesToGenerateMapEntrySet.getKey();
            for (var sealedInterfacesToGenerate : sealedInterfacesToGenerateMapEntrySet.getValue().entrySet()) {
                var profile = sealedInterfacesToGenerate.getKey();
                var generatedSealedInterfaceName = sealedInterfaceNameConvention(profile, largeInterfaceElement);
                createSealedInterfaceFile(processingEnvironment, generatedFiles, sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement), largeInterfaceElement, sealedInterfacesToGenerate, generatedSealedInterfaceName);
            }
            createFinalClassFile(processingEnvironment, generatedFiles, largeInterfaceElement, sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement));
            createJiselReportFile(processingEnvironment, generatedFiles, largeInterfaceElement, sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement), sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement));
        }
        return generatedFiles;
    }

    private void createSealedInterfaceFile(final ProcessingEnvironment processingEnvironment,
                                           final List<String> generatedFiles,
                                           final Map<String, List<String>> sealedInterfacesPermitsMap,
                                           final Element largeInterfaceElement,
                                           final Map.Entry<String, Set<Element>> sealedInterfacesToGenerateMapEntrySet,
                                           final String generatedSealedInterfaceName) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        try {
            var qualifiedName = packageNameOpt.isPresent() ? packageNameOpt.get() + DOT + generatedSealedInterfaceName : generatedSealedInterfaceName;
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(sealedInterfaceContentGenerator.generateSealedInterfaceContent(processingEnvironment, sealedInterfacesToGenerateMapEntrySet, largeInterfaceElement, sealedInterfacesPermitsMap));
            }
            generatedFiles.add(qualifiedName);
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
    }

    private void createFinalClassFile(final ProcessingEnvironment processingEnvironment,
                                      final List<String> generatedFiles,
                                      final Element largeInterfaceElement,
                                      final Map<String, List<String>> sealedInterfacesPermitsMap) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        try {
            var qualifiedName = packageNameOpt.isPresent()
                    ? packageNameOpt.get() + DOT + UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX
                    : UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX;
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(finalClassGenerator.generateFinalClassContent(processingEnvironment, largeInterfaceElement, sealedInterfacesPermitsMap));
            }
            generatedFiles.add(qualifiedName);
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
    }

    private void createJiselReportFile(final ProcessingEnvironment processingEnvironment,
                                       final List<String> generatedFiles,
                                       final Element largeInterfaceElement,
                                       final Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                       final Map<String, List<String>> sealedInterfacesPermitsMap) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        try {
            var qualifiedName = packageNameOpt.isPresent()
                    ? packageNameOpt.get() + DOT + UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + JISEL_REPORT_SUFFIX
                    : UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + JISEL_REPORT_SUFFIX;
            var fileObject = processingEnvironment.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageNameOpt.isPresent() ? packageNameOpt.get() : EMPTY_STRING, UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + JISEL_REPORT_SUFFIX);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(reportGenerator.generateReportForBloatedInterface(largeInterfaceElement, sealedInterfacesToGenerateMap, sealedInterfacesPermitsMap));
            }
            generatedFiles.add(qualifiedName);
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
    }
}