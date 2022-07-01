// TODO licnse + jdoc all even protctd vars & mthds as well
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

import org.jisel.generators.contentgen.AbstractSealedContentGenerator;
import org.jisel.generators.contentgen.impl.FinalClassContentGenerator;
import org.jisel.generators.contentgen.impl.InterfaceContentGenerator;
import org.jisel.generators.contentgen.impl.ReportContentGenerator;
import org.jisel.generators.filegen.impl.InterfaceSourceFileGenerator;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jisel.generators.StringGenerator.DOT;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.FINAL_CLASS_SUFFIX;
import static org.jisel.generators.StringGenerator.JISEL_REPORT_SUFFIX;
import static org.jisel.generators.StringGenerator.UNDERSCORE;
import static org.jisel.generators.StringGenerator.UNSEALED;
import static org.jisel.generators.StringGenerator.generatePackageName;

/**
 * TODO jdoc...
 */
public abstract sealed class AbstractSealedSourceFileGenerator
        implements SourceFileGenerator
        permits InterfaceSourceFileGenerator {

    protected final ProcessingEnvironment processingEnvironment;
    protected final AbstractSealedContentGenerator interfaceContentGenerator;
    protected final AbstractSealedContentGenerator finalClassContentGenerator;
    protected final AbstractSealedContentGenerator reportContentGenerator;

    /**
     * InterfaceSourceFileGenerator constructor. Injects needed instance of {@link ProcessingEnvironment} and creates
     * needed instances of {@link InterfaceContentGenerator}, {@link FinalClassContentGenerator} and {@link ReportContentGenerator}
     *
     * @param processingEnvironment
     */
    protected AbstractSealedSourceFileGenerator(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.interfaceContentGenerator = new InterfaceContentGenerator(this.processingEnvironment);
        this.finalClassContentGenerator = new FinalClassContentGenerator(this.processingEnvironment);
        this.reportContentGenerator = new ReportContentGenerator(this.processingEnvironment);
    }

    /**
     * TODO jdoc...
     *
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateMap
     * @param sealedInterfacesPermitsMap
     * @param generatedSealedInterfaceName
     * @return
     * @throws IOException
     */
    protected String createSealedInterfaceSourceFile(Element largeInterfaceElement,
                                                     Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                     Map<String, List<String>> sealedInterfacesPermitsMap,
                                                     String generatedSealedInterfaceName) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent() ? packageNameOpt.get() + DOT + generatedSealedInterfaceName : generatedSealedInterfaceName;
        try {
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(
                        interfaceContentGenerator.generateContent(
                                largeInterfaceElement,
                                false,
                                sealedInterfacesToGenerateMap,
                                sealedInterfacesPermitsMap)
                );
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }

    /**
     * TODO jdoc...
     *
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateMap
     * @param sealedInterfacesPermitsMap
     * @param generatedUnSealedInterfaceName
     * @return
     * @throws IOException
     */
    protected String createUnSealedInterfaceSourceFile(Element largeInterfaceElement,
                                                       Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                       Map<String, List<String>> sealedInterfacesPermitsMap,
                                                       String generatedUnSealedInterfaceName) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + UNSEALED.toLowerCase() + DOT + generatedUnSealedInterfaceName
                : UNSEALED.toLowerCase() + DOT + generatedUnSealedInterfaceName;
        try {
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(
                        interfaceContentGenerator.generateContent(
                                largeInterfaceElement,
                                true,
                                sealedInterfacesToGenerateMap,
                                sealedInterfacesPermitsMap
                        )
                );
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }

    /**
     * TODO jdoc...
     *
     * @param largeInterfaceElement
     * @param sealedInterfacesPermitsMap
     * @return
     * @throws IOException
     */
    protected String createFinalClassFile(Element largeInterfaceElement,
                                          Map<String, List<String>> sealedInterfacesPermitsMap) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX
                : UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX;
        try {
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(
                        finalClassContentGenerator.generateContent(
                                largeInterfaceElement,
                                false,
                                Map.of(),
                                sealedInterfacesPermitsMap
                        )
                );
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }

    /**
     * TODO jdoc...
     *
     * @param largeInterfaceElement
     * @param unSeal
     * @param sealedInterfacesToGenerateMap
     * @param sealedInterfacesPermitsMap
     * @return
     * @throws IOException
     */
    protected String createJiselReportFile(Element largeInterfaceElement,
                                           boolean unSeal,
                                           Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                           Map<String, List<String>> sealedInterfacesPermitsMap) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + JISEL_REPORT_SUFFIX
                : UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + JISEL_REPORT_SUFFIX;
        try {
            var fileObject = processingEnvironment.getFiler().createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    packageNameOpt.isPresent() ? packageNameOpt.get() : EMPTY_STRING,
                    UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + JISEL_REPORT_SUFFIX
            );
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(
                        reportContentGenerator.generateContent(
                                largeInterfaceElement,
                                unSeal,
                                sealedInterfacesToGenerateMap,
                                sealedInterfacesPermitsMap
                        )
                );
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }
}