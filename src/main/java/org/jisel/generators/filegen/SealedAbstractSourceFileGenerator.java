// TODO licnse + jdoc all
package org.jisel.generators.filegen;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.contentgen.FinalClassContentGenerator;
import org.jisel.generators.contentgen.InterfaceContentGenerator;
import org.jisel.generators.contentgen.ReportContentGenerator;
import org.jisel.generators.contentgen.SealedAbstractContentGenerator;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public sealed abstract class SealedAbstractSourceFileGenerator implements StringGenerator permits InterfaceSourceFileGenerator {

    protected final SealedAbstractContentGenerator interfaceContentGenerator;
    protected final SealedAbstractContentGenerator finalClassContentGenerator;
    protected final SealedAbstractContentGenerator reportContentGenerator;

    /**
     * SealedInterfaceSourceFileGenerator constructor. Creates needed instances of {@link InterfaceContentGenerator},
     * {@link FinalClassContentGenerator} and {@link ReportContentGenerator}
     */
    protected SealedAbstractSourceFileGenerator() {
        this.interfaceContentGenerator = new InterfaceContentGenerator();
        this.finalClassContentGenerator = new FinalClassContentGenerator();
        this.reportContentGenerator = new ReportContentGenerator();
    }

    public abstract List<String> createSourceFiles(ProcessingEnvironment processingEnvironment,
                                                   Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                   Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                   Map<Element, Boolean> unSealValueByLargeInterface) throws IOException;

    protected String createInterfaceSourceFile(ProcessingEnvironment processingEnvironment,
                                               Element largeInterfaceElement,
                                               boolean unSeal,
                                               Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                               Map<String, List<String>> sealedInterfacesPermitsMap,
                                               String generatedSealedInterfaceName) throws IOException {
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent() ? packageNameOpt.get() + DOT + generatedSealedInterfaceName : generatedSealedInterfaceName;
        try {
            // TODO IMPORTANT CHANGE HERE TO FIRST RUN THE FILE CREATION WITH UNSEAL = FALSE
            // AND THEN RUN THE SAME WITH UNSEAL = TRUE ???
            var fileObject = processingEnvironment.getFiler().createSourceFile(qualifiedName);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(interfaceContentGenerator.generateContent(processingEnvironment, largeInterfaceElement, unSeal, sealedInterfacesToGenerateMap, sealedInterfacesPermitsMap));
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
                out.println(finalClassContentGenerator.generateContent(processingEnvironment, largeInterfaceElement, false, Map.of(), sealedInterfacesPermitsMap));
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
            var fileObject = processingEnvironment.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageNameOpt.isPresent() ? packageNameOpt.get() : EMPTY_STRING, UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + JISEL_REPORT_SUFFIX);
            try (var out = new PrintWriter(fileObject.openWriter())) {
                out.println(reportContentGenerator.generateContent(processingEnvironment, largeInterfaceElement, unSeal, sealedInterfacesToGenerateMap, sealedInterfacesPermitsMap));
            }
        } catch (FilerException e) {
            // File was already generated - do nothing
        }
        return qualifiedName;
    }
}