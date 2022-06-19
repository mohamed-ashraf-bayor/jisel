package org.jisel.generators.codegen;

import org.jisel.generators.StringGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * Exposes contract to be fulfilled by a class generating the "extends" clause of a sealed interface definition, along with
 * the list of the parent classes or interfaces
 */
public sealed interface ExtendsGenerator extends CodeGenerator permits InterfaceExtendsGenerator {

    /**
     * Generates the "extends" clause of a sealed interface being generated, along with the list of parent interfaces, based on
     * a provided {@link Map} containing parents/subtypes information (the permits Map) and the name of the profile for which the
     * sealed interface will be generated
     *
     * @param processingEnvironment  {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param sealedInterfaceContent {@link StringBuilder} object containing the sealed interface code being generated
     * @param permitsMap             {@link Map} containing parents/subtypes information. The Map key is the profile name whose generated
     *                               sealed interface will be a parent interface, while the value is the list of profiles names whose
     *                               sealed interfaces will be generated as subtypes
     * @param processedProfile       name of the profile whose sealed interface is being generated
     * @param largeInterfaceElement  {@link Element} instance of the large interface being segregated
     */
    void generateExtendsClauseFromPermitsMapAndProcessedProfile(
            ProcessingEnvironment processingEnvironment,
            StringBuilder sealedInterfaceContent,
            Map<String, List<String>> permitsMap,
            String processedProfile,
            Element largeInterfaceElement,
            boolean unSeal
    );

    @Override
    default void generateCode(StringBuilder classOrInterfaceContent, List<String> params) {
        classOrInterfaceContent.append(format(
                " %s %s ",
                isInterface(classOrInterfaceContent.toString()) ? EXTENDS : IMPLEMENTS,
                params.stream().map(StringGenerator::removeCommaSeparator).collect(joining(COMMA_SEPARATOR + WHITESPACE))
        ));
    }

    private boolean isInterface(String classOrInterfaceContent) {
        return classOrInterfaceContent.contains(INTERFACE + WHITESPACE) && !classOrInterfaceContent.contains(CLASS + WHITESPACE);
    }
}
