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

/**
 * A bunch of String literals and commonly used string handling functions
 */
public interface StringGenerator {

    /**
     * "," comma separator
     */
    String COMMA_SEPARATOR = ",";
    /**
     * ";" semi-colon
     */
    String SEMICOLON = ";";
    /**
     * " " whitespace
     */
    String WHITESPACE = " ";
    /**
     * "Sealed"
     */
    String SEALED_PREFIX = "Sealed";
    /**
     * "package"
     */
    String PACKAGE = "package";
    /**
     * "interface"
     */
    String INTERFACE = "interface";
    /**
     * "class"
     */
    String CLASS = "class";
    /**
     * "public sealed interface"
     */
    String PUBLIC_SEALED_INTERFACE = "public sealed interface";
    /**
     * "public final class"
     */
    String PUBLIC_FINAL_CLASS = "public final class";
    /**
     * "extends"
     */
    String EXTENDS = "extends";
    /**
     * "implements"
     */
    String IMPLEMENTS = "implements";
    /**
     * "permits"
     */
    String PERMITS = "permits";
    /**
     * "{"
     */
    String OPENING_BRACKET = "{";
    /**
     * "}"
     */
    String CLOSING_BRACKET = "}";
    /**
     * "("
     */
    String OPENING_PARENTHESIS = "(";
    /**
     * ")"
     */
    String CLOSING_PARENTHESIS = ")";
    /**
     * ""
     */
    String EMPTY_STRING = "";
    /**
     * "."
     */
    String DOT = ".";
    /**
     * "param"
     */
    String PARAMETER_PREFIX = "param";
    /**
     * "@"
     */
    String TEMP_PLACEHOLDER = "@";
    /**
     * "_"
     */
    String UNDERSCORE = "_";
    /**
     * "FinalClass"
     */
    String FINAL_CLASS_SUFFIX = "FinalCass";
    /**
     * "return"
     */
    String RETURN = "return";
    /**
     * "java.lang.Object"
     */
    String JAVA_LANG_OBJECT = "java.lang.Object";

    /**
     * "SealForProfile"
     */
    String SEAL_FOR_PROFILE = "SealForProfile";
    /**
     * "AddToProfile"
     */
    String ADD_TO_PROFILE = "AddToProfile";
    /**
     * Regex expression to read the string value of the "profile" attribute
     */
    String PROFILE_ATTRIBUTE_REGEX = "profile=\"([^\"]*)\"";
    /**
     * Regex expression to read the string value of the "largeInterface" attribute
     */
    String LARGE_INTERFACE_ATTRIBUTE_REGEX = "largeInterface=\"([^\"]*)\"";
    /**
     * Regex expression to read any attribute value provided within ""
     */
    String ANNOTATION_VALUES_REGEX = "\"([^\"]*)\"";
    /**
     * Regex expression to read attributes information provided using the {@link org.jisel.AddToProfile} annotation.<br>
     * Sample value to be parsed by the regex: @org.jisel.AddToProfile(profile="ActiveWorker", largeInterface="com.bayor.jisel.annotation.client.data.Sociable")
     */
    String ADD_TO_PROFILE_REGEX = "AddToProfile\\((.*?)\\)";
    /**
     * Regex expression to read attributes information provided using the {@link org.jisel.AddToProfiles} annotation.<br>
     * Sample value to be parsed by the regex: @org.jisel.AddToProfiles(profiles={"Student", "Worker"}, largeInterface="com.bayor.jisel.annotation.client.data.Sociable")
     */
    String ADD_TO_PROFILES_REGEX = "AddToProfiles\\((.*?)\\)";

    /**
     * Title of the text report displayed in the logs during compilation.<br>
     * The report is displayed only when an unexpected scenario was encountered (ex: More than 1 top-level parent interfaces found, profile not existing,...)
     */
    String STATUS_REPORT_TITLE = "JISEL GENERATION REPORT";

    /**
     * Displayed only when a "severe" error occurred while a sealed interface file was being generated
     */
    String FILE_GENERATION_ERROR = "Error generating sealed interfaces";
    /**
     * Displayed as a header while listing the successfully generated files
     */
    String FILE_GENERATION_SUCCESS = "Successfully generated";

    /**
     * Fully qualified name of the {@link org.jisel.SealForProfile} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILE = "org.jisel.SealForProfile";
    /**
     * Fully qualified name of the {@link org.jisel.SealForProfiles} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILES = "org.jisel.SealForProfiles";
    /**
     * Fully qualified name of the {@link org.jisel.SealForProfile.SealForProfilez} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILEZ = "org.jisel.SealForProfile.SealForProfilez";
    /**
     * Fully qualified name of the {@link org.jisel.SealForProfiles.SealForProfilezz} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILEZZ = "org.jisel.SealForProfiles.SealForProfilezz";
    /**
     * Fully qualified name of the {@link org.jisel.AddToProfile} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILE = "org.jisel.AddToProfile";
    /**
     * Fully qualified name of the {@link org.jisel.AddToProfiles} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILES = "org.jisel.AddToProfiles";
    /**
     * Fully qualified name of the {@link org.jisel.AddToProfile.AddToProfilez} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILEZ = "org.jisel.AddToProfile.AddToProfilez";
    /**
     * Fully qualified name of the {@link org.jisel.AddToProfiles.AddToProfilezz} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILEZZ = "org.jisel.AddToProfiles.AddToProfilezz";

    /**
     * Default value to use for boolean returned values
     */
    String DEFAULT_BOOLEAN_VALUE = "false";
    /**
     * Default value to use for numeric returned values (int, long, float, double,...)
     */
    String DEFAULT_NUMBER_VALUE = "0";
    /**
     * Default value to use for Object returned values
     */
    String DEFAULT_NULL_VALUE = "null";

    /**
     * Array of methods to exclude while pulling the list of all inherited methods of a class or interface
     */
    String[] METHODS_TO_EXCLUDE = {"getClass", "wait", "notifyAll", "hashCode", "equals", "notify", "toString"};

    /**
     * Message displayed during compilation when 1 or many provided profiles are not found in the provided parent interfaces.
     */
    String ADD_TO_PROFILE_REPORT_MSG = "1 or many provided profiles are not found in the provided parent interfaces. Check your profiles and/or parent interfaces names.";
    /**
     * Message displayed during compilation when more than 1 top-level parent sealed interfaces was encountered based on provided profiles
     */
    String SEAL_FOR_PROFILE_REPORT_MSG = "More than 1 Top-Level Parent Sealed Interfaces will be generated. Check your profiles mapping.";

    /**
     * "Report.txt"
     */
    String JISEL_REPORT_SUFFIX = "Report.txt";

    /**
     * Header displayed above the list of the generated sealed interfaces, in the Jisel Report file
     */
    String JISEL_REPORT_CREATED_SEALED_INTERFACES_HEADER = "Created sealed interfaces:";

    /**
     * Header displayed above the list of the sub-types of the generated sealed interfaces, in the Jisel Report file
     */
    String JISEL_REPORT_CHILDREN_HEADER = "Children:";

    /**
     * Removes all commas from the provided string
     *
     * @param text contains commas as a string separator
     * @return provided text with all commas removed
     */
    static String removeCommaSeparator(final String text) {
        return asList(text.split(COMMA_SEPARATOR)).stream().collect(joining());
    }

    /**
     * Constructs a string based on the provided profile and a large interface {@link Element} instance, according to the naming convention:<br>
     * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br><br>
     *
     * @param profile          name of the profile
     * @param interfaceElement {@link Element} instance of the large interface to be segregated
     * @return a string following Jisel sealed interface naming convention
     */
    default String sealedInterfaceNameConvention(final String profile, final Element interfaceElement) {
        var nameSuffix = removeCommaSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString();
        // any profile name starting w _ (final classes names) or containing a dot (classes annotated with addtoprofile) is returned as is
        return removeCommaSeparator(profile).startsWith(UNDERSCORE) || profile.contains(DOT) ? removeCommaSeparator(profile) : format(
                "%s%s%s",
                SEALED_PREFIX,
                removeCommaSeparator(profile),
                nameSuffix
        );
    }

    /**
     * Constructs a string based on the provided profiles and a large interface {@link Element} instance, according to the naming convention:<br>
     * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br><br>
     *
     * @param profiles         {@link List} of profiles names
     * @param interfaceElement {@link Element} instance of the large interface to be segregated
     * @return a List of string literals following Jisel sealed interface naming convention
     */
    default List<String> sealedInterfaceNameConventionForList(final List<String> profiles, final Element interfaceElement) {
        final UnaryOperator<String> nameSuffix = profile -> removeCommaSeparator(profile).equals(interfaceElement.getSimpleName().toString()) ? EMPTY_STRING : interfaceElement.getSimpleName().toString();
        return profiles.stream()
                .map(profile -> removeCommaSeparator(profile).startsWith(UNDERSCORE) || profile.contains(DOT) ? removeCommaSeparator(profile) : format(
                        "%s%s%s",
                        SEALED_PREFIX,
                        removeCommaSeparator(profile),
                        nameSuffix.apply(profile)
                )).toList();
    }

    /**
     * Constructs the java package name based on an {@link Element} instance of the large interface to be segregated.
     *
     * @param largeInterfaceElement {@link Element} instance of the large interface to be segregated
     * @return the package name if any
     */
    default Optional<String> generatePackageName(final Element largeInterfaceElement) {
        var qualifiedClassName = largeInterfaceElement.toString();
        int lastDot = qualifiedClassName.lastIndexOf('.');
        return lastDot > 0 ? Optional.of(qualifiedClassName.substring(0, lastDot)) : Optional.empty();
    }

    /**
     * Replace all double occurences of whitespace ("  ") into a single whitespace (" ")
     *
     * @param text contains double occurrences of whitespace
     * @return the provided text with all double occurrences of whitespace replaced with a single occurence
     */
    default String removeDoubleSpaceOccurrences(final String text) {
        return text.replace(WHITESPACE + WHITESPACE, WHITESPACE);
    }
}