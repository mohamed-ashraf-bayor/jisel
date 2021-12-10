package org.jisel.generator;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public interface StringGenerator {

    String COMMA_SEPARATOR = ",";
    String WHITESPACE = " ";
    String SEALED_PREFIX = "Sealed";
    String PACKAGE = "package";
    String PUBLIC_SEALED_INTERFACE = "public sealed interface";
    String EXTENDS = "extends";
    String PERMITS = "permits";
    String OPENING_BRACKET = "{";
    String CLOSING_BRACKET = "}";
    String OPENING_PARENTHESIS = "(";
    String CLOSING_PARENTHESIS = ")";
    String EMPTY_STRING = "";
    String DOT = ".";
    String PARAMETER_PREFIX = "param";
    String TEMP_PLACEHOLDER = "@";

    default String removeSeparator(final String text) {
        return asList(text.split(COMMA_SEPARATOR)).stream().collect(joining());
    }

    default String sealedInterfaceNameConvention(final String profile, final Element interfaceElement) {
        return format(
                "%s%s%s",
                SEALED_PREFIX,
                removeSeparator(profile),
                removeSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString()
        );
    }

    default List<String> sealedInterfaceNameConventionForList(final List<String> profiles, final Element interfaceElement) {
        return profiles.stream()
                .map(profile ->
                        format(
                                "%s%s%s",
                                SEALED_PREFIX,
                                removeSeparator(profile),
                                removeSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString()
                        )).toList();
    }

    default Optional<String> generatePackageName(final Element bloatedInterfaceName) {
        var qualifiedClassName = bloatedInterfaceName.toString();
        int lastDot = qualifiedClassName.lastIndexOf('.');
        return lastDot > 0 ? Optional.of(qualifiedClassName.substring(0, lastDot)) : Optional.empty();
    }

    default String removeDoubleSpaceOccurrences(final String text) {
        return text.replaceAll(WHITESPACE + WHITESPACE, WHITESPACE);
    }
}