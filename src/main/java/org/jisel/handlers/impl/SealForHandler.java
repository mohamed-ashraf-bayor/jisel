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
package org.jisel.handlers.impl;

import org.jisel.annotations.SealFor;
import org.jisel.handlers.AbstractSealedSealForHandler;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;

/**
 * Handles all elements annotated with &#64;{@link SealFor}
 */
public final class SealForHandler extends AbstractSealedSealForHandler {

    @Override
    public Map<Element, String> handleAnnotatedElements(Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var statusReport = new HashMap<Element, String>();
        var allAnnotatedElementsToProcess = allAnnotatedElements.stream()
                .filter(element -> ElementKind.METHOD.equals(element.getKind()))
                .filter(element -> ElementKind.INTERFACE.equals(element.getEnclosingElement().getKind()))
                // only where @TopLevel is used
                .filter(element -> sealedInterfacesToGenerateByLargeInterface.containsKey(element.getEnclosingElement()))
                // only if the same method is NOT also annotated with @TopLevel
                .filter(element -> !sealedInterfacesToGenerateByLargeInterface.get(element.getEnclosingElement()).get(element.getEnclosingElement().getSimpleName().toString()).contains(element))
                .collect(toSet());
        // statusReport - only add the name of the processed large interfaces with no description
        allAnnotatedElementsToProcess.forEach(element -> statusReport.put(element.getEnclosingElement(), EMPTY_STRING));
        annotationInfoCollectionHandler.populateSealedInterfacesMap(allAnnotatedElementsToProcess, sealedInterfacesToGenerateByLargeInterface);
        parentChildInheritanceHandler.buildInheritanceRelations(sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        return statusReport;
    }
}