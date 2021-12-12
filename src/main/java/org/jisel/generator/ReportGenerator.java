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

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class ReportGenerator implements StringGenerator {

    public String generateReportForBloatedInterface(final Element largeInterfaceElement,
                                                    final Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                                    final Map<String, List<String>> sealedInterfacesPermitsMap) {
        var reportContent = new StringBuilder();
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + largeInterfaceElement.getSimpleName().toString()
                : largeInterfaceElement.getSimpleName().toString();
        reportContent.append(format("%s%n", qualifiedName));
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
        return reportContent.toString();
    }
}