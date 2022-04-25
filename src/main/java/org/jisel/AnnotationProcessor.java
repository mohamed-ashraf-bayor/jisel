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
package org.jisel;

import org.jisel.handlers.JiselAnnotationHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static org.jisel.generator.StringGenerator.ADD_TO;
import static org.jisel.generator.StringGenerator.AT_SIGN;
import static org.jisel.generator.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generator.StringGenerator.SEAL_FOR;
import static org.jisel.generator.StringGenerator.STATUS_REPORT_TITLE;
import static org.jisel.generator.StringGenerator.TOP_LEVEL;
import static org.jisel.generator.StringGenerator.TOP_LEVEL_REPORT_MSG;
import static org.jisel.generator.StringGenerator.WHITESPACE;

public sealed interface AnnotationProcessor permits JiselAnnotationProcessor {

    String ALL_ANNOTATED_SEALFOR_ELEMENTS = "allAnnotatedSealForElements";
    String ALL_ANNOTATED_TOPLEVEL_ELEMENTS = "allAnnotatedTopLevelElements";
    String ALL_ANNOTATED_ADDTO_ELEMENTS = "allAnnotatedAddToElements";

    void notifyReportDisplay(String reportText);

    default void populateAllAnnotatedElementsSets(Set<? extends TypeElement> annotations,
                                                  RoundEnvironment roundEnv,
                                                  Map<String, Set<Element>> allAnnotatedElementsMap) {
        for (var annotation : annotations) {
            if (annotation.getSimpleName().toString().contains(SEAL_FOR)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_SEALFOR_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(TOP_LEVEL)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_TOPLEVEL_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(ADD_TO)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_ADDTO_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
        }
    }

    default void processTopLevelAndSealForAnnotatedElements(ProcessingEnvironment processingEnv,
                                                            JiselAnnotationHandler topLevelHandler,
                                                            JiselAnnotationHandler sealForHandler,
                                                            Map<String, Set<Element>> allAnnotatedElementsMap,
                                                            Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                            Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        // process all interface methods annotated with @TopLevel
        var topLevelStatusReport = topLevelHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedElementsMap.get(ALL_ANNOTATED_TOPLEVEL_ELEMENTS)),
                sealedInterfacesToGenerateByLargeInterface,
                unmodifiableMap(sealedInterfacesPermitsByLargeInterface)
        );

        // process all interface methods annotated with @SealFor
        var sealForStatusReport = sealForHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedElementsMap.get(ALL_ANNOTATED_SEALFOR_ELEMENTS)),
                sealedInterfacesToGenerateByLargeInterface,
                sealedInterfacesPermitsByLargeInterface
        );

        displayStatusReport(extractLargeInterfacesWithNoTopLevel(sealForStatusReport, topLevelStatusReport), SEAL_FOR, TOP_LEVEL);
    }

    default void processAddToAnnotatedElements(ProcessingEnvironment processingEnv,
                                               JiselAnnotationHandler addToHandler,
                                               Set<Element> allAnnotatedAddToElements,
                                               Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                               Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        // process all child classes or interfaces annotated with @AddTo
        var addToStatusReport = addToHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedAddToElements),
                unmodifiableMap(sealedInterfacesToGenerateByLargeInterface),
                sealedInterfacesPermitsByLargeInterface
        );
        displayStatusReport(addToStatusReport, ADD_TO);
    }

    private Map<Element, String> extractLargeInterfacesWithNoTopLevel(Map<Element, String> sealForStatusReport,
                                                                      Map<Element, String> topLevelStatusReport) {
        topLevelStatusReport.keySet().forEach(sealForStatusReport::remove);
        sealForStatusReport.replaceAll((key, value) -> TOP_LEVEL_REPORT_MSG);
        return sealForStatusReport;
    }

    private void displayStatusReport(Map<Element, String> statusReport, String... annotationsNames) {
        if (!statusReport.values().stream().collect(joining()).isBlank()) {
            var output = new StringBuilder(format(
                    "%n%s - %s%n",
                    STATUS_REPORT_TITLE,
                    stream(annotationsNames).map(name -> AT_SIGN + name).collect(joining(COMMA_SEPARATOR + WHITESPACE))
            ));
            statusReport.entrySet().forEach(mapEntry -> {
                if (!mapEntry.getValue().isBlank()) {
                    output.append(format("\t> %s: %s%n", mapEntry.getKey().toString(), mapEntry.getValue()));
                }
            });
            notifyReportDisplay(output.toString());
        }
    }
}