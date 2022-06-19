package org.jisel.generators.contentgen;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.codegen.CodeGenerator;
import org.jisel.generators.codegen.ExtendsGenerator;
import org.jisel.generators.codegen.InterfaceExtendsGenerator;
import org.jisel.generators.codegen.InterfaceMethodsGenerator;
import org.jisel.generators.codegen.JavaxGeneratedGenerator;
import org.jisel.generators.codegen.MethodsGenerator;
import org.jisel.generators.codegen.PermitsGenerator;
import org.jisel.generators.codegen.SealedInterfacePermitsGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

// TODO jdoc

/**
 * Generates content of the final class generated for the provided large interface
 * ... ...
 */
public abstract sealed class AbstractSealedContentGenerator
        implements StringGenerator
        permits FinalClassContentGenerator, ReportContentGenerator, InterfaceContentGenerator {

    protected final CodeGenerator javaxGeneratedGenerator;
    protected final ExtendsGenerator extendsGenerator;
    protected final PermitsGenerator permitsGenerator;
    protected final MethodsGenerator methodsGenerator;

    /**
     *
     */
    protected AbstractSealedContentGenerator() {
        this.javaxGeneratedGenerator = new JavaxGeneratedGenerator();
        this.extendsGenerator = new InterfaceExtendsGenerator();
        this.permitsGenerator = new SealedInterfacePermitsGenerator();
        this.methodsGenerator = new InterfaceMethodsGenerator();
    }

    /**
     * Generates content of the final class generated for the provided large interface
     *
     * @param processingEnvironment         {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param largeInterfaceElement         {@link Element} instance of the large interface being segregated
     * @param unSeal                        if true, indicates that additionally to generating the sealed interfaces hierarchy, also generate the classic (non-sealed) interfaces hierarchy.
     *                                      If 'false', only generate the sealed interfaces' hierarchy
     * @param sealedInterfacesToGenerateMap {@link Map} instance containing information about the sealed interface to be generated
     * @param sealedInterfacesPermitsMap    {@link Map} containing information about the subtypes permitted by each one of the sealed interfaces to be generated
     * @return the requested class or interface string content
     */
    public abstract String generateContent(ProcessingEnvironment processingEnvironment,
                                           Element largeInterfaceElement,
                                           boolean unSeal,
                                           Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                           Map<String, List<String>> sealedInterfacesPermitsMap);

    protected String generateSealedInterfacesReport(Element largeInterfaceElement,
                                                    Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                    Map<String, List<String>> sealedInterfacesPermitsMap) {
        var reportContent = new StringBuilder();
        reportContent.append(format("%s%n", JISEL_REPORT_CREATED_SEALED_INTERFACES_HEADER));
        sealedInterfacesToGenerateMap.entrySet().forEach(entrySet -> {
            var sealedInterfaceName = sealedInterfaceNameConvention(entrySet.getKey(), largeInterfaceElement);
            reportContent.append(format("\t%s%n", sealedInterfaceName));
            var sealedInterfaceChildrenOpt = Optional.ofNullable(sealedInterfacesPermitsMap.get(entrySet.getKey()));
            if (sealedInterfaceChildrenOpt.isPresent() && !sealedInterfaceChildrenOpt.get().isEmpty()) {
                reportContent.append(format("\t - %s%n", JISEL_REPORT_CHILDREN_HEADER));
                if (!sealedInterfaceChildrenOpt.get().isEmpty()) {
                    reportContent.append(format(
                            "\t\t%s%n",
                            sealedInterfaceChildrenOpt.get().stream()
                                    .map(childName -> sealedInterfaceNameConvention(childName, largeInterfaceElement))
                                    .collect(joining(format("%n\t\t")))
                    ));
                }
            }
        });
        reportContent.append(NEW_LINE);
        return reportContent.toString();
    }

    protected String generateUnSealedInterfacesReport(Element largeInterfaceElement,
                                                      Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                      Map<String, List<String>> sealedInterfacesPermitsMap) {
        var reportContent = new StringBuilder();
        reportContent.append(format("%s%n", JISEL_REPORT_CREATED_UNSEALED_INTERFACES_HEADER));
        sealedInterfacesToGenerateMap.entrySet().forEach(entrySet -> {
            var interfaceName = unSealedInterfaceNameConvention(entrySet.getKey(), largeInterfaceElement);
            reportContent.append(format("\t%s%n", interfaceName));
            var interfaceChildrenOpt = Optional.ofNullable(sealedInterfacesPermitsMap.get(entrySet.getKey()));
            if (interfaceChildrenOpt.isPresent() && !interfaceChildrenOpt.get().isEmpty()) {
                var childrenListOutput = interfaceChildrenOpt.get().stream()
                        .filter(childName -> !childName.startsWith(UNDERSCORE) && !childName.endsWith(FINAL_CLASS_SUFFIX))
                        .filter(childName -> !childName.contains(DOT))
                        .toList();
                if (!childrenListOutput.isEmpty()) {
                    reportContent.append(format("\t - %s%n", JISEL_REPORT_CHILDREN_HEADER));
                    reportContent.append(format(
                            "\t\t%s%n",
                            childrenListOutput.stream()
                                    .map(childName -> unSealedInterfaceNameConvention(childName, largeInterfaceElement))
                                    .collect(joining(format("%n\t\t")))
                    ));
                }
            }
        });
        reportContent.append(NEW_LINE);
        return reportContent.toString();
    }
}