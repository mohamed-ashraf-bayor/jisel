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
import org.jisel.annotations.Detach;
import org.jisel.annotations.DetachAll;
import org.jisel.annotations.SealFor;
import org.jisel.annotations.TopLevel;
import org.jisel.annotations.UnSeal;
import org.jisel.generators.filegen.impl.InterfaceSourceFileGenerator;
import org.jisel.handlers.AbstractSealedSealForHandler;
import org.jisel.handlers.JiselAnnotationHandler;
import org.jisel.handlers.impl.AddToHandler;
import org.jisel.handlers.impl.DetachHandler;
import org.jisel.handlers.impl.SealForHandler;
import org.jisel.handlers.impl.TopLevelHandler;
import org.jisel.handlers.impl.UnSealHandler;

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
import static java.util.stream.Collectors.joining;
import static org.jisel.generators.StringGenerator.NEW_LINE;
import static org.jisel.generators.StringGenerator.ORG_JISEL_ADD_TO;
import static org.jisel.generators.StringGenerator.ORG_JISEL_ADD_TOS;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACH;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACHALL;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACHS;
import static org.jisel.generators.StringGenerator.ORG_JISEL_SEAL_FOR;
import static org.jisel.generators.StringGenerator.ORG_JISEL_SEAL_FORS;
import static org.jisel.generators.StringGenerator.ORG_JISEL_TOP_LEVEL;
import static org.jisel.generators.StringGenerator.ORG_JISEL_UNSEAL;

/**
 * Jisel annotation processor class. Picks up and processes all elements annotated with &#64;{@link SealFor},
 * &#64;{@link AddTo}, &#64;{@link TopLevel}, &#64;{@link UnSeal}, &#64;{@link Detach} and &#64;{@link DetachAll}
 */
@SupportedAnnotationTypes({ORG_JISEL_TOP_LEVEL, ORG_JISEL_ADD_TO, ORG_JISEL_SEAL_FOR, ORG_JISEL_SEAL_FORS, ORG_JISEL_ADD_TOS,
        ORG_JISEL_UNSEAL, ORG_JISEL_DETACH, ORG_JISEL_DETACHALL, ORG_JISEL_DETACHS})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public final class JiselAnnotationProcessor extends AbstractProcessor implements AnnotationProcessor {

    private final Logger log = Logger.getLogger(JiselAnnotationProcessor.class.getName());

    private final JiselAnnotationHandler topLevelHandler;
    private final AbstractSealedSealForHandler sealForHandler;
    private final JiselAnnotationHandler unSealHandler;

    /**
     * JiselAnnotationProcessor constructor. Initializes needed instances of {@link SealForHandler}, {@link AddToHandler},
     * {@link TopLevelHandler}, {@link UnSealHandler} and {@link InterfaceSourceFileGenerator}
     */
    public JiselAnnotationProcessor() {
        this.sealForHandler = new SealForHandler();
        this.topLevelHandler = new TopLevelHandler();
        this.unSealHandler = new UnSealHandler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // the following 3 objects must be instantiating only when processingEnv is != null
        var addToHandler = new AddToHandler(processingEnv);
        var detachHandler = new DetachHandler(processingEnv);
        var interfaceSourceFileGenerator = new InterfaceSourceFileGenerator(processingEnv);

        var allAnnotatedSealForElements = new HashSet<Element>();
        var allAnnotatedTopLevelElements = new HashSet<Element>();
        var allAnnotatedAddToElements = new HashSet<Element>();
        var allAnnotatedUnSealElements = new HashSet<Element>();
        var allAnnotatedDetachElements = new HashSet<Element>();

        var sealedInterfacesToGenerateByLargeInterface = new HashMap<Element, Map<String, Set<Element>>>();
        var sealedInterfacesPermitsByLargeInterface = new HashMap<Element, Map<String, List<String>>>();
        var unSealValueByLargeInterface = new HashMap<Element, Boolean>();
        var detachedInterfacesToGenerateByLargeInterface = new HashMap<Element, Map<String, Map<String, Object>>>();

        populateAllAnnotatedElementsSets(
                annotations,
                roundEnv,
                Map.of(
                        ALL_ANNOTATED_TOPLEVEL_ELEMENTS, allAnnotatedTopLevelElements,
                        ALL_ANNOTATED_SEALFOR_ELEMENTS, allAnnotatedSealForElements,
                        ALL_ANNOTATED_ADDTO_ELEMENTS, allAnnotatedAddToElements,
                        ALL_ANNOTATED_UNSEAL_ELEMENTS, allAnnotatedUnSealElements,
                        ALL_ANNOTATED_DETACH_ELEMENTS, allAnnotatedDetachElements
                )
        );

        // continue execution only if at least 1 element has been annotated with @TopLevel
        if (!allAnnotatedTopLevelElements.isEmpty()) {

            processTopLevelAndSealForAnnotatedElements(
                    topLevelHandler,
                    sealForHandler,
                    Map.of(
                            ALL_ANNOTATED_TOPLEVEL_ELEMENTS, allAnnotatedTopLevelElements,
                            ALL_ANNOTATED_SEALFOR_ELEMENTS, allAnnotatedSealForElements
                    ),
                    sealedInterfacesToGenerateByLargeInterface,
                    sealedInterfacesPermitsByLargeInterface
            );

            processUnSealAnnotatedElements(
                    unSealHandler,
                    allAnnotatedUnSealElements,
                    unSealValueByLargeInterface,
                    sealedInterfacesToGenerateByLargeInterface
            );

            processDetachAnnotatedElements(
                    detachHandler,
                    allAnnotatedDetachElements,
                    sealedInterfacesToGenerateByLargeInterface,
                    sealedInterfacesPermitsByLargeInterface,
                    detachedInterfacesToGenerateByLargeInterface
            );

            processAddToAnnotatedElements(
                    addToHandler,
                    allAnnotatedAddToElements,
                    sealedInterfacesToGenerateByLargeInterface,
                    sealedInterfacesPermitsByLargeInterface
            );

            checkForPermitsMapWithSingleEntryPerLargeInterface(sealedInterfacesPermitsByLargeInterface);

            try {
                var generatedFiles = interfaceSourceFileGenerator.createSourceFiles(
                        sealedInterfacesToGenerateByLargeInterface,
                        sealedInterfacesPermitsByLargeInterface,
                        unSealValueByLargeInterface,
                        detachedInterfacesToGenerateByLargeInterface
                );
                if (!generatedFiles.isEmpty()) {
                    log.info(() -> format("%s:%n%s", FILE_GENERATION_SUCCESS, generatedFiles.stream().collect(joining(NEW_LINE))));
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, FILE_GENERATION_ERROR, e);
            }
        }

        return true;
    }

    @Override
    public void notifyStatusReportDisplay(String statusReportText) {
        log.warning(statusReportText::toString);
    }
}