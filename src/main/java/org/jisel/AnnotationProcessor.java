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

import org.jisel.handlers.AbstractSealedAddToHandler;
import org.jisel.handlers.AbstractSealedDetachHandler;
import org.jisel.handlers.AbstractSealedSealForHandler;
import org.jisel.handlers.JiselAnnotationHandler;

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
import static org.jisel.generators.StringGenerator.ADD_TO;
import static org.jisel.generators.StringGenerator.AT_SIGN;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generators.StringGenerator.DETACH;
import static org.jisel.generators.StringGenerator.DETACH_ALL;
import static org.jisel.generators.StringGenerator.SEAL_FOR;
import static org.jisel.generators.StringGenerator.TOP_LEVEL;
import static org.jisel.generators.StringGenerator.TOP_LEVEL_REPORT_NOT_FOUND_MSG;
import static org.jisel.generators.StringGenerator.UNSEAL;
import static org.jisel.generators.StringGenerator.UNSEAL_REPORT_NO_TOPLEVEL_MSG;
import static org.jisel.generators.StringGenerator.WHITESPACE;

/**
 * Exposes contract to be fulfilled by Jisel annotation processor main class
 */
public sealed interface AnnotationProcessor permits JiselAnnotationProcessor {

    /**
     * String used as a Map key to store all {@link Element} instances annotated with &#64;{@link org.jisel.annotations.SealFor}
     */
    String ALL_ANNOTATED_SEALFOR_ELEMENTS = "allAnnotatedSealForElements";

    /**
     * String used as a Map key to store all {@link Element} instances annotated with &#64;{@link org.jisel.annotations.TopLevel}
     */
    String ALL_ANNOTATED_TOPLEVEL_ELEMENTS = "allAnnotatedTopLevelElements";

    /**
     * String used as a Map key to store all {@link Element} instances annotated with &#64;{@link org.jisel.annotations.AddTo}
     */
    String ALL_ANNOTATED_ADDTO_ELEMENTS = "allAnnotatedAddToElements";

    /**
     * String used as a Map key to store all {@link Element} instances annotated with &#64;{@link org.jisel.annotations.UnSeal}
     */
    String ALL_ANNOTATED_UNSEAL_ELEMENTS = "allAnnotatedUnSealElements";

    /**
     * String used as a Map key to store all {@link Element} instances annotated with &#64;{@link org.jisel.annotations.Detach}
     */
    String ALL_ANNOTATED_DETACH_ELEMENTS = "allAnnotatedDetachElements";

    /**
     * Title of the text report displayed in the logs during compilation.<br>
     * The report is displayed only when an unexpected scenario was encountered <br>
     * (ex: More than 1 top-level parent interfaces found, profile not existing,...)
     */
    String STATUS_REPORT_TITLE = "JISEL GENERATION REPORT";

    /**
     * Displayed only when a "severe" error occurred while a sealed interface file was being generated
     */
    String FILE_GENERATION_ERROR = "Error generating sealed interfaces";

    /**
     * Displayed as a header while listing the successfully generated files
     */
    String FILE_GENERATION_SUCCESS = "Successfully generated";

    /**
     * Displays the provided statusReport text information.<br>
     * Called once the processing of annotated elements completes
     *
     * @param statusReportText status report text info to be displayed by the caller
     */
    void notifyStatusReportDisplay(String statusReportText);

    /**
     * Populates the provided {@link Map} based on content of the provided {@link Set} of {@link TypeElement}s
     *
     * @param annotations             the annotation interfaces requested to be processed
     * @param roundEnv                environment for information about the current and prior round
     * @param allAnnotatedElementsMap {@link Map} to be populated with {@link Set}s of annotated {@link Element} instances
     */
    default void populateAllAnnotatedElementsSets(Set<? extends TypeElement> annotations,
                                                  RoundEnvironment roundEnv,
                                                  Map<String, Set<Element>> allAnnotatedElementsMap) {
        for (var annotation : annotations) {
            if (annotation.getSimpleName().toString().contains(TOP_LEVEL)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_TOPLEVEL_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(SEAL_FOR)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_SEALFOR_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(ADD_TO)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_ADDTO_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(UNSEAL)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_UNSEAL_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(DETACH)) {
                allAnnotatedElementsMap.get(ALL_ANNOTATED_DETACH_ELEMENTS).addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
        }
    }

    /**
     * Processes {@link Element} instances annotated with &#64;{@link org.jisel.annotations.TopLevel} and &#64;{{@link org.jisel.annotations.SealFor}
     * annotations and populates the provided {@link Map}s with the collected information
     *
     * @param topLevelHandler                            {@link org.jisel.handlers.impl.TopLevelHandler} instance needed to
     *                                                   process elements annotated with &#64;{@link org.jisel.annotations.TopLevel}
     * @param sealForHandler                             {@link org.jisel.handlers.impl.SealForHandler} instance needed to
     *                                                   process elements annotated with &#64;{@link org.jisel.annotations.SealFor}
     * @param allAnnotatedElementsMap                    {@link Map} containing grouped {@link Element} instances annotated with
     *                                                   &#64;{@link org.jisel.annotations.TopLevel} and
     *                                                   &#64;{{@link org.jisel.annotations.SealFor}
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface    {@link Map} containing information about the subtypes permitted by
     *                                                   each one of the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a List of profiles names as the value.
     */
    default void processTopLevelAndSealForAnnotatedElements(JiselAnnotationHandler topLevelHandler,
                                                            AbstractSealedSealForHandler sealForHandler,
                                                            Map<String, Set<Element>> allAnnotatedElementsMap,
                                                            Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                            Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        // process all interface methods annotated with @TopLevel
        var topLevelStatusReport = topLevelHandler.handleAnnotatedElements(
                unmodifiableSet(allAnnotatedElementsMap.get(ALL_ANNOTATED_TOPLEVEL_ELEMENTS)),
                sealedInterfacesToGenerateByLargeInterface,
                Map.of()
        );
        // process all interface methods annotated with @SealFor
        var sealForStatusReport = sealForHandler.handleAnnotatedElements(
                unmodifiableSet(allAnnotatedElementsMap.get(ALL_ANNOTATED_SEALFOR_ELEMENTS)),
                sealedInterfacesToGenerateByLargeInterface,
                sealedInterfacesPermitsByLargeInterface
        );
        displayStatusReport(extractLargeInterfacesWithNoTopLevel(sealForStatusReport, topLevelStatusReport), SEAL_FOR, TOP_LEVEL);
    }

    /**
     * Processes {@link Element} instances annotated with &#64;{@link org.jisel.annotations.UnSeal} and populates the
     * provided {@link Map}s with the collected information
     *
     * @param unSealHandler                              {@link org.jisel.handlers.impl.UnSealHandler} instance needed to
     *                                                   process elements annotated with &#64;{@link org.jisel.annotations.UnSeal}
     * @param allAnnotatedUnSealElements                 {@link Set} of {@link Element} instances annotated with &#64;{@link org.jisel.annotations.UnSeal}
     * @param unSealValueByLargeInterface                {@link Map} storing 'unSeal' boolean value for each large interface
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     */
    default void processUnSealAnnotatedElements(JiselAnnotationHandler unSealHandler,
                                                Set<Element> allAnnotatedUnSealElements,
                                                Map<Element, Boolean> unSealValueByLargeInterface,
                                                Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        var unSealStatusReport = unSealHandler.handleAnnotatedElements(
                unmodifiableSet(allAnnotatedUnSealElements),
                Map.of(),
                Map.of()
        );
        unSealStatusReport.forEach(((element, unSealValueString) -> unSealValueByLargeInterface.put(element, Boolean.valueOf(unSealValueString))));
        // report any interfaces tagged with @UnSeal but not containing methods tagged with @TopLevel
        displayStatusReport(extractUnSealedInterfacesWithNoTopLevel(unSealStatusReport, sealedInterfacesToGenerateByLargeInterface), UNSEAL);
    }

    /**
     * Processes {@link Element} instances annotated with &#64;{@link org.jisel.annotations.Detach} and &#64;{@link org.jisel.annotations.DetachAll}
     * and populates the provided {@link Map}s with the collected information
     *
     * @param detachHandler                                {@link org.jisel.handlers.impl.DetachHandler} instance needed to
     *                                                     process elements annotated with &#64;{@link org.jisel.annotations.Detach}
     *                                                     and &#64;{@link org.jisel.annotations.DetachAll}
     * @param allAnnotatedDetachElements                   {@link Set} of {@link Element} instances annotated with &#64;{@link org.jisel.annotations.Detach}
     *                                                     and &#64;{@link org.jisel.annotations.DetachAll}
     * @param sealedInterfacesToGenerateByLargeInterface   {@link Map} containing information about the sealed interfaces to be generated.
     *                                                     To be populated and/or modified if needed. The key represents the Element instance of
     *                                                     each one of the large interfaces to be segregated, while the associated value is
     *                                                     a Map of profile name as the key and a Set of Element instances as the value.
     *                                                     The Element instances represent each one of the abstract methods to be
     *                                                     added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface      {@link Map} containing information about the subtypes permitted by
     *                                                     each one of the sealed interfaces to be generated.
     *                                                     To be populated and/or modified if needed. The key represents the Element instance of
     *                                                     each one of the large interfaces to be segregated, while the associated value is
     *                                                     a Map of profile name as the key and a List of profiles names as the value.
     * @param detachedInterfacesToGenerateByLargeInterface {@link Map} containing information about the detached interfaces to
     *                                                     be generated for each large interface
     */
    default void processDetachAnnotatedElements(AbstractSealedDetachHandler detachHandler,
                                                Set<Element> allAnnotatedDetachElements,
                                                Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface) {
        var detachStatusReport = detachHandler.handleDetachAnnotatedElements(
                unmodifiableSet(allAnnotatedDetachElements),
                unmodifiableMap(sealedInterfacesToGenerateByLargeInterface),
                unmodifiableMap(sealedInterfacesPermitsByLargeInterface),
                detachedInterfacesToGenerateByLargeInterface
        );
        displayStatusReport(detachStatusReport, DETACH, DETACH_ALL);
    }

    /**
     * Processes {@link Element} instances annotated with &#64;{@link org.jisel.annotations.AddTo} and populates the
     * provided {@link Map}s with the collected information
     *
     * @param addToHandler                               {@link org.jisel.handlers.impl.AddToHandler} instance needed to
     *                                                   process elements annotated with &#64;{@link org.jisel.annotations.AddTo}
     * @param allAnnotatedAddToElements                  {@link Set} of {@link Element} instances annotated with &#64;{@link org.jisel.annotations.AddTo}
     * @param sealedInterfacesToGenerateByLargeInterface {@link Map} containing information about the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a Set of Element instances as the value.
     *                                                   The Element instances represent each one of the abstract methods to be
     *                                                   added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface    {@link Map} containing information about the subtypes permitted by
     *                                                   each one of the sealed interfaces to be generated.
     *                                                   To be populated and/or modified if needed. The key represents the Element instance of
     *                                                   each one of the large interfaces to be segregated, while the associated value is
     *                                                   a Map of profile name as the key and a List of profiles names as the value.
     */
    default void processAddToAnnotatedElements(AbstractSealedAddToHandler addToHandler,
                                               Set<Element> allAnnotatedAddToElements,
                                               Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                               Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        // process all child classes or interfaces annotated with @AddTo
        var addToStatusReport = addToHandler.handleAnnotatedElements(
                unmodifiableSet(allAnnotatedAddToElements),
                unmodifiableMap(sealedInterfacesToGenerateByLargeInterface),
                sealedInterfacesPermitsByLargeInterface
        );
        displayStatusReport(addToStatusReport, ADD_TO);
    }

    private Map<Element, String> extractLargeInterfacesWithNoTopLevel(Map<Element, String> sealForStatusReport,
                                                                      Map<Element, String> topLevelStatusReport) {
        topLevelStatusReport.keySet().forEach(sealForStatusReport::remove);
        sealForStatusReport.replaceAll((key, value) -> TOP_LEVEL_REPORT_NOT_FOUND_MSG);
        return sealForStatusReport;
    }

    private Map<Element, String> extractUnSealedInterfacesWithNoTopLevel(Map<Element, String> unSealStatusReport,
                                                                         Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        sealedInterfacesToGenerateByLargeInterface.keySet().forEach(unSealStatusReport::remove);
        unSealStatusReport.replaceAll((key, value) -> UNSEAL_REPORT_NO_TOPLEVEL_MSG);
        return unSealStatusReport;
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
            notifyStatusReportDisplay(output.toString());
        }
    }
}