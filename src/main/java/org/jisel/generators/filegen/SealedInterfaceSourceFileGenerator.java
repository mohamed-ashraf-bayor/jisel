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
package org.jisel.generators.filegen;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.contentgen.FinalClassContentGenerator;
import org.jisel.generators.contentgen.ReportContentGenerator;
import org.jisel.generators.contentgen.SealedInterfaceContentGenerator;

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

/**
 * Creates the content of a sealed interface and writes it to the filesystem.<br>
 * The sealed interface generation process also includes the final class and report generation
 */
public final class SealedInterfaceSourceFileGenerator implements SourceFileGenerator { //TODO create interf

    private final SealedInterfaceContentGenerator sealedInterfaceContentGenerator;
    private final FinalClassContentGenerator finalClassContentGenerator;
    private final ReportContentGenerator reportContentGenerator;

    /**
     * SealedInterfaceSourceFileGenerator constructor. Creates needed instances of {@link SealedInterfaceContentGenerator},
     * {@link FinalClassContentGenerator} and {@link ReportContentGenerator}
     */
    public SealedInterfaceSourceFileGenerator() {
        this.sealedInterfaceContentGenerator = new SealedInterfaceContentGenerator();
        this.finalClassContentGenerator = new FinalClassContentGenerator();
        this.reportContentGenerator = new ReportContentGenerator();
    }

    /**
     * @param processingEnvironment                      {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
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
     * @return List of all created source files
     * @throws IOException if an I/O error occured
     */
    public List<String> createSourceFiles(ProcessingEnvironment processingEnvironment,
                                          Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                          Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                          Map<Element, Boolean> unSealValueByLargeInterface) throws IOException {
        var generatedFiles = new ArrayList<String>();
        for (var sealedInterfacesToGenerateMapEntrySet : sealedInterfacesToGenerateByLargeInterface.entrySet()) {
            var largeInterfaceElement = sealedInterfacesToGenerateMapEntrySet.getKey();
            for (var mapEntry : sealedInterfacesToGenerateMapEntrySet.getValue().entrySet()) {
                var profile = mapEntry.getKey();
                var generatedSealedInterfaceName = sealedInterfaceNameConvention(profile, largeInterfaceElement);
                // TODO reorg all this to consider unsealed intrfces
//                createSealedInterfaceFile(processingEnvironment, generatedFiles, sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement), largeInterfaceElement, mapEntry, generatedSealedInterfaceName);
                createSealedInterfaceFile(processingEnvironment, generatedFiles, sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement), largeInterfaceElement, Map.ofEntries(mapEntry), generatedSealedInterfaceName);
                if (unSealValueByLargeInterface.getOrDefault(largeInterfaceElement, false)) {
                    // createUnSealedInterfaceFile(processingEnvironment, generatedFiles, sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement), largeInterfaceElement, mapEntry, generatedSealedInterfaceName);
                }
            }
            createFinalClassFile(processingEnvironment, generatedFiles, largeInterfaceElement, sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement));
            createJiselReportFile(processingEnvironment, generatedFiles, largeInterfaceElement, sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement), sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement));
        }
        return generatedFiles;
    }

    private void createSealedInterfaceFile(ProcessingEnvironment processingEnvironment,
                                           List<String> generatedFiles,
                                           Map<String, List<String>> sealedInterfacesPermitsMap,
                                           Element largeInterfaceElement,
                                           Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                           String generatedSealedInterfaceName) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        try {
            var qualifiedName = packageNameOpt.isPresent() ? packageNameOpt.get() + StringGenerator.DOT + generatedSealedInterfaceName : generatedSealedInterfaceName;
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(sealedInterfaceContentGenerator.generateSealedInterfaceContent(processingEnvironment, sealedInterfacesToGenerateMap, largeInterfaceElement, sealedInterfacesPermitsMap));
            }
            generatedFiles.add(qualifiedName);
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
    }

    private void createFinalClassFile(ProcessingEnvironment processingEnvironment,
                                      List<String> generatedFiles,
                                      Element largeInterfaceElement,
                                      Map<String, List<String>> sealedInterfacesPermitsMap) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        try {
            var qualifiedName = packageNameOpt.isPresent()
                    ? packageNameOpt.get() + StringGenerator.DOT + StringGenerator.UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + StringGenerator.FINAL_CLASS_SUFFIX
                    : StringGenerator.UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + StringGenerator.FINAL_CLASS_SUFFIX;
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(finalClassContentGenerator.generateFinalClassContent(processingEnvironment, largeInterfaceElement, sealedInterfacesPermitsMap));
            }
            generatedFiles.add(qualifiedName);
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
    }

    // todo modify to include gnrtd non-ealed interfcs
    private void createJiselReportFile(ProcessingEnvironment processingEnvironment,
                                       List<String> generatedFiles,
                                       Element largeInterfaceElement,
                                       Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                       Map<String, List<String>> sealedInterfacesPermitsMap) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        try {
            var qualifiedName = packageNameOpt.isPresent()
                    ? packageNameOpt.get() + StringGenerator.DOT + StringGenerator.UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + StringGenerator.JISEL_REPORT_SUFFIX
                    : StringGenerator.UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + StringGenerator.JISEL_REPORT_SUFFIX;
            var fileObject = processingEnvironment.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageNameOpt.isPresent() ? packageNameOpt.get() : StringGenerator.EMPTY_STRING, StringGenerator.UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + StringGenerator.JISEL_REPORT_SUFFIX);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(reportContentGenerator.generateReportContentForLargeInterface(largeInterfaceElement, sealedInterfacesToGenerateMap, sealedInterfacesPermitsMap));
            }
            generatedFiles.add(qualifiedName);
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
    }
}