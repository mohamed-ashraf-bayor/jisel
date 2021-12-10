package org.jisel.generator;

import javax.lang.model.element.Element;
import javax.lang.model.type.ExecutableType;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public interface StringGenerator {

    String COMMA_SEPARATOR = ",";
    String SEMICOLON = ";";
    String WHITESPACE = " ";
    String SEALED_PREFIX = "Sealed";
    String PACKAGE = "package";
    String INTERFACE = "interface";
    String CLASS = "class";
    String PUBLIC_SEALED_INTERFACE = "public sealed interface";
    String PUBLIC_FINAL_CLASS = "public final class";
    String EXTENDS = "extends";
    String IMPLEMENTS = "implements";
    String PERMITS = "permits";
    String OPENING_BRACKET = "{";
    String CLOSING_BRACKET = "}";
    String OPENING_PARENTHESIS = "(";
    String CLOSING_PARENTHESIS = ")";
    String EMPTY_STRING = "";
    String DOT = ".";
    String PARAMETER_PREFIX = "param";
    String TEMP_PLACEHOLDER = "@";
    String UNDERSCORE = "_";
    String FINAL_CLASS_SUFFIX = "FinalCass";
    String RETURN = "return";
    String JAVA_LANG_OBJECT = "java.lang.Object";

    String SEAL_FOR_PROFILE = "SealForProfile";
    String ADD_TO_PROFILE = "AddToProfile";

    String STATUS_REPORT_TITLE = "JISEL GENERATION REPORT";

    String FILE_GENERATION_ERROR = "Error generating sealed interfaces";
    String FILE_GENERATION_SUCCESS = "Successfully generated";

    String ORG_JISEL_SEAL_FOR_PROFILE = "org.jisel.SealForProfile";
    String ORG_JISEL_SEAL_FOR_PROFILES = "org.jisel.SealForProfiles";
    String ORG_JISEL_SEAL_FOR_PROFILEZ = "org.jisel.SealForProfile.SealForProfilez";
    String ORG_JISEL_ADD_TO_PROFILE = "org.jisel.AddToProfile";
    String ORG_JISEL_ADD_TO_PROFILES = "org.jisel.AddToProfiles";
    String ORG_JISEL_ADD_TO_PROFILEZ = "org.jisel.AddToProfile.AddToProfilez";

    String DEFAULT_BOOLEAN_VALUE = "false";
    String DEFAULT_NUMBER_VALUE = "0";
    String DEFAULT_NULL_VALUE = "null";

    String[] METHODS_TO_EXCLUDE = {"getClass", "wait", "notifyAll", "hashCode", "equals", "notify", "toString"};

    String ADD_TO_PROFILE_REPORT_MSG = "1 or many provided profiles are not found in provided parent interfaces. Check your profiles names.";
    String REPORT_MSG = "More than 1 Top-Level Parent Sealed Interfaces will be generated. Check your profiles mapping.";

    static String removeSeparator(final String text) {
        return asList(text.split(COMMA_SEPARATOR)).stream().collect(joining());
    }

    default String sealedInterfaceNameConvention(final String profile, final Element interfaceElement) {
        var nameSuffix = removeSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString();
        // any profile name starting w _ is returned as it is
        return removeSeparator(profile).startsWith(UNDERSCORE) ? removeSeparator(profile) : format(
                "%s%s%s",
                SEALED_PREFIX,
                removeSeparator(profile),
                nameSuffix
        );
    }

    default List<String> sealedInterfaceNameConventionForList(final List<String> profiles, final Element interfaceElement) {
        final UnaryOperator<String> nameSuffix = profile -> removeSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString();
        return profiles.stream()
                .map(profile -> removeSeparator(profile).startsWith(UNDERSCORE) ? removeSeparator(profile) : format(
                        "%s%s%s",
                        SEALED_PREFIX,
                        removeSeparator(profile),
                        nameSuffix.apply(profile)
                )).toList();
    }

    default Optional<String> generatePackageName(final Element bloatedInterfaceName) {
        var qualifiedClassName = bloatedInterfaceName.toString();
        int lastDot = qualifiedClassName.lastIndexOf('.');
        return lastDot > 0 ? Optional.of(qualifiedClassName.substring(0, lastDot)) : Optional.empty();
    }

    default String removeDoubleSpaceOccurrences(final String text) {
        return text.replace(WHITESPACE + WHITESPACE, WHITESPACE);
    }

    default String generateDefaultReturnValueForMethod(final Element methodElement) {
        return switch (((ExecutableType) methodElement.asType()).getReturnType().getKind()) {
            case BOOLEAN -> RETURN + WHITESPACE + DEFAULT_BOOLEAN_VALUE;
            case VOID -> RETURN;
            case BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, CHAR -> RETURN + WHITESPACE + DEFAULT_NUMBER_VALUE;
            default -> RETURN + WHITESPACE + DEFAULT_NULL_VALUE;
        };
    }
}