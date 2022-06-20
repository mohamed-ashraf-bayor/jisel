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
package org.jisel.generators.codegen;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * Exposes contract to be fulfilled by a class generating the list of abstracts methods of a sealed interface being generated.<br>
 * Same is also used to generate the list of concrete methods of the convenience final class generated for the bottom-level generated sealed interfaces
 */
public sealed interface MethodsGenerator extends CodeGenerator permits InterfaceMethodsGenerator {

    /**
     * Generates a list of abstracts methods definitions and appends it to the sealed interface code being generated
     *
     * @param sealedInterfaceContent {@link StringBuilder} object containing the sealed interface code being generated
     * @param methodsSet             {@link Set} of {@link Element} instances representing each one of the abstract methods to generate
     */
    void generateAbstractMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet);

    /**
     * Mainly used for a final class generation.<br>
     * Generates a list of concrete methods definitions (signature and body), and appends it to the final class being generated
     *
     * @param sealedInterfaceContent {@link StringBuilder} object containing the sealed interface code being generated
     * @param methodsSet             {@link Set} of {@link Element} instances representing each one of the abstract methods to generate
     */
    void generateEmptyConcreteMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet);

    @Override
    default void generateCode(StringBuilder classOrInterfaceContent, List<String> params) {
        params.forEach(methodDefinition -> classOrInterfaceContent.append(format("\t%s%n", methodDefinition)));
    }

    /**
     * Returns a string representing the fully qualified name of a method return type
     *
     * @param methodElement method {@link Element} instance
     * @return a string representing the fully qualified name of the provided method's return type
     */
    default String generateReturnType(Element methodElement) {
        return ((ExecutableType) methodElement.asType()).getReturnType().toString();
    }

    /**
     * Constructs a string containing a method name and parameters formatted as per the method signature
     *
     * @param methodElement method {@link Element} instance
     * @return a string containing a method name and parameters formatted as per the method signature
     */
    default String generateMethodNameAndParameters(Element methodElement) {
        // TODO rewrite
        if (((ExecutableElement) methodElement).getParameters().isEmpty()) { // checks whether the provided method Element instance has 0 args
            return methodElement.toString();
        }
        int paramIdx = 0;
        var output = methodElement.toString();
        while (output.contains(COMMA_SEPARATOR)) {
            output = output.replace(COMMA_SEPARATOR, WHITESPACE + PARAMETER_PREFIX + paramIdx + TEMP_PLACEHOLDER + WHITESPACE);
            paramIdx++;
        }
        return output.replace(CLOSING_PARENTHESIS, WHITESPACE + PARAMETER_PREFIX + paramIdx + CLOSING_PARENTHESIS)
                .replaceAll(TEMP_PLACEHOLDER, COMMA_SEPARATOR);
    }

    /**
     * Constructs a string containing a comma-separated list of exceptions classes thrown by the provided method {@link Element} instance
     *
     * @param methodElement method {@link Element} instance
     * @return a comma-separated list of exceptions classes thrown by the provided method {@link Element} instance
     */
    default String generateThrownExceptions(Element methodElement) {
        return ((ExecutableType) methodElement.asType()).getThrownTypes().stream()
                .map(Object::toString)
                .collect(joining(COMMA_SEPARATOR + WHITESPACE));
    }

    /**
     * Constructs a string containing the default value for the provided method's return type
     *
     * @param methodElement method {@link Element} instance
     * @return a string containing the default value for the provided method's return type
     */
    default String generateDefaultReturnValueForMethod(Element methodElement) {
        return switch (((ExecutableType) methodElement.asType()).getReturnType().getKind()) {
            case BOOLEAN -> RETURN + WHITESPACE + DEFAULT_BOOLEAN_VALUE;
            case VOID -> RETURN;
            case BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, CHAR -> RETURN + WHITESPACE + DEFAULT_NUMBER_VALUE;
            default -> RETURN + WHITESPACE + DEFAULT_NULL_VALUE;
        };
    }
}
