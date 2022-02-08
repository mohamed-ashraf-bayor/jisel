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

import com.google.auto.service.AutoService;
import org.jisel.annotations.AddTo;
import org.jisel.annotations.AddToProfile;
import org.jisel.annotations.AddToProfiles;
import org.jisel.annotations.SealFor;
import org.jisel.annotations.SealForProfile;
import org.jisel.annotations.SealForProfiles;
import org.jisel.annotations.TopLevel;
import org.jisel.generator.SealedInterfaceSourceFileGenerator;
import org.jisel.generator.StringGenerator;
import org.jisel.handlers.AddToHandler;
import org.jisel.handlers.JiselAnnotationHandler;
import org.jisel.handlers.SealForHandler;
import org.jisel.handlers.TopLevelHandler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TO;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TOS;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TO_PROFILE;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TO_PROFILES;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TO_PROFILEZ;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TO_PROFILEZZ;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FORS;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR_PROFILE;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR_PROFILES;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR_PROFILEZ;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR_PROFILEZZ;
import static org.jisel.generator.StringGenerator.ORG_JISEL_TOP_LEVEL;

/**
 * Jisel annotation processor class. Picks up and processes all elements annotated with &#64;{@link SealFor},
 * &#64;{@link AddTo}, &#64;{@link TopLevel}, &#64;{@link SealForProfile}, &#64;{@link SealForProfiles},
 * &#64;{@link AddToProfile} and &#64;{@link AddToProfiles}<br>
 */
@SupportedAnnotationTypes({ORG_JISEL_SEAL_FOR_PROFILE, ORG_JISEL_SEAL_FOR_PROFILES, ORG_JISEL_SEAL_FOR_PROFILEZ,
        ORG_JISEL_SEAL_FOR_PROFILEZZ, ORG_JISEL_ADD_TO_PROFILE, ORG_JISEL_ADD_TO_PROFILES, ORG_JISEL_ADD_TO_PROFILEZ,
        ORG_JISEL_ADD_TO_PROFILEZZ, ORG_JISEL_TOP_LEVEL, ORG_JISEL_ADD_TO, ORG_JISEL_SEAL_FOR, ORG_JISEL_SEAL_FORS,
        ORG_JISEL_ADD_TOS})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JiselAnnotationProcessor extends AbstractProcessor implements StringGenerator {

    private final Logger log = Logger.getLogger(JiselAnnotationProcessor.class.getName());

    private final JiselAnnotationHandler sealForHandler;

    private final JiselAnnotationHandler addToHandler;

    private final JiselAnnotationHandler topLevelHandler;

    private final SealedInterfaceSourceFileGenerator sealedInterfaceSourceFileGenerator;

    /**
     * JiselAnnotationProcessor constructor. Initializes needed instances of {@link SealForHandler}, {@link AddToHandler},
     * and {@link SealedInterfaceSourceFileGenerator}
     */
    public JiselAnnotationProcessor() {
        this.sealForHandler = new SealForHandler();
        this.addToHandler = new AddToHandler();
        this.topLevelHandler = new TopLevelHandler();
        this.sealedInterfaceSourceFileGenerator = new SealedInterfaceSourceFileGenerator();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        var allAnnotatedSealForElements = new HashSet<Element>();
        var allAnnotatedTopLevelElements = new HashSet<Element>();
        var allAnnotatedAddToElements = new HashSet<Element>();
        var sealedInterfacesToGenerateByLargeInterface = new HashMap<Element, Map<String, Set<Element>>>();
        var sealedInterfacesPermitsByLargeInterface = new HashMap<Element, Map<String, List<String>>>();

        populateAllAnnotatedElementsSets(annotations, roundEnv, allAnnotatedSealForElements, allAnnotatedTopLevelElements, allAnnotatedAddToElements);

        // process all interface methods annotated with @SealFor
        var sealForStatusReport = sealForHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedSealForElements),
                sealedInterfacesToGenerateByLargeInterface,
                sealedInterfacesPermitsByLargeInterface
        );

        // process all interface methods annotated with @TopLevel
        var topLevelStatusReport = topLevelHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedTopLevelElements),
                sealedInterfacesToGenerateByLargeInterface,
                sealedInterfacesPermitsByLargeInterface
        );

        displayStatusReport(mergeSealForAndTopLevelStatusReports(sealForStatusReport, topLevelStatusReport), SEAL_FOR, TOP_LEVEL);

        // process all child classes or interfaces annotated with @AddTo
        var addToStatusReport = addToHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedAddToElements),
                unmodifiableMap(sealedInterfacesToGenerateByLargeInterface),
                sealedInterfacesPermitsByLargeInterface
        );
        displayStatusReport(addToStatusReport, ADD_TO);

        try {
            var generatedFiles = sealedInterfaceSourceFileGenerator.createSourceFiles(
                    processingEnv,
                    sealedInterfacesToGenerateByLargeInterface,
                    sealedInterfacesPermitsByLargeInterface
            );
            if (!generatedFiles.isEmpty()) {
                log.info(() -> format("%s:%n%s", FILE_GENERATION_SUCCESS, generatedFiles.stream().collect(joining(format("%n")))));
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, FILE_GENERATION_ERROR, e);
        }

        return true;
    }

    private void populateAllAnnotatedElementsSets(final Set<? extends TypeElement> annotations,
                                                  final RoundEnvironment roundEnv,
                                                  final Set<Element> allAnnotatedSealForElements,
                                                  final Set<Element> allAnnotatedTopLevelElements,
                                                  final Set<Element> allAnnotatedAddToElements) {
        for (var annotation : annotations) {
            if (annotation.getSimpleName().toString().contains(SEAL_FOR)) {
                allAnnotatedSealForElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(TOP_LEVEL)) {
                allAnnotatedTopLevelElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(ADD_TO)) {
                allAnnotatedAddToElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
        }
    }

    private Map<Element, String> mergeSealForAndTopLevelStatusReports(final Map<Element, String> sealForStatusReport,
                                                                      final Map<Element, String> topLevelStatusReport) {
        topLevelStatusReport.forEach((largeInterfaceElement, reportText) -> sealForStatusReport.remove(largeInterfaceElement));
        return sealForStatusReport;
    }

    private void displayStatusReport(final Map<Element, String> statusReport, final String... annotationsNames) {
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
            log.warning(output::toString);
        }
    }
}