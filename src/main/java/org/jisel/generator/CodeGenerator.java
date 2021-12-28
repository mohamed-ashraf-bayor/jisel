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
package org.jisel.generator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.ExecutableType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * Exposes contract to be fulfilled by a class generating the code of a sealed interface
 */
public sealed interface CodeGenerator permits JavaxGeneratedGenerator, ExtendsGenerator, PermitsGenerator, MethodsGenerator {

    /**
     * Generates piece of code requested, based on the parameters provided in the params object and appends it to the provided classOrInterfaceContent param
     *
     * @param classOrInterfaceContent Stringbuilder object containing the sealed interface code being generated
     * @param params                  expected parameters
     */
    void generateCode(StringBuilder classOrInterfaceContent, List<String> params);
}

/**
 * Exposes contract to be fulfilled by a class generating the "extends" clause of a sealed interface definition, along with
 * the list of the parent classes or interfaces
 */
sealed interface ExtendsGenerator extends CodeGenerator, StringGenerator permits SealedInterfaceExtendsGenerator {

    /**
     * Generates the "extends" clause of a sealed interface being generated, along with the list of parent interfaces, based on
     * a provided Map containing parents/subtypes information (the permits Map) and the name of the profile for which the
     * sealed interface will be generated
     *
     * @param processingEnvironment  {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param sealedInterfaceContent StringBuilder object containing the sealed interface code being generated
     * @param permitsMap             Map containing parents/subtypes information. The Map key is the profile name whose generated
     *                               sealed interface will be a parent interface, while the value is the list of profiles names whose
     *                               sealed interfaces will be generated as subtypes
     * @param processedProfile       name of the profile whose sealed interface is being generated
     * @param largeInterfaceElement  Element instance of the large interface being segregated
     */
    void generateExtendsClauseFromPermitsMapAndProcessedProfile(
            ProcessingEnvironment processingEnvironment,
            StringBuilder sealedInterfaceContent,
            Map<String, List<String>> permitsMap,
            String processedProfile,
            Element largeInterfaceElement
    );

    @Override
    default void generateCode(final StringBuilder classOrInterfaceContent, final List<String> params) {
        classOrInterfaceContent.append(format(
                " %s %s ",
                isInterface(classOrInterfaceContent.toString()) ? EXTENDS : IMPLEMENTS,
                params.stream().map(StringGenerator::removeCommaSeparator).collect(joining(COMMA_SEPARATOR + WHITESPACE))
        ));
    }

    private boolean isInterface(final String classOrInterfaceContent) {
        return classOrInterfaceContent.contains(INTERFACE + WHITESPACE) && !classOrInterfaceContent.contains(CLASS + WHITESPACE);
    }
}

/**
 * Exposes contract to be fulfilled by a class generating the "permits" clause of a sealed interface definition, along with
 * the list of the subtypes classes or interfaces permitted by the sealed interface being generated
 */
sealed interface PermitsGenerator extends CodeGenerator, StringGenerator permits SealedInterfacePermitsGenerator {

    /**
     * Generates the "permits" clause of a sealed interface being generated, along with the list of parent interfaces, based on
     * a provided Map containing parents/subtypes information (the permits Map) and the name of the profile for which the
     * sealed interface will be generated
     *
     * @param sealedInterfaceContent StringBuilder object containing the sealed interface code being generated
     * @param permitsMap             Map containing parents/subtypes information. The Map key is the profile name whose generated
     *                               sealed interface will be a parent interface, while the value is the list of profiles names whose
     *                               sealed interfaces will be generated as subtypes
     * @param processedProfile       name of the profile whose sealed interface is being generated
     * @param largeInterfaceElement  Element instance of the large interface being segregated
     */
    void generatePermitsClauseFromPermitsMapAndProcessedProfile(
            StringBuilder sealedInterfaceContent,
            Map<String, List<String>> permitsMap,
            String processedProfile,
            Element largeInterfaceElement
    );

    @Override
    default void generateCode(final StringBuilder classOrInterfaceContent, final List<String> params) {
        classOrInterfaceContent.append(format(
                " %s %s ",
                PERMITS,
                params.stream().map(StringGenerator::removeCommaSeparator).collect(joining(COMMA_SEPARATOR + WHITESPACE))
        ));
    }

    /**
     * Adds a generated final class to the Map containing parents/subtypes information, only for the sealed interfaces at the
     * lowest-level of the generated hierarchy (also know as childless interfaces).<br>
     * Practice proper to Jisel only to avoid compilation errors for sealed interfaces not having any existing subtypes
     *
     * @param permitsMap            Map containing parents/subtypes information. The Map key is the profile name whose generated
     *                              sealed interface will be a parent interface, while the value is the list of profiles names whose
     *                              sealed interfaces will be generated as subtypes
     * @param largeInterfaceElement Element instance of the large interface being segregated
     */
    default void addFinalClassToPermitsMap(final Map<String, List<String>> permitsMap, final Element largeInterfaceElement) {
        var finalClassName = UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX;
        var childlessProfiles = permitsMap.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .filter(childProfileName -> permitsMap.keySet().stream().noneMatch(parentProfile -> parentProfile.equals(childProfileName)))
                .filter(childProfileName -> !finalClassName.equals(childProfileName)) // if finalClassName found remove it from the new list
                .filter(childProfileName -> !childProfileName.contains(DOT)) // also skip all qualifiedname classes added by @addToProfile
                .toList();
        childlessProfiles.forEach(childlessProfile -> permitsMap.put(childlessProfile, asList(finalClassName)));
    }
}

/**
 * Exposes contract to be fulfilled by a class generating the list of abstracts methods of a sealed interface being generated.<br>
 * Same is also used to generate the list of concrete methods of the convenience final class generated for the bottom-level generated sealed interfaces
 */
sealed interface MethodsGenerator extends CodeGenerator, StringGenerator permits SealedInterfaceMethodsGenerator {

    /**
     * Generates a list of abstracts methods definitions and appends it to the sealed interface code being generated
     *
     * @param sealedInterfaceContent StringBuilder object containing the sealed interface code being generated
     * @param methodsSet             Set of {@link Element} instances representing each one of the abstract methods to generate
     */
    void generateAbstractMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet);

    /**
     * Mainly used for a final class generation.<br>
     * Generates a list of concrete methods definitions (signature and body), and appends it to the final class being generated
     *
     * @param sealedInterfaceContent StringBuilder object containing the sealed interface code being generated
     * @param methodsSet             Set of {@link Element} instances representing each one of the abstract methods to generate
     */
    void generateEmptyConcreteMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet);

    @Override
    default void generateCode(final StringBuilder classOrInterfaceContent, final List<String> params) {
        params.forEach(methodDefinition -> classOrInterfaceContent.append(format("\t%s%n", methodDefinition)));
    }

    /**
     * Checks whether the provided method {@link Element} instance has any arguments
     *
     * @param methodElement method {@link Element} instance
     * @return true if the provided method has any arguments
     */
    default boolean methodHasArguments(final Element methodElement) {
        return methodElement.toString().indexOf(CLOSING_PARENTHESIS) - methodElement.toString().indexOf(OPENING_PARENTHESIS) > 1;
    }

    /**
     * Returns a string representing the fully qualified name of a method return type
     *
     * @param methodElement method {@link Element} instance
     * @return a string representing the fully qualified name of the provided method's return type
     */
    default String generateReturnType(final Element methodElement) {
        return ((ExecutableType) methodElement.asType()).getReturnType().toString();
    }

    /**
     * Constructs a string containing a method name and parameters formatted as in the method signature
     *
     * @param methodElement method {@link Element} instance
     * @return a string containing a method name and parameters formatted as in the method signature
     */
    default String generateMethodNameAndParameters(final Element methodElement) {
        var output = methodElement.toString();
        if (methodHasArguments(methodElement)) {
            int paramIdx = 0;
            while (output.contains(COMMA_SEPARATOR)) {
                output = output.replace(COMMA_SEPARATOR, WHITESPACE + PARAMETER_PREFIX + paramIdx + TEMP_PLACEHOLDER + WHITESPACE);
                paramIdx++;
            }
            output = output.replace(CLOSING_PARENTHESIS, WHITESPACE + PARAMETER_PREFIX + paramIdx + CLOSING_PARENTHESIS).replaceAll(TEMP_PLACEHOLDER, COMMA_SEPARATOR);
        }
        return output;
    }

    /**
     * Constructs a string containing a comma-separated list of exceptions classes thrown by the provided method {@link Element} instance
     *
     * @param methodElement method {@link Element} instance
     * @return a comma-separated list of exceptions classes thrown by the provided method {@link Element} instance
     */
    default String generateThrownExceptions(final Element methodElement) {
        return ((ExecutableType) methodElement.asType()).getThrownTypes().stream().map(Object::toString).collect(joining(COMMA_SEPARATOR + WHITESPACE));
    }

    /**
     * Constructs a string containing the default value for the provided method's return type
     *
     * @param methodElement method {@link Element} instance
     * @return a string containing the default value for the provided method's return type
     */
    default String generateDefaultReturnValueForMethod(final Element methodElement) {
        return switch (((ExecutableType) methodElement.asType()).getReturnType().getKind()) {
            case BOOLEAN -> RETURN + WHITESPACE + DEFAULT_BOOLEAN_VALUE;
            case VOID -> RETURN;
            case BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, CHAR -> RETURN + WHITESPACE + DEFAULT_NUMBER_VALUE;
            default -> RETURN + WHITESPACE + DEFAULT_NULL_VALUE;
        };
    }
}