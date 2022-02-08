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
package org.jisel.handlers;

import org.jisel.annotations.TopLevel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Handles all elements annotated with &#64;{@link TopLevel}
 */
public final class TopLevelHandler implements JiselAnnotationHandler {

    @Override
    public Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                        final Set<Element> allAnnotatedElements,
                                                        final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var statusReport = new HashMap<Element, String>();
        sealedInterfacesToGenerateByLargeInterface.forEach((largeInterfaceElement, sealedInterfacesToGenerate) -> {
            var allAnnotatedElementsToProcess = extractAnnotatedElementsFromLargeInterface(allAnnotatedElements, largeInterfaceElement);
            sealedInterfacesToGenerate.forEach((profile, methodsElement) -> methodsElement.removeIf(allAnnotatedElementsToProcess::contains));
            if (sealedInterfacesToGenerate.containsKey(largeInterfaceElement.getSimpleName().toString())) {
                // unique top-level parent detected, update only sealedInterfacesToGenerateByLargeInterface
                sealedInterfacesToGenerate.merge(
                        largeInterfaceElement.getSimpleName().toString(),
                        allAnnotatedElementsToProcess,
                        (oldValue, newValue) -> Stream.concat(oldValue.stream(), newValue.stream()).collect(toSet())
                );
            } else {
                // multiple top-level parent sealed interfaces, update both sealedInterfacesToGenerateByLargeInterface and sealedInterfacesPermitsByLargeInterface
                // add the toplevel parent interface with all methods annotated with toplevel
                sealedInterfacesToGenerate.put(largeInterfaceElement.getSimpleName().toString(), allAnnotatedElementsToProcess);
                // update the hierarchy
                var sealedInterfacesPermits = sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement);
                sealedInterfacesPermits.put(largeInterfaceElement.getSimpleName().toString(), extractTopLevelParentProfiles(sealedInterfacesPermits));
            }
            statusReport.put(largeInterfaceElement, EMPTY_STRING);
        });
        return statusReport;
    }

    private Set<Element> extractAnnotatedElementsFromLargeInterface(final Set<Element> allAnnotatedElements, final Element largeInterfaceElement) {
        return allAnnotatedElements.stream()
                .filter(element -> element.getEnclosingElement().equals(largeInterfaceElement))
                .collect(toSet());
    }

    private List<String> extractTopLevelParentProfiles(final Map<String, List<String>> sealedInterfacesPermitsMap) {
        var allLowerLevelsProfiles = sealedInterfacesPermitsMap.values().stream().flatMap(Collection::stream).collect(toSet());
        return sealedInterfacesPermitsMap.entrySet().stream()
                .map(Map.Entry::getKey)
                .filter(profile -> !allLowerLevelsProfiles.contains(profile))
                .toList();
    }
}