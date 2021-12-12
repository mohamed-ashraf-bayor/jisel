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
package org.jisel;

import com.google.auto.service.AutoService;
import org.jisel.generator.SealedInterfaceSourceFileGenerator;
import org.jisel.generator.StringGenerator;
import org.jisel.handlers.AddToProfileHandler;
import org.jisel.handlers.JiselAnnotationHandler;
import org.jisel.handlers.SealForProfileHandler;

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
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TO_PROFILE;
import static org.jisel.generator.StringGenerator.ORG_JISEL_ADD_TO_PROFILEZ;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR_PROFILE;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR_PROFILES;
import static org.jisel.generator.StringGenerator.ORG_JISEL_SEAL_FOR_PROFILEZ;

@SupportedAnnotationTypes({ORG_JISEL_SEAL_FOR_PROFILE, ORG_JISEL_SEAL_FOR_PROFILES, ORG_JISEL_SEAL_FOR_PROFILEZ,
        ORG_JISEL_ADD_TO_PROFILE, ORG_JISEL_ADD_TO_PROFILEZ})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JiselAnnotationProcessor extends AbstractProcessor implements StringGenerator {

    private final Logger log = Logger.getLogger(JiselAnnotationProcessor.class.getName());

    private final JiselAnnotationHandler sealForProfileHandler;

    private final JiselAnnotationHandler addToProfileHandler;

    private final SealedInterfaceSourceFileGenerator sealedInterfaceSourceFileGenerator;

    public JiselAnnotationProcessor() {
        this.sealForProfileHandler = new SealForProfileHandler();
        this.addToProfileHandler = new AddToProfileHandler();
        this.sealedInterfaceSourceFileGenerator = new SealedInterfaceSourceFileGenerator();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        var allAnnotatedSealForProfileElements = new HashSet<Element>();
        var allAnnotatedAddToProfileElements = new HashSet<Element>();
        var sealedInterfacesToGenerateByLargeInterface = new HashMap<Element, Map<String, Set<Element>>>();
        var sealedInterfacesPermitsByLargeInterface = new HashMap<Element, Map<String, List<String>>>();

        populateAllAnnotatedElementsSets(annotations, roundEnv, allAnnotatedSealForProfileElements, allAnnotatedAddToProfileElements);

        // process all interface methods annotated with @SealForProfile
        var statusReport = sealForProfileHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedSealForProfileElements),
                sealedInterfacesToGenerateByLargeInterface,
                sealedInterfacesPermitsByLargeInterface
        );
        displayStatusReport(statusReport, SEAL_FOR_PROFILE);

        // process all child classes or interfaces annotated with @AddToProfile
        statusReport = addToProfileHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedAddToProfileElements),
                unmodifiableMap(sealedInterfacesToGenerateByLargeInterface),
                sealedInterfacesPermitsByLargeInterface
        );
        displayStatusReport(statusReport, ADD_TO_PROFILE);

        try {
            var generatedFiles = sealedInterfaceSourceFileGenerator.createSourceFiles(processingEnv, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
            if (!generatedFiles.isEmpty()) {
                log.info(() -> format("%s:%n%s", FILE_GENERATION_SUCCESS, generatedFiles.stream().collect(joining(format("%n")))));
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, FILE_GENERATION_ERROR, e);
        }

        return true;
    }

    private void populateAllAnnotatedElementsSets(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv,
                                                  final Set<Element> allAnnotatedSealForProfileElements, final Set<Element> allAnnotatedAddToProfileElements) {
        for (var annotation : annotations) {
            if (annotation.getSimpleName().toString().contains(SEAL_FOR_PROFILE)) {
                allAnnotatedSealForProfileElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(ADD_TO_PROFILE)) {
                allAnnotatedAddToProfileElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
        }
    }

    private void displayStatusReport(final Map<Element, String> statusReport, final String annotationName) {
        if (!statusReport.values().stream().collect(joining()).isBlank()) {
            var output = new StringBuilder(format("%n%s - @%s%n", STATUS_REPORT_TITLE, annotationName));
            statusReport.entrySet().forEach(mapEntry -> {
                if (!mapEntry.getValue().isBlank()) {
                    output.append(format("\t> %s: %s%n", mapEntry.getKey().getSimpleName().toString(), mapEntry.getValue()));
                }
            });
            log.warning(output::toString);
        }
    }
}