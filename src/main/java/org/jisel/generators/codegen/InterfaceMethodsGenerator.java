package org.jisel.generators.codegen;

import javax.lang.model.element.Element;
import java.util.Set;

import static java.lang.String.format;

/**
 * Generates the list of abstracts methods of a sealed interface being generated
 */
public final class InterfaceMethodsGenerator implements MethodsGenerator {

    @Override
    public void generateAbstractMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet) {
        generateCode(
                sealedInterfaceContent,
                methodsSet.stream()
                        .map(element -> format(
                                "%s %s%s",
                                generateReturnType(element),
                                generateMethodNameAndParameters(element),
                                generateThrownExceptions(element).isEmpty()
                                        ? SEMICOLON
                                        : format(" throws %s", generateThrownExceptions(element) + SEMICOLON)
                        ))
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
