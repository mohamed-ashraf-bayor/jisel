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
package org.jisel.generators.contentgen;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.codegen.AnnotationsGenerator;
import org.jisel.generators.codegen.DeclarationGenerator;
import org.jisel.generators.codegen.ExtendsGenerator;
import org.jisel.generators.codegen.MethodsGenerator;
import org.jisel.generators.codegen.PermitsGenerator;
import org.jisel.generators.codegen.impl.InterfaceAnnotationsGenerator;
import org.jisel.generators.codegen.impl.InterfaceDeclarationGenerator;
import org.jisel.generators.codegen.impl.InterfaceExtendsGenerator;
import org.jisel.generators.codegen.impl.InterfaceMethodsGenerator;
import org.jisel.generators.codegen.impl.SealedInterfacePermitsGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
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

    protected final AnnotationsGenerator annotationsGenerator;
    protected final ExtendsGenerator extendsGenerator;
    protected final PermitsGenerator permitsGenerator;
    protected final MethodsGenerator methodsGenerator;
    protected final DeclarationGenerator declarationGenerator;

    /**
     *
     */
    protected AbstractSealedContentGenerator() {
        this.annotationsGenerator = new InterfaceAnnotationsGenerator();
        this.extendsGenerator = new InterfaceExtendsGenerator();
        this.permitsGenerator = new SealedInterfacePermitsGenerator();
        this.methodsGenerator = new InterfaceMethodsGenerator();
        this.declarationGenerator = new InterfaceDeclarationGenerator();
    }

    /**
     * Generates content of the final class generated for the provided large interface
     *
     * @param processingEnvironment         {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param largeInterfaceElement         {@link Element} instance of the large interface being segregated
     * @param unSeal                        if 'true', indicates that additionally to generating the sealed interfaces hierarchy, also generate the classic (non-sealed) interfaces hierarchy.
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