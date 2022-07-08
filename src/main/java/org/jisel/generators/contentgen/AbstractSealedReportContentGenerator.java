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

import org.jisel.generators.contentgen.impl.ReportContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.jisel.generators.StringGenerator.DOT;
import static org.jisel.generators.StringGenerator.FINAL_CLASS_SUFFIX;
import static org.jisel.generators.StringGenerator.NEW_LINE;
import static org.jisel.generators.StringGenerator.UNDERSCORE;
import static org.jisel.generators.StringGenerator.sealedInterfaceNameConvention;
import static org.jisel.generators.StringGenerator.unSealedInterfaceNameConvention;

/**
 * TODO jdoc...
 */
public abstract sealed class AbstractSealedReportContentGenerator extends AbstractSealedSourceContentGenerator permits ReportContentGenerator {

    /**
     * "Report.txt" suffix appended by the end of the generated report filename
     */
    public static final String REPORT_FILENAME_SUFFIX = "Report.txt";

    /**
     * Header displayed above the list of the generated sealed interfaces, in the Jisel Report file
     */
    private static final String GENERATED_SEALED_INTERFACES_HEADER = "Generated sealed interfaces:";

    /**
     * Header displayed above the list of the generated unsealed interfaces, in the Jisel Report file
     */
    private static final String GENERATED_UNSEALED_INTERFACES_HEADER = "Generated unsealed interfaces:";

    /**
     * Header displayed above the list of the generated detached interfaces, in the Jisel Report file
     */
    private static final String GENERATED_DETACHED_INTERFACES_HEADER = "Generated detached interfaces:";

    /**
     * Header displayed above the list of the sub-types of the generated sealed interfaces, in the Jisel Report file
     */
    private static final String CHILDREN_HEADER = "Children:";

    private static final String GENERATED_INTERFACE_NAME_DISPLAY_FORMAT = "\t%s%n";

    private static final String HEADER_TITLE_FORMAT = "%s%n";

    public static final String CHILDREN_HEADER_TITLE_FORMAT = "\t - %s%n";
    public static final String CHILD_INTERFACE_NAME_FORMAT = "\t\t%s%n";
    public static final String CHILDREN_NAMES_SEPARATOR = "%n\t\t";


    /**
     * TODO jdoc...
     *
     * @param processingEnvironment
     */
    protected AbstractSealedReportContentGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    /**
     * TODO ...
     *
     * @param largeInterfaceElement
     * @param unSeal
     * @param sealedInterfacesToGenerate
     * @param sealedInterfacesPermitsMap
     * @param generatedDetachedInterfaces
     * @return
     */
    public abstract String generateReportContent(Element largeInterfaceElement,
                                                 boolean unSeal,
                                                 Map<String, Set<Element>> sealedInterfacesToGenerate,
                                                 Map<String, List<String>> sealedInterfacesPermitsMap,
                                                 List<String> generatedDetachedInterfaces);

    /**
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateMap
     * @param sealedInterfacesPermitsMap
     * @return
     */
    protected String generateSealedInterfacesReportContent(Element largeInterfaceElement,
                                                           Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                           Map<String, List<String>> sealedInterfacesPermitsMap) {
        var reportContent = new StringBuilder();
        reportContent.append(format(HEADER_TITLE_FORMAT, GENERATED_SEALED_INTERFACES_HEADER));
        sealedInterfacesToGenerateMap.keySet().forEach(profile -> {
            var sealedInterfaceName = sealedInterfaceNameConvention(profile, largeInterfaceElement);
            reportContent.append(format(GENERATED_INTERFACE_NAME_DISPLAY_FORMAT, sealedInterfaceName));
            var sealedInterfaceChildrenOpt = Optional.ofNullable(sealedInterfacesPermitsMap.get(profile));
            if (sealedInterfaceChildrenOpt.isPresent() && !sealedInterfaceChildrenOpt.get().isEmpty()) {
                reportContent.append(format(CHILDREN_HEADER_TITLE_FORMAT, CHILDREN_HEADER));
                if (!sealedInterfaceChildrenOpt.get().isEmpty()) {
                    reportContent.append(format(
                            CHILD_INTERFACE_NAME_FORMAT,
                            sealedInterfaceChildrenOpt.get().stream()
                                    .map(childName -> sealedInterfaceNameConvention(childName, largeInterfaceElement))
                                    .collect(joining(format(CHILDREN_NAMES_SEPARATOR)))
                    ));
                }
            }
        });
        reportContent.append(NEW_LINE);
        return reportContent.toString();
    }

    /**
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateMap
     * @param sealedInterfacesPermitsMap
     * @return
     */
    protected String generateUnSealedInterfacesReportContent(Element largeInterfaceElement,
                                                             Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                             Map<String, List<String>> sealedInterfacesPermitsMap) {
        var reportContent = new StringBuilder();
        reportContent.append(format(HEADER_TITLE_FORMAT, GENERATED_UNSEALED_INTERFACES_HEADER));
        sealedInterfacesToGenerateMap.keySet().forEach(profile -> {
            var interfaceName = unSealedInterfaceNameConvention(profile, largeInterfaceElement);
            reportContent.append(format(GENERATED_INTERFACE_NAME_DISPLAY_FORMAT, interfaceName));
            var interfaceChildrenOpt = Optional.ofNullable(sealedInterfacesPermitsMap.get(profile));
            if (interfaceChildrenOpt.isPresent() && !interfaceChildrenOpt.get().isEmpty()) {
                var childrenListOutput = interfaceChildrenOpt.get().stream()
                        .filter(childName -> !childName.startsWith(UNDERSCORE) && !childName.endsWith(FINAL_CLASS_SUFFIX))
                        .filter(childName -> !childName.contains(DOT))
                        .toList();
                if (!childrenListOutput.isEmpty()) {
                    reportContent.append(format(CHILDREN_HEADER_TITLE_FORMAT, CHILDREN_HEADER));
                    reportContent.append(format(
                            CHILD_INTERFACE_NAME_FORMAT,
                            childrenListOutput.stream()
                                    .map(childName -> unSealedInterfaceNameConvention(childName, largeInterfaceElement))
                                    .collect(joining(format(CHILDREN_NAMES_SEPARATOR)))
                    ));
                }
            }
        });
        reportContent.append(NEW_LINE);
        return reportContent.toString();
    }

    protected String generateDetachedInterfacesReportContent(List<String> generatedDetachedInterfaces) {
        var reportContent = new StringBuilder();
        reportContent.append(format(HEADER_TITLE_FORMAT, GENERATED_DETACHED_INTERFACES_HEADER));
        generatedDetachedInterfaces.stream().forEach(qualifiedName -> reportContent.append(format(GENERATED_INTERFACE_NAME_DISPLAY_FORMAT, qualifiedName)));
        reportContent.append(NEW_LINE);
        return reportContent.toString();
    }

    @Override
    public String generateSourceContent(Element largeInterfaceElement,
                                        boolean unSeal,
                                        Map.Entry<String, Set<Element>> sealedInterfaceToGenerate,
                                        Map<String, List<String>> sealedInterfacesPermitsMap) {
        return generateReportContent(largeInterfaceElement, unSeal, Map.ofEntries(sealedInterfaceToGenerate), sealedInterfacesPermitsMap, List.of());
    }
}