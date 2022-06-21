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
package org.jisel.generators.codegen.impl;

import org.jisel.generators.codegen.MethodsGenerator;

import javax.lang.model.element.Element;
import java.util.Set;

import static java.lang.String.format;
import static org.jisel.generators.codegen.AnnotationsGenerator.buildExistingAnnotations;

/**
 * Generates the list of abstracts methods of a sealed interface being generated
 */
public final class InterfaceMethodsGenerator implements MethodsGenerator {

    @Override
    public void generateAbstractMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet) {
        generateCode(
                sealedInterfaceContent,
                methodsSet.stream()
                        .map(element -> {
                            var existingAnnotations = buildExistingAnnotations(element, NEW_LINE);
                            var thrownExceptions = generateThrownExceptions(element);
                            return format(
                                    "%s%s %s%s",
                                    existingAnnotations.isEmpty() ? EMPTY_STRING : existingAnnotations + NEW_LINE + TAB,
                                    generateReturnType(element),
                                    generateMethodNameAndParameters(element),
                                    thrownExceptions.isEmpty() ? SEMICOLON : format(" throws %s", thrownExceptions + SEMICOLON)
                            );
                        })
                        .toList()
        );
    }

    @Override
    public void generateEmptyConcreteMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet) {
        generateCode(
                sealedInterfaceContent,
                methodsSet.stream()
                        .map(methodElement -> format(
                                "public %s %s %s",
                                generateReturnType(methodElement),
                                generateMethodNameAndParameters(methodElement),
                                generateThrownExceptions(methodElement).isEmpty()
                                        ? OPENING_CURLY_BRACE + generateDefaultReturnValueForMethod(methodElement) + SEMICOLON + CLOSING_CURLY_BRACE
                                        : format("throws %s",
                                        generateThrownExceptions(methodElement) + WHITESPACE + OPENING_CURLY_BRACE
                                                + generateDefaultReturnValueForMethod(methodElement) + SEMICOLON + CLOSING_CURLY_BRACE
                                )
                        ))
                        .toList()
        );
    }
}
