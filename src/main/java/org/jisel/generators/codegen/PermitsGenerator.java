package org.jisel.generators.codegen;

import org.jisel.generators.StringGenerator;

import javax.lang.model.element.Element;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * Exposes contract to be fulfilled by a class generating the "permits" clause of a sealed interface definition, along with
 * the list of the subtypes classes or interfaces permitted by the sealed interface being generated
 */
public sealed interface PermitsGenerator extends CodeGenerator permits SealedInterfacePermitsGenerator {

    /**
     * Generates the "permits" clause of a sealed interface being generated, along with the list of parent interfaces, based on
     * a provided {@link Map} containing parents/subtypes information (the permits Map) and the name of the profile for which the
     * sealed interface will be generated
     *
     * @param sealedInterfaceContent {@link StringBuilder} object containing the sealed interface code being generated
     * @param permitsMap             {@link Map} containing parents/subtypes information. The Map key is the profile name whose generated
     *                               sealed interface will be a parent interface, while the value is the list of profiles names whose
     *                               sealed interfaces will be generated as subtypes
     * @param processedProfile       name of the profile whose sealed interface is being generated
     * @param largeInterfaceElement  {@link Element} instance of the large interface being segregated
     */
    void generatePermitsClauseFromPermitsMapAndProcessedProfile(
            StringBuilder sealedInterfaceContent,
            Map<String, List<String>> permitsMap,
            String processedProfile,
            Element largeInterfaceElement
    );

    @Override
    default void generateCode(StringBuilder classOrInterfaceContent, List<String> params) {
        classOrInterfaceContent.append(format(
                " %s %s ",
                PERMITS,
                params.stream().map(StringGenerator::removeCommaSeparator).collect(joining(COMMA_SEPARATOR + WHITESPACE))
        ));
    }

    /**
     * Adds a generated final class to the {@link Map} containing parents/subtypes information, only for the sealed interfaces at the
     * lowest-level of the generated hierarchy (also known as childless interfaces).<br>
     * Practice proper to Jisel only to avoid compilation errors for sealed interfaces not having any existing subtypes
     *
     * @param permitsMap            {@link Map} containing parents/subtypes information. The Map key is the profile name whose generated
     *                              sealed interface will be a parent interface, while the value is the list of profiles names whose
     *                              sealed interfaces will be generated as subtypes
     * @param largeInterfaceElement {@link Element} instance of the large interface being segregated
     */
    default void addFinalClassToPermitsMap(Map<String, List<String>> permitsMap, Element largeInterfaceElement) {
        var finalClassName = UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX;
        var childlessProfiles = permitsMap.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .filter(childProfileName -> permitsMap.keySet().stream().noneMatch(parentProfile -> parentProfile.equals(childProfileName)))
                .filter(childProfileName -> !finalClassName.equals(childProfileName)) // if finalClassName found remove it from the new list
                .filter(childProfileName -> !childProfileName.contains(DOT)) // also skip all qualifiedname classes added by @AddTo
                .toList();
        childlessProfiles.forEach(childlessProfile -> permitsMap.put(childlessProfile, asList(finalClassName)));
    }
}