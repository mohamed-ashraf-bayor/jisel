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
import static org.jisel.generators.StringGenerator.JISEL_REPORT_CHILDREN_HEADER;
import static org.jisel.generators.StringGenerator.JISEL_REPORT_GENERATED_SEALED_INTERFACES_HEADER;
import static org.jisel.generators.StringGenerator.JISEL_REPORT_GENERATED_UNSEALED_INTERFACES_HEADER;
import static org.jisel.generators.StringGenerator.NEW_LINE;
import static org.jisel.generators.StringGenerator.UNDERSCORE;
import static org.jisel.generators.StringGenerator.sealedInterfaceNameConvention;
import static org.jisel.generators.StringGenerator.unSealedInterfaceNameConvention;

/**
 * TODO jdoc...
 */
public abstract sealed class AbstractSealedReportContentGenerator extends AbstractSealedSourceContentGenerator permits ReportContentGenerator {

    /**
     * TODO jdoc...
     *
     * @param processingEnvironment
     */
    protected AbstractSealedReportContentGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    /**
     *
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateMap
     * @param sealedInterfacesPermitsMap
     * @return
     */
    protected String generateSealedInterfacesReportContent(Element largeInterfaceElement,
                                                           Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                           Map<String, List<String>> sealedInterfacesPermitsMap) {
        var reportContent = new StringBuilder();
        reportContent.append(format("%s%n", JISEL_REPORT_GENERATED_SEALED_INTERFACES_HEADER));
        sealedInterfacesToGenerateMap.entrySet().forEach(mapEntry -> {
            var sealedInterfaceName = sealedInterfaceNameConvention(mapEntry.getKey(), largeInterfaceElement);
            reportContent.append(format("\t%s%n", sealedInterfaceName));
            var sealedInterfaceChildrenOpt = Optional.ofNullable(sealedInterfacesPermitsMap.get(mapEntry.getKey()));
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

    /**
     *
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateMap
     * @param sealedInterfacesPermitsMap
     * @return
     */
    protected String generateUnSealedInterfacesReportContent(Element largeInterfaceElement,
                                                             Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                             Map<String, List<String>> sealedInterfacesPermitsMap) {
        var reportContent = new StringBuilder();
        reportContent.append(format("%s%n", JISEL_REPORT_GENERATED_UNSEALED_INTERFACES_HEADER));
        sealedInterfacesToGenerateMap.entrySet().forEach(mapEntry -> {
            var interfaceName = unSealedInterfaceNameConvention(mapEntry.getKey(), largeInterfaceElement);
            reportContent.append(format("\t%s%n", interfaceName));
            var interfaceChildrenOpt = Optional.ofNullable(sealedInterfacesPermitsMap.get(mapEntry.getKey()));
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