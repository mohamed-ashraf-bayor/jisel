package org.jisel;

import com.google.auto.service.AutoService;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

@SupportedAnnotationTypes({"org.jisel.SealForProfile", "org.jisel.SealForProfiles", "org.jisel.SealForProfile.SealForProfilez",
        "org.jisel.AddToProfile", "org.jisel.AddToProfiles", "org.jisel.AddToProfile.AddToProfilez"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JiselAnnotationProcessor extends AbstractProcessor {

    private final Logger log = Logger.getLogger(JiselAnnotationProcessor.class.getName());

    private static final String SEAL_FOR_PROFILE = "SealForProfile";
    private static final String ADD_TO_PROFILE = "AddToProfile";

    private JiselAnnotationHandler sealForProfileHandler;

    private JiselAnnotationHandler addToProfileHandler;

    public JiselAnnotationProcessor() {
        this.sealForProfileHandler = new SealForProfileHandler();
        this.addToProfileHandler = new AddToProfileHandler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        var allAnnotatedSealForProfileElements = new HashSet<Element>();
        var allAnnotatedAddToProfileElements = new HashSet<Element>();
        var sealedInterfacesToGenerate = new HashMap<Element, Map<String, Set<Element>>>();
        var sealedInterfacesPermits = new HashMap<Element, Map<String, List<String>>>();

        for (var annotation : annotations) {
            if (annotation.getSimpleName().toString().contains(SEAL_FOR_PROFILE)) {
                allAnnotatedSealForProfileElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
            if (annotation.getSimpleName().toString().contains(ADD_TO_PROFILE)) {
                allAnnotatedAddToProfileElements.addAll(roundEnv.getElementsAnnotatedWith(annotation));
            }
        }

        // process all annotated interface methods annotated with @SealForProfile
        var statusReport = sealForProfileHandler.handleAnnotatedElements(processingEnv, unmodifiableSet(allAnnotatedSealForProfileElements), sealedInterfacesToGenerate, sealedInterfacesPermits);
        if (!statusReport.isEmpty()) {
            // TODO output log - create priv meth browse the map and output report per interface
        }
        // TODO sealedInterfacesPermits here shld be getting only qlfd names of child classes

        // process all annotated child class or interfaces annotated with @AddToProfile
        statusReport = addToProfileHandler.handleAnnotatedElements(processingEnv, unmodifiableSet(allAnnotatedAddToProfileElements), unmodifiableMap(sealedInterfacesToGenerate), sealedInterfacesPermits);
        if (!statusReport.isEmpty()) {
            //  output log - create priv meth
        }

//        System.out.println(" \n\n@@@@@@@@@@@@@ sealedInterfacesToGenerate: " + sealedInterfacesToGenerate);
//        System.out.println("\n~~~~~~~~~~ ~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~ \n sealedInterfacesPermits" + sealedInterfacesPermits);

        return true;
    }
}