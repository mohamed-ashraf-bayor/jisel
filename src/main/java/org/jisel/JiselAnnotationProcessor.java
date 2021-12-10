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
import org.jisel.handlers.AddToProfileHandler;
import org.jisel.handlers.JiselAnnotationHandler;
import org.jisel.handlers.SealForProfileHandler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
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

@SupportedAnnotationTypes({"org.jisel.SealForProfile", "org.jisel.SealForProfiles", "org.jisel.SealForProfile.SealForProfilez",
        "org.jisel.AddToProfile", "org.jisel.AddToProfiles", "org.jisel.AddToProfile.AddToProfilez"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JiselAnnotationProcessor extends AbstractProcessor {

    private final Logger log = Logger.getLogger(JiselAnnotationProcessor.class.getName());

    private static final String SEAL_FOR_PROFILE = "SealForProfile";
    private static final String ADD_TO_PROFILE = "AddToProfile";

    private static final String STATUS_REPORT_TITLE = "JISEL GENERATION REPORT";

    private JiselAnnotationHandler sealForProfileHandler;

    private JiselAnnotationHandler addToProfileHandler;

    public JiselAnnotationProcessor() {
        this.sealForProfileHandler = new SealForProfileHandler();
        this.addToProfileHandler = new AddToProfileHandler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        var allAnnotatedSealForProfileElements = new HashSet<Element>();
        var allAnnotatedAddToProfileElements = new HashSet<Element>();
        var sealedInterfacesToGenerateByBloatedInterface = new HashMap<Element, Map<String, Set<Element>>>();
        var sealedInterfacesPermitsByBloatedInterface = new HashMap<Element, Map<String, List<String>>>();

        populateAllAnnotatedElementsSets(annotations, roundEnv, allAnnotatedSealForProfileElements, allAnnotatedAddToProfileElements);

        // process all interface methods annotated with @SealForProfile
        var statusReport = sealForProfileHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedSealForProfileElements),
                sealedInterfacesToGenerateByBloatedInterface,
                sealedInterfacesPermitsByBloatedInterface
        );
        displayStatusReport(statusReport, SealForProfile.class);

        // process all child classes or interfaces annotated with @AddToProfile
        statusReport = addToProfileHandler.handleAnnotatedElements(
                processingEnv,
                unmodifiableSet(allAnnotatedAddToProfileElements),
                unmodifiableMap(sealedInterfacesToGenerateByBloatedInterface),
                sealedInterfacesPermitsByBloatedInterface
        );
        displayStatusReport(statusReport, AddToProfile.class);

        try {
            new SealedInterfaceSourceFileGenerator(processingEnv).createSourceFiles(sealedInterfacesToGenerateByBloatedInterface, sealedInterfacesPermitsByBloatedInterface);
        } catch (IOException e) {
            log.log(Level.SEVERE, format("Error generating sealed interfaces", e));
        }

        System.out.println(" \n\n@@@@@@@@@@@@@ sealedInterfacesToGenerateByBloatedInterface: " + sealedInterfacesToGenerateByBloatedInterface);
        System.out.println("\n~~~~~~~~~~ ~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~ \n sealedInterfacesPermitsByBloatedInterface" + sealedInterfacesPermitsByBloatedInterface);

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

    private void displayStatusReport(final Map<Element, String> statusReport, final Class annotation) {
        if (!statusReport.values().stream().collect(joining()).isBlank()) {
            var output = new StringBuilder(format("%n%s - @%s(s)%n", STATUS_REPORT_TITLE, annotation.getSimpleName()));
            statusReport.entrySet().forEach(mapEntry -> {
                if (!mapEntry.getValue().isBlank()) {
                    output.append(format("\t> %s: %s%n", mapEntry.getKey().getSimpleName().toString(), mapEntry.getValue()));
                }
            });
            log.warning(output.toString());
        }
    }
}