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
package org.jisel.generators.contentgen.impl;

import org.jisel.generators.contentgen.AbstractSealedReportContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.jisel.generators.StringGenerator.DOT;
import static org.jisel.generators.StringGenerator.generatePackageName;

/**
 * //TODO add classic interf generation parts in jdoc
 * Generates a Report file listing all generated sealed interfaces for the provided large interfaces.<br>
 * Sample report:<br>
 * com.bayor.jisel.annotation.client.hierarchicalinheritance.Sociable<br>
 * &#9;Created sealed interfaces:<br>
 * &#9;&#9;SealedActiveWorkerSociable<br>
 * &#9;&#9;- Children:<br>
 * &#9;&#9;&#9;_SociableFinalCass<br>
 * &#9;&#9;SealedWorkerSociable<br>
 * &#9;&#9;- Children:<br>
 * &#9;&#9;&#9;SealedActiveWorkerSociable<br>
 * &#9;&#9;&#9;com.bayor.jisel.annotation.client.hierarchicalinheritance.subclasses.StudentWorkerHybrid<br>
 * &#9;&#9;SealedStudentSociable<br>
 * &#9;&#9;- Children:<br>
 * &#9;&#9;&#9;com.bayor.jisel.annotation.client.hierarchicalinheritance.subclasses.StudentWorkerHybrid<br>
 * &#9;&#9;SealedSociable<br>
 * &#9;&#9;- Children:<br>
 * &#9;&#9;&#9;SealedWorkerSociable<br>
 * &#9;&#9;&#9;SealedStudentSociable<br>
 */
public final class ReportContentGenerator extends AbstractSealedReportContentGenerator {

    /**
     * TODO jdoc...
     *
     * @param processingEnvironment
     */
    public ReportContentGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public String generateReportContent(Element largeInterfaceElement,
                                        boolean unSeal,
                                        Map<String, Set<Element>> sealedInterfacesToGenerate,
                                        Map<String, List<String>> sealedInterfacesPermitsMap,
                                        List<String> generatedDetachedInterfaces) {
        var reportContent = new StringBuilder();
        var packageNameOpt = generatePackageName(largeInterfaceElement);
        var qualifiedName = packageNameOpt.isPresent()
                ? packageNameOpt.get() + DOT + largeInterfaceElement.getSimpleName().toString()
                : largeInterfaceElement.getSimpleName().toString();
        reportContent.append(format("%s%n%n", qualifiedName));
        reportContent.append(generateSealedInterfacesReportContent(largeInterfaceElement, sealedInterfacesToGenerate, sealedInterfacesPermitsMap));
        if (unSeal) {
            reportContent.append(generateUnSealedInterfacesReportContent(largeInterfaceElement, sealedInterfacesToGenerate, sealedInterfacesPermitsMap));
        }
        reportContent.append(generateDetachedInterfacesReportContent(generatedDetachedInterfaces));
        return reportContent.toString();
    }
}