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

import javax.lang.model.element.Element;
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
    String PROFILE_ATTRIBUTE_REGEX = "profile=\"([^\"]*)\"";
    // String PROFILES_ATTRIBUTE_REGEX = "profiles=\"([^\"]*)\""; // not working when { } present ?
    String LARGE_INTERFACE_ATTRIBUTE_REGEX = "largeInterface=\"([^\"]*)\"";
    String ANNOTATION_VALUES_REGEX = "\"([^\"]*)\"";
    String ADD_TO_PROFILE_REGEX = "AddToProfile\\((.*?)\\)"; // @org.jisel.AddToProfile(profile="ActiveWorker", largeInterface="com.bayor.jisel.annotation.client.data.Sociable")
    String ADD_TO_PROFILES_REGEX = "AddToProfiles\\((.*?)\\)"; // @org.jisel.AddToProfiles(profiles={"Student", "Worker"}, largeInterface="com.bayor.jisel.annotation.client.data.Sociable"),


    String STATUS_REPORT_TITLE = "JISEL GENERATION REPORT";

    String FILE_GENERATION_ERROR = "Error generating sealed interfaces";
    String FILE_GENERATION_SUCCESS = "Successfully generated";

    String ORG_JISEL_SEAL_FOR_PROFILE = "org.jisel.SealForProfile";
    String ORG_JISEL_SEAL_FOR_PROFILES = "org.jisel.SealForProfiles";
    String ORG_JISEL_SEAL_FOR_PROFILEZ = "org.jisel.SealForProfile.SealForProfilez";
    String ORG_JISEL_SEAL_FOR_PROFILEZZ = "org.jisel.SealForProfiles.SealForProfilezz";
    String ORG_JISEL_ADD_TO_PROFILE = "org.jisel.AddToProfile";
    String ORG_JISEL_ADD_TO_PROFILES = "org.jisel.AddToProfiles";
    String ORG_JISEL_ADD_TO_PROFILEZ = "org.jisel.AddToProfile.AddToProfilez";
    String ORG_JISEL_ADD_TO_PROFILEZZ = "org.jisel.AddToProfiles.AddToProfilezz";

    String DEFAULT_BOOLEAN_VALUE = "false";
    String DEFAULT_NUMBER_VALUE = "0";
    String DEFAULT_NULL_VALUE = "null";

    String[] METHODS_TO_EXCLUDE = {"getClass", "wait", "notifyAll", "hashCode", "equals", "notify", "toString"};

    String ADD_TO_PROFILE_REPORT_MSG = "1 or many provided profiles are not found in the provided parent interfaces. Check your profiles and/or parent interfaces names.";
    String SEAL_FOR_PROFILE_REPORT_MSG = "More than 1 Top-Level Parent Sealed Interfaces will be generated. Check your profiles mapping.";

    String JISEL_REPORT_SUFFIX = "Report.txt";

    String JISEL_REPORT_CREATED_SEALED_INTERFACES_HEADER = "Created sealed interfaces:";

    String JISEL_REPORT_CHILDREN_HEADER = "Children:";

    static String removeSeparator(final String text) {
        return asList(text.split(COMMA_SEPARATOR)).stream().collect(joining());
    }

    default String sealedInterfaceNameConvention(final String profile, final Element interfaceElement) {
        var nameSuffix = removeSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString();
        // any profile name starting w _ (final classes names) or containing a dot (classes annotated with addtoprofile) is returned as is
        return removeSeparator(profile).startsWith(UNDERSCORE) || profile.contains(DOT) ? removeSeparator(profile) : format(
                "%s%s%s",
                SEALED_PREFIX,
                removeSeparator(profile),
                nameSuffix
        );
    }

    default List<String> sealedInterfaceNameConventionForList(final List<String> profiles, final Element interfaceElement) {
        final UnaryOperator<String> nameSuffix = profile -> removeSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString();
        return profiles.stream()
                .map(profile -> removeSeparator(profile).startsWith(UNDERSCORE) || profile.contains(DOT) ? removeSeparator(profile) : format(
                        "%s%s%s",
                        SEALED_PREFIX,
                        removeSeparator(profile),
                        nameSuffix.apply(profile)
                )).toList();
    }

    default Optional<String> generatePackageName(final Element largeInterfaceName) {
        var qualifiedClassName = largeInterfaceName.toString();
        int lastDot = qualifiedClassName.lastIndexOf('.');
        return lastDot > 0 ? Optional.of(qualifiedClassName.substring(0, lastDot)) : Optional.empty();
    }

    default String removeDoubleSpaceOccurrences(final String text) {
        return text.replace(WHITESPACE + WHITESPACE, WHITESPACE);
    }

    default String removeCurlyBraces(final String text) {
        return text.replace(OPENING_BRACKET, EMPTY_STRING).replace(CLOSING_BRACKET, EMPTY_STRING);
    }
}