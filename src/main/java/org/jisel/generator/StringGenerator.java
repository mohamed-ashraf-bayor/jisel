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

import org.jisel.annotations.AddTo;
import org.jisel.annotations.AddToProfile;
import org.jisel.annotations.AddToProfiles;
import org.jisel.annotations.SealFor;
import org.jisel.annotations.SealForProfile;
import org.jisel.annotations.SealForProfiles;
import org.jisel.annotations.TopLevel;

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
     * "=" equals
     */
    String EQUALS = "=";
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
     * ".class"
     */
    String DOT_CLASS = ".class";
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
    String OPENING_CURLY_BRACE = "{";
    /**
     * "}"
     */
    String CLOSING_CURLY_BRACE = "}";
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
     * "\""
     */
    String ESCAPED_DOUBLE_QUOTES = "\"";
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
     * "SealFor"
     */
    String SEAL_FOR = "SealFor";
    /**
     * "AddTo"
     */
    String ADD_TO = "AddTo";
    /**
     * "SealForProfile"
     */
    String SEAL_FOR_PROFILE = "SealForProfile";
    /**
     * "AddToProfile"
     */
    String ADD_TO_PROFILE = "AddToProfile";
    /**
     * "profile"
     */
    String PROFILE = "profile";
    /**
     * Regex expression to read the string value of the "profile" attribute
     */
    String PROFILE_ATTRIBUTE_REGEX = "profile=\"([^\"]*)\"";
    /**
     * "profiles"
     */
    String PROFILES = "profiles";
    /**
     * Regex expression to read the string value of the "profile" attribute
     */
    String PROFILES_ATTRIBUTE_REGEX = "profiles=\"([^\"]*)\"";
    /**
     * "largeInterface"
     */
    String LARGE_INTERFACE = "largeInterface";
    /**
     * Regex expression to read the string value of the "largeInterface" attribute
     */
    String LARGE_INTERFACE_ATTRIBUTE_REGEX = "largeInterface=\"([^\"]*)\"";
    /**
     * Regex expression to read any attribute value provided within ""
     */
    String ANNOTATION_VALUES_REGEX = "\"([^\"]*)\"";
    /**
     * Regex expression to read attributes information provided using the {@link AddTo} annotation.<br>
     * Sample value to be parsed by the regex: @org.jisel.annotations.AddTo(profiles={"ActiveWorker"}, largeInterface=Sociable.class)
     */
    String ADD_TO_REGEX = "AddTo\\((.*?)\\)";
    /**
     * Regex expression to read attributes information provided using the {@link AddToProfile} annotation.<br>
     * Sample value to be parsed by the regex: @org.jisel.annotations.AddToProfile(profile="ActiveWorker", largeInterface="com.bayor.jisel.annotation.client.data.Sociable")
     */
    String ADD_TO_PROFILE_REGEX = "AddToProfile\\((.*?)\\)";
    /**
     * Regex expression to read attributes information provided using the {@link AddToProfiles} annotation.<br>
     * Sample value to be parsed by the regex: @org.jisel.annotations.AddToProfiles(profiles={"Student", "Worker"}, largeInterface="com.bayor.jisel.annotation.client.data.Sociable")
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
     * Fully qualified name of the {@link SealFor} annotation
     */
    String ORG_JISEL_SEAL_FOR = "org.jisel.annotations.SealFor";
    /**
     * Fully qualified name of the {@link SealFor.SealFors} annotation
     */
    String ORG_JISEL_SEAL_FORS = "org.jisel.annotations.SealFor.SealFors";
    /**
     * Fully qualified name of the {@link SealForProfile} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILE = "org.jisel.annotations.SealForProfile";
    /**
     * Fully qualified name of the {@link SealForProfiles} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILES = "org.jisel.annotations.SealForProfiles";
    /**
     * Fully qualified name of the {@link SealForProfile.SealForProfilez} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILEZ = "org.jisel.annotations.SealForProfile.SealForProfilez";
    /**
     * Fully qualified name of the {@link SealForProfiles.SealForProfilezz} annotation
     */
    String ORG_JISEL_SEAL_FOR_PROFILEZZ = "org.jisel.annotations.SealForProfiles.SealForProfilezz";
    /**
     * Fully qualified name of the {@link AddTo} annotation
     */
    String ORG_JISEL_ADD_TO = "org.jisel.annotations.AddTo";
    /**
     * Fully qualified name of the {@link AddTo.AddTos} annotation
     */
    String ORG_JISEL_ADD_TOS = "org.jisel.annotations.AddTo.AddTos";
    /**
     * Fully qualified name of the {@link AddToProfile} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILE = "org.jisel.annotations.AddToProfile";
    /**
     * Fully qualified name of the {@link AddToProfiles} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILES = "org.jisel.annotations.AddToProfiles";
    /**
     * Fully qualified name of the {@link AddToProfile.AddToProfilez} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILEZ = "org.jisel.annotations.AddToProfile.AddToProfilez";
    /**
     * Fully qualified name of the {@link AddToProfiles.AddToProfilezz} annotation
     */
    String ORG_JISEL_ADD_TO_PROFILEZZ = "org.jisel.annotations.AddToProfiles.AddToProfilezz";
    /**
     * Fully qualified name of the {@link TopLevel} annotation
     */
    String ORG_JISEL_TOP_LEVEL = "org.jisel.annotations.TopLevel";

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
    String ADD_TO_REPORT_MSG = "1 or many provided profiles are not found in the provided parent interfaces. Check your profiles and/or parent interfaces names.";
    /**
     * Message displayed during compilation when more than 1 top-level parent sealed interfaces was encountered based on provided profiles
     */
    String SEAL_FOR_REPORT_MSG = "More than 1 Top-Level Parent Sealed Interfaces will be generated. Check your profiles mapping.";

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
     * Constructs a string made of the qualified name of the class without the latest occurrence of ".class". <br>
     * If the provided name doesn't end with ".class", it is returned as is
     *
     * @param qualifiedName qualified name of the class or interface
     * @return qualified name of the class without the latest occurrence of ".class"
     */
    default String removeDotClass(final String qualifiedName) {
        return qualifiedName.contains(DOT_CLASS)
                ? qualifiedName.substring(0, qualifiedName.lastIndexOf(DOT_CLASS))
                : qualifiedName;
    }

    /**
     * Adds double quotes to the largeInterface attribute value and removes the ".class" string.<br>
     * To be called while processing largeInterface attribute values provided though &#64;{@link AddTo}
     *
     * @param largeInterfaceAttributeRawString the toString representation of the provided largeInterface attribute value<br>
     *                                         ex: largeInterface=com.bayor.Drivable.class
     * @return string containg the largeInterface attribute value with
     */
    default String addQuotesToLargeInterfaceValue(final String largeInterfaceAttributeRawString) {
        return largeInterfaceAttributeRawString.replace(LARGE_INTERFACE + EQUALS, LARGE_INTERFACE + EQUALS + ESCAPED_DOUBLE_QUOTES).replace(DOT_CLASS, ESCAPED_DOUBLE_QUOTES);
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