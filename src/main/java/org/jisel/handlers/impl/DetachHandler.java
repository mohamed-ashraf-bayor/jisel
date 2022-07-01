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

import org.jisel.annotations.Detach;
import org.jisel.handlers.AbstractSealedDetachHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jisel.generators.StringGenerator.DETACH_METHODS;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_ALL;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACHS;
import static org.jisel.handlers.JiselAnnotationHandler.findAllAbstractMethodsForProfile;

/**
 * Handles all elements annotated with &#64;{@link Detach}
 */
public final class DetachHandler extends AbstractSealedDetachHandler {

    public DetachHandler(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public Map<Element, String> handleDetachAnnotatedElements(Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                        Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface) {
        var statusReport = new HashMap<Element, String>();
        allAnnotatedElements.stream()
                .filter(element -> ElementKind.INTERFACE.equals(element.getKind()))
                .forEach(largeInterfaceElement -> {
                    // handle @DetachAll annotation
                    statusReport.put(
                            largeInterfaceElement,
                            handleDetachAllAnnotation(
                                    detachedInterfacesToGenerateByLargeInterface,
                                    largeInterfaceElement,
                                    sealedInterfacesToGenerateByLargeInterface
                            )
                    );
                    // handle @Detach.Detachs and @Detach annotations
                    var detachsAnnotationMirrorOpt = largeInterfaceElement.getAnnotationMirrors().stream()
                            .filter(annotationMirror -> annotationMirror.toString().contains(ORG_JISEL_DETACHS))
                            .findFirst();
                    // detachsAnnotationMirrorOpt sample value:
                    // Optional[@org.jisel.annotations.Detach.Detachs({@org.jisel.annotations.Detach(profile="(toplevel)" ...), @org.jisel.annotations.Detach(profile="PRo1") ...})]
                    detachsAnnotationMirrorOpt.ifPresentOrElse(
                            // consumer triggered when @Detachs present (multiple @Detach)
                            detachsAnnotationMirror -> statusReport.merge(
                                    largeInterfaceElement,
                                    handleMultipleDetachAnnotations(
                                            detachedInterfacesToGenerateByLargeInterface,
                                            largeInterfaceElement,
                                            detachsAnnotationMirror,
                                            sealedInterfacesToGenerateByLargeInterface
                                    ),
                                    String::concat
                            ),
                            // runnable triggered when a single @Detach was used
                            () -> statusReport.merge(
                                    largeInterfaceElement,
                                    handleSingleDetachAnnotation(
                                            detachedInterfacesToGenerateByLargeInterface,
                                            largeInterfaceElement,
                                            sealedInterfacesToGenerateByLargeInterface
                                    ),
                                    String::concat
                            )
                    );
                    if (detachedInterfacesToGenerateByLargeInterface.containsKey(largeInterfaceElement)) {
                        // for each detached interface, get the corresponding abstract methods (including the parent profiles methods)
                        detachedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).keySet().stream()
                                .filter(detachedProfile -> !JISEL_KEYWORD_ALL.equals(detachedProfile))
                                .forEach(detachedProfile -> detachedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(detachedProfile).put(
                                        DETACH_METHODS,
                                        findAllAbstractMethodsForProfile(
                                                detachedProfile,
                                                sealedInterfacesToGenerateByLargeInterface,
                                                sealedInterfacesPermitsByLargeInterface,
                                                largeInterfaceElement
                                        )
                                ));
                    }
                });
        return statusReport;
    }
}