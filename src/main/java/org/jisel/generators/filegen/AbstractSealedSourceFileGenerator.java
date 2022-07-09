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

import org.jisel.generators.contentgen.AbstractSealedDetachedInterfaceSourceContentGenerator;
import org.jisel.generators.contentgen.AbstractSealedReportContentGenerator;
import org.jisel.generators.contentgen.AbstractSealedSourceContentGenerator;
import org.jisel.generators.contentgen.impl.DetachedInterfaceSourceContentGenerator;
import org.jisel.generators.contentgen.impl.FinalClassSourceContentGenerator;
import org.jisel.generators.contentgen.impl.InterfaceSourceContentGenerator;
import org.jisel.generators.contentgen.impl.ReportContentGenerator;
import org.jisel.generators.filegen.impl.InterfaceSourceFileGenerator;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.jisel.generators.StringGenerator.ALL;
import static org.jisel.generators.StringGenerator.AT_SIGN;
import static org.jisel.generators.StringGenerator.DETACHED;
import static org.jisel.generators.StringGenerator.DETACH_METHODS;
import static org.jisel.generators.StringGenerator.DETACH_PROFILE;
import static org.jisel.generators.StringGenerator.DETACH_RENAME;
import static org.jisel.generators.StringGenerator.DOT;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.FINAL_CLASS_SUFFIX;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_ALL;
import static org.jisel.generators.StringGenerator.UNDERSCORE;
import static org.jisel.generators.StringGenerator.UNSEALED;
import static org.jisel.generators.StringGenerator.generatePackageName;
import static org.jisel.generators.contentgen.AbstractSealedReportContentGenerator.REPORT_FILENAME_SUFFIX;
import static org.jisel.generators.contentgen.SourceContentGenerator.DETACHED_INTERFACE_NAME_FUNC;
import static org.jisel.generators.contentgen.SourceContentGenerator.DETACHED_TOP_LEVEL_INTERFACE_NAME_FUNC;
import static org.jisel.generators.contentgen.SourceContentGenerator.findAllAbstractMethodsForProfile;

/**
 * Exposes contract to fulfill by classes generating Source files, along with a bunch of convenience methods
 */
public abstract sealed class AbstractSealedSourceFileGenerator implements SourceFileGenerator permits InterfaceSourceFileGenerator {

    /**
     * {@link ProcessingEnvironment} instance needed to perform low-level operations on {@link javax.lang.model.element.Element} instances
     */
    protected final ProcessingEnvironment processingEnvironment;

    /**
     * {@link AbstractSealedSourceContentGenerator} instance needed to generate interfaces source content
     */
    protected final AbstractSealedSourceContentGenerator interfaceSourceContentGenerator;

    /**
     * {@link AbstractSealedDetachedInterfaceSourceContentGenerator} instance needed to generate detached interfaces source content
     */
    protected final AbstractSealedDetachedInterfaceSourceContentGenerator detachedInterfaceSourceContentGenerator;

    /**
     * {@link AbstractSealedSourceContentGenerator} instance needed to generate the convenience final class content
     */
    protected final AbstractSealedSourceContentGenerator finalClassSourceContentGenerator;

    /**
     * {@link AbstractSealedReportContentGenerator} instance needed to generate Jisel report file content
     */
    protected final AbstractSealedReportContentGenerator reportContentGenerator;

    /**
     * Injects needed instance of {@link ProcessingEnvironment} and creates/initializes needed instances of
     * {@link InterfaceSourceContentGenerator}, {@link DetachedInterfaceSourceContentGenerator}, {@link FinalClassSourceContentGenerator}
     * and {@link ReportContentGenerator}
     *
     * @param processingEnvironment {@link ProcessingEnvironment} instance needed for source content generation
     */
    protected AbstractSealedSourceFileGenerator(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.interfaceSourceContentGenerator = new InterfaceSourceContentGenerator(this.processingEnvironment);
        this.detachedInterfaceSourceContentGenerator = new DetachedInterfaceSourceContentGenerator(this.processingEnvironment);
        this.finalClassSourceContentGenerator = new FinalClassSourceContentGenerator(this.processingEnvironment);
        this.reportContentGenerator = new ReportContentGenerator(this.processingEnvironment);
    }

    /**
     * Creates source file of the generated sealed interface
     *
     * @param largeInterfaceElement        {@link Element} instance of the large interface being segregated
     * @param sealedInterfaceToGenerate    {@link Map.Entry} instance containing information about the sealed interface to generate
     *                                     (profile as key and value is a Set of abstract methods {@link Element} instances)
     * @param sealedInterfacesPermitsMap   {@link Map} containing information about the subtypes permitted by
     *                                     each one of the sealed interfaces to be generated
     * @param generatedSealedInterfaceName qualified name of the sealed interface file being generated
     * @return qualified name of the generated sealed interface file
     * @throws IOException if a severe error occurs during file creation
     */
    protected String createSealedInterfaceSourceFile(Element largeInterfaceElement,
                                                     Map.Entry<String, Set<Element>> sealedInterfaceToGenerate,
                                                     Map<String, List<String>> sealedInterfacesPermitsMap,
                                                     String generatedSealedInterfaceName) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent() ? packageNameOpt.get() + DOT + generatedSealedInterfaceName : generatedSealedInterfaceName;
        try {
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(
                        interfaceSourceContentGenerator.generateSourceContent(
                                largeInterfaceElement,
                                false,
                                sealedInterfaceToGenerate,
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
     * Creates source file of the generated sealed interface
     *
     * @param largeInterfaceElement          {@link Element} instance of the large interface being segregated
     * @param sealedInterfaceToGenerate      {@link Map.Entry} instance containing information about the sealed interface to generate
     *                                       (profile as key and value is a Set of abstract methods {@link Element} instances)
     * @param sealedInterfacesPermitsMap     {@link Map} containing information about the subtypes permitted by
     *                                       each one of the sealed interfaces to be generated
     * @param generatedUnSealedInterfaceName qualified name of the unsealed interface file being generated
     * @return qualified name of the generated unsealed interface file
     * @throws IOException if a severe error occurs during file creation
     */
    protected String createUnSealedInterfaceSourceFile(Element largeInterfaceElement,
                                                       Map.Entry<String, Set<Element>> sealedInterfaceToGenerate,
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
                        interfaceSourceContentGenerator.generateSourceContent(
                                largeInterfaceElement,
                                true,
                                sealedInterfaceToGenerate,
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
     * Creates source file of the generated convenience final class
     *
     * @param largeInterfaceElement      {@link Element} instance of the large interface being segregated
     * @param sealedInterfacesPermitsMap {@link Map} containing information about the subtypes permitted by
     *                                   each one of the sealed interfaces to be generated
     * @return qualified name of the generated final class file
     * @throws IOException if a severe error occurs during file creation
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
                        finalClassSourceContentGenerator.generateSourceContent(
                                largeInterfaceElement,
                                false,
                                null,
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
     * Creates source files for the detached interfaces
     *
     * @param largeInterfaceElement                        {@link Element} instance of the large interface being segregated
     * @param detachedInterfacesToGenerateByLargeInterface {@link Map} containing information about the detached interfaces to
     *                                                     be generated for each large interface
     * @param sealedInterfacesToGenerateByLargeInterface   {@link Map} containing information about the sealed interfaces to
     *                                                     be generated for each large interface
     * @param sealedInterfacesPermitsByLargeInterface      {@link Map} containing information about the subtypes permitted by each one of the
     *                                                     sealed interfaces to be generated
     * @return {@link List} of generated detached interfaces
     * @throws IOException if a severe error occurs during file creation
     */
    protected List<String> createDetachedInterfacesSourceFiles(Element largeInterfaceElement,
                                                               Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
                                                               Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                               Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) throws IOException {
        var generatedFiles = new ArrayList<String>();
        var mapEntrySet = detachedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).entrySet();
        for (var mapEntry : mapEntrySet) {
            var detachedProfileUniqueKey = mapEntry.getKey();
            var detachAttribs = mapEntry.getValue();
            var profile = detachedProfileUniqueKey.contains(AT_SIGN)
                    ? detachedProfileUniqueKey.substring(0, detachedProfileUniqueKey.indexOf(AT_SIGN))
                    : detachedProfileUniqueKey;
            generatedFiles.addAll(
                    JISEL_KEYWORD_ALL.equals(profile)
                            ? createDetachedInterfacesForAllProfiles(largeInterfaceElement, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface)
                            : List.of(createDetachedInterfaceForProfile(largeInterfaceElement, detachAttribs, false))
            );
        }
        return generatedFiles;
    }

    private List<String> createDetachedInterfacesForAllProfiles(Element largeInterfaceElement,
                                                                Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                                Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) throws IOException {
        var generatedFiles = new ArrayList<String>();
        for (var profile : sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).keySet()) {
            generatedFiles.add(
                    createDetachedInterfaceForProfile(
                            largeInterfaceElement,
                            Map.of(
                                    DETACH_PROFILE, profile,
                                    DETACH_METHODS, findAllAbstractMethodsForProfile(
                                            profile,
                                            sealedInterfacesToGenerateByLargeInterface,
                                            sealedInterfacesPermitsByLargeInterface,
                                            largeInterfaceElement
                                    )
                            ),
                            true)
            );
        }
        return generatedFiles;
    }

    private String createDetachedInterfaceForProfile(Element largeInterfaceElement, Map<String, Object> detachAttribs, boolean detachAll) throws IOException {
        Function<Boolean, String> allSubPackageFunc = detachAllFlag -> detachAllFlag.booleanValue() ? ALL + DOT : EMPTY_STRING;
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var detachedInterfaceSimpleName = DETACHED_TOP_LEVEL_INTERFACE_NAME_FUNC.apply(
                DETACHED_INTERFACE_NAME_FUNC.apply(
                        detachAttribs.get(DETACH_PROFILE).toString(),
                        Optional.ofNullable(detachAttribs.get(DETACH_RENAME)).orElse(EMPTY_STRING).toString()
                ),
                largeInterfaceElement
        );
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + DETACHED.toLowerCase() + DOT + allSubPackageFunc.apply(detachAll) + detachedInterfaceSimpleName
                : DETACHED.toLowerCase() + DOT + detachedInterfaceSimpleName;
        try {
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(
                        detachedInterfaceSourceContentGenerator.generateDetachedInterfaceSourceContent(qualifiedName, detachAttribs, largeInterfaceElement)
                );
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }

    /**
     * Creates the Jisel Generation Report file
     *
     * @param largeInterfaceElement      {@link Element} instance of the large interface being segregated
     * @param unSeal                     boolean, indicates whether to add the generated unselaed interfaces to the report
     * @param sealedInterfacesToGenerate {@link Map} containing information about the generated sealed interfaces
     * @param sealedInterfacesPermitsMap {@link Map} containing information about the subtypes permitted by each one of the
     *                                   sealed interfaces to be generated
     * @return qualified name of the generated report file
     * @throws IOException if a severe error occurs during file creation
     */
    protected String createJiselReportFileForLargeInterface(Element largeInterfaceElement,
                                                            boolean unSeal,
                                                            Map<String, Set<Element>> sealedInterfacesToGenerate,
                                                            Map<String, List<String>> sealedInterfacesPermitsMap,
                                                            List<String> generatedDetachedInterfaces) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + REPORT_FILENAME_SUFFIX
                : UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + REPORT_FILENAME_SUFFIX;
        try {
            var fileObject = processingEnvironment.getFiler().createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    packageNameOpt.isPresent() ? packageNameOpt.get() : EMPTY_STRING,
                    UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + REPORT_FILENAME_SUFFIX
            );
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(
                        reportContentGenerator.generateReportContent(
                                largeInterfaceElement,
                                unSeal,
                                sealedInterfacesToGenerate,
                                sealedInterfacesPermitsMap,
                                generatedDetachedInterfaces
                        )
                );
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }
}