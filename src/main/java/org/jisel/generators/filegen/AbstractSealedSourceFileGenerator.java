// TODO licnse + jdoc all even protctd vars & mthds as well
package org.jisel.generators.filegen;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.contentgen.AbstractSealedContentGenerator;
import org.jisel.generators.contentgen.FinalClassSealedContentGenerator;
import org.jisel.generators.contentgen.InterfaceSealedContentGenerator;
import org.jisel.generators.contentgen.ReportSealedContentGenerator;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract sealed class AbstractSealedSourceFileGenerator implements StringGenerator permits InterfaceSourceFileGenerator {

    protected final AbstractSealedContentGenerator interfaceContentGenerator;
    protected final AbstractSealedContentGenerator finalClassContentGenerator;
    protected final AbstractSealedContentGenerator reportContentGenerator;

    /**
     * SealedInterfaceSourceFileGenerator constructor. Creates needed instances of {@link InterfaceSealedContentGenerator},
     * {@link FinalClassSealedContentGenerator} and {@link ReportSealedContentGenerator}
     */
    protected AbstractSealedSourceFileGenerator() {
        this.interfaceContentGenerator = new InterfaceSealedContentGenerator();
        this.finalClassContentGenerator = new FinalClassSealedContentGenerator();
        this.reportContentGenerator = new ReportSealedContentGenerator();
    }

    /**
     * @param processingEnvironment
     * @param sealedInterfacesToGenerateByLargeInterface
     * @param sealedInterfacesPermitsByLargeInterface
     * @param unSealValueByLargeInterface
     * @return
     * @throws IOException
     */
    public abstract List<String> createSourceFiles(ProcessingEnvironment processingEnvironment,
                                                   Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                   Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                   Map<Element, Boolean> unSealValueByLargeInterface) throws IOException;

    protected String createSealedInterfaceSourceFile(ProcessingEnvironment processingEnvironment,
                                                     Element largeInterfaceElement,
                                                     Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                     Map<String, List<String>> sealedInterfacesPermitsMap,
                                                     String generatedSealedInterfaceName) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent() ? packageNameOpt.get() + DOT + generatedSealedInterfaceName : generatedSealedInterfaceName;
        try {
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(interfaceContentGenerator.generateContent(processingEnvironment, largeInterfaceElement, false,
                        sealedInterfacesToGenerateMap, sealedInterfacesPermitsMap));
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }

    protected String createUnSealedInterfaceSourceFile(ProcessingEnvironment processingEnvironment,
                                                       Element largeInterfaceElement,
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
                out.println(interfaceContentGenerator.generateContent(processingEnvironment, largeInterfaceElement, true,
                        sealedInterfacesToGenerateMap, sealedInterfacesPermitsMap));
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }

    protected String createFinalClassFile(ProcessingEnvironment processingEnvironment,
                                          Element largeInterfaceElement,
                                          Map<String, List<String>> sealedInterfacesPermitsMap) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX
                : UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX;
        try {
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(finalClassContentGenerator.generateContent(processingEnvironment, largeInterfaceElement, false,
                        Map.of(), sealedInterfacesPermitsMap));
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }

    protected String createJiselReportFile(ProcessingEnvironment processingEnvironment,
                                           Element largeInterfaceElement,
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
                out.println(reportContentGenerator.generateContent(processingEnvironment, largeInterfaceElement, unSeal,
                        sealedInterfacesToGenerateMap, sealedInterfacesPermitsMap));
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }
}