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

import org.jisel.annotations.UnSeal;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles all elements annotated with &#64;{@link UnSeal}
 */
public final class DetachHandler implements JiselAnnotationHandler {

    @Override
    public Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                        Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        var statusReport = new HashMap<Element, String>();
//        allAnnotatedElements.stream()
//                .filter(element -> ElementKind.INTERFACE.equals(element.getKind()))
//                .forEach(element -> {
//                            var annotationElementValues = element.getAnnotationMirrors().stream()
//                                    .filter(annotationMirror -> annotationMirror.toString().contains(ORG_JISEL_UNSEAL))
//                                    .toList().get(0) // @UnSeal not repeatable, so only 1 occurrence will be found
//                                    .getElementValues();
//                            // stores values of UnSeal parameters (true or false) in the statusReport map
//                            statusReport.put(element, annotationElementValues.isEmpty()
//                                    ? TRUE
//                                    : annotationElementValues.entrySet().stream()
//                                    .filter(entry -> entry.getKey().toString().equals(VALUE + OPENING_PARENTHESIS + CLOSING_PARENTHESIS))
//                                    .findFirst().get().getValue().toString());
//                        }
//                );
        return statusReport;
    }
}