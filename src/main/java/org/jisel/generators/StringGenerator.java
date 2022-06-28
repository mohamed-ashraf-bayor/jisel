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
package org.jisel.generators;

import org.jisel.annotations.AddTo;
import org.jisel.annotations.Detach;
import org.jisel.annotations.DetachAll;
import org.jisel.annotations.SealFor;
import org.jisel.annotations.TopLevel;
import org.jisel.annotations.UnSeal;

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
// TODO split and reorg this intref
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
    String EQUALS_SIGN = "=";
    /**
     * "Sealed"
     */
    String SEALED_PREFIX = "Sealed";
    /**
     * "UnSealed"
     */
    String UNSEALED = "UnSealed";
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
     * "public interface"
     */
    String PUBLIC_INTERFACE = "public interface";
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
     * "@"
     */
    String AT_SIGN = "@";
    /**
     * "_"
     */
    String UNDERSCORE = "_";
    /**
     * "\""
     */
    String ESCAPED_DOUBLE_QUOTES = "\"";
    /**
     * "\n"
     */
    String NEW_LINE = format("%n");
    /**
     * "\t"
     */
    String TAB = " \t";
    /**
     * "true"
     */
    String TRUE = "true";
    /**
     * "false"
     */
    String FALSE = "false";
    /**
     * "value"
     */
    String VALUE = "value";
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
     * "TopLevel"
     */
    String TOP_LEVEL = "TopLevel";
    /**
     * "UnSeal"
     */
    String UNSEAL = "UnSeal";
    /**
     * "Detach"
     */
    String DETACH = "Detach";
    /**
     * "profile"
     */
    String PROFILE = "profile";
    /**
     * "profiles"
     */
    String PROFILES = "profiles";
    /**
     * Regex expression to read any attribute value provided within ""
     */
    String ANNOTATION_STRING_VALUE_REGEX = "\"([^\"]*)\"";
    /**
     * Regex expression to read any attribute value provided within {}
     */
    String ANNOTATION_ARRAY_VALUE_REGEX = "\\{(.*?)\\}";
    /**
     * "largeInterface"
     */
    String LARGE_INTERFACE = "largeInterface";
    /**
     * Regex expression to read the string value of the "largeInterface" attribute
     */
    String LARGE_INTERFACE_ATTRIBUTE_REGEX = "largeInterface=" + ANNOTATION_STRING_VALUE_REGEX;
    /**
     * Regex expression to read attributes information provided using the {@link AddTo} annotation.<br>
     * Sample value to be parsed by the regex: @org.jisel.annotations.AddTo(profiles={"ActiveWorker"}, largeInterface=Sociable.class)
     */
    String ADD_TO_REGEX = "AddTo\\((.*?)\\)";

    /**
     * Regex expression to read attributes information provided using the {@link Detach} annotation.<br>
     * Sample value to be parsed by the regex: @org.jisel.annotations.Detach(profile="(toplevel)", superInterfaces={com.bayor.Sociable.class, com.bayor.Processor.class}, ...)
     */
    String DETACH_REGEX = "Detach\\((.*?)\\)";

    String DETACH_PROFILE = "profile";
    String DETACH_RENAME = "rename";
    String DETACH_SUPERINTERFACES = "superInterfaces";
    String DETACH_FIRSTSUPERINTERFACEGENERICS = "firstSuperInterfaceGenerics";
    String DETACH_SECONDSUPERINTERFACEGENERICS = "secondSuperInterfaceGenerics";
    String DETACH_THIRDSUPERINTERFACEGENERICS = "thirdSuperInterfaceGenerics";
    String DETACH_APPLYANNOTATIONS = "applyAnnotations";
    
    String DETACH_PROFILE_REGEX = "profile=" + ANNOTATION_STRING_VALUE_REGEX;
    String DETACH_RENAME_REGEX = "rename=" + ANNOTATION_STRING_VALUE_REGEX;
    String DETACH_SUPERINTERFACES_REGEX = "superInterfaces=" + ANNOTATION_ARRAY_VALUE_REGEX;
    String DETACH_FIRSTSUPERINTERFACEGENERICS_REGEX = "firstSuperInterfaceGenerics=" + ANNOTATION_ARRAY_VALUE_REGEX;
    String DETACH_SECONDSUPERINTERFACEGENERICS_REGEX = "secondSuperInterfaceGenerics=" + ANNOTATION_ARRAY_VALUE_REGEX;
    String DETACH_THIRDSUPERINTERFACEGENERICS_REGEX = "thirdSuperInterfaceGenerics=" + ANNOTATION_ARRAY_VALUE_REGEX;
    String DETACH_APPLYANNOTATIONS_REGEX = "applyAnnotations=" + ANNOTATION_ARRAY_VALUE_REGEX;

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
     * Fully qualified name of the {@link UnSeal} annotation
     */
    String ORG_JISEL_UNSEAL = "org.jisel.annotations.UnSeal";
    /**
     * Fully qualified name of the {@link Detach} annotation
     */
    String ORG_JISEL_DETACH = "org.jisel.annotations.Detach";
    /**
     * Fully qualified name of the {@link DetachAll} annotation
     */
    String ORG_JISEL_DETACHALL = "org.jisel.annotations.DetachAll";
    /**
     * Fully qualified name of the {@link Detach.Detachs} annotation
     */
    String ORG_JISEL_DETACHS = "org.jisel.annotations.Detach.Detachs";
    /**
     * Fully qualified name of the {@link AddTo} annotation
     */
    String ORG_JISEL_ADD_TO = "org.jisel.annotations.AddTo";
    /**
     * Fully qualified name of the {@link AddTo.AddTos} annotation
     */
    String ORG_JISEL_ADD_TOS = "org.jisel.annotations.AddTo.AddTos";
    /**
     * Fully qualified name of the {@link TopLevel} annotation
     */
    String ORG_JISEL_TOP_LEVEL = "org.jisel.annotations.TopLevel";

    String JISEL_ANNOTATIONS_PACKAGE = "org.jisel.annotations";

    /**
     * Default value to use for boolean returned values
     */
    String DEFAULT_BOOLEAN_VALUE = FALSE;
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
    String ADD_TO_REPORT_MSG = "1 or many provided profiles are not found in the provided parent interfaces. " +
            "Check your profiles and/or parent interfaces names. " +
            "Also check the use of @TopLevel in the provided large interfaces.";
    /**
     * Message displayed during compilation when &#64;TopLevel is not found within the provided large interface
     */
    String TOP_LEVEL_REPORT_MSG = "@TopLevel annotation not found. Check your mappings.";

    /**
     * Message displayed during compilation when &#64;TopLevel is not found within the provided large interface
     */
    String UNSEAL_REPORT_MSG = "@UnSeal annotation applied on interfaces not making use of @TopLevel. Check your mappings.";

    /**
     * "Report.txt"
     */
    String JISEL_REPORT_SUFFIX = "Report.txt";

    /**
     * Header displayed above the list of the generated sealed interfaces, in the Jisel Report file
     */
    String JISEL_REPORT_CREATED_SEALED_INTERFACES_HEADER = "Created sealed interfaces:";

    /**
     * Header displayed above the list of the generated sealed interfaces, in the Jisel Report file
     */
    String JISEL_REPORT_CREATED_UNSEALED_INTERFACES_HEADER = "Created unsealed interfaces:";

    /**
     * Header displayed above the list of the sub-types of the generated sealed interfaces, in the Jisel Report file
     */
    String JISEL_REPORT_CHILDREN_HEADER = "Children:";

    // TODO updt all jdocs

    String JISEL_KEYWORD_ALL = "(all)";

    String JISEL_KEYWORD_TOPLEVEL = "(toplevel)";

    String JISEL_KEYWORD_TOPLEVEL_TRANSFORMED = "_toplevel_";

    /**
     * Removes all commas from the provided string
     *
     * @param text contains commas as a string separator
     * @return provided text with all commas removed
     */
    static String removeCommaSeparator(String text) {
        return asList(text.split(COMMA_SEPARATOR)).stream().collect(joining());
    }

    /**
     * Replace all double occurences of whitespace ("  ") into a single whitespace (" ")
     *
     * @param text contains double occurrences of whitespace
     * @return the provided text with all double occurrences of whitespace replaced with a single occurence
     */
    static String removeDoubleSpaceOccurrences(String text) {
        return text.replace(WHITESPACE + WHITESPACE, WHITESPACE);
    }

    /**
     * Constructs a string based on the provided profile and a large interface {@link Element} instance, according to the naming convention:<br>
     * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br><br>
     *
     * @param profile          name of the profile
     * @param interfaceElement {@link Element} instance of the large interface to be segregated
     * @return a string following Jisel sealed interface naming convention
     */
    default String sealedInterfaceNameConvention(String profile, Element interfaceElement) {
        var nameSuffixFunc = removeCommaSeparator(profile).equals(interfaceElement.getSimpleName().toString())
                ? EMPTY_STRING
                : interfaceElement.getSimpleName().toString();
        // any profile name starting w _ (final classes names) or containing a dot (classes annotated with @Addto) is returned as is
        return removeCommaSeparator(profile).startsWith(UNDERSCORE) || profile.contains(DOT)
                ? removeCommaSeparator(profile)
                : format(
                "%s%s%s",
                SEALED_PREFIX,
                removeCommaSeparator(profile),
                nameSuffixFunc
        );
    }

    /**
     * Constructs a string based on the provided profile and a large interface {@link Element} instance, according to the naming convention:<br>
     * <b>&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br><br>
     *
     * @param profile          name of the profile
     * @param interfaceElement {@link Element} instance of the large interface to be segregated
     * @return a string following Jisel sealed interface naming convention
     */
    default String unSealedInterfaceNameConvention(String profile, Element interfaceElement) {
        return sealedInterfaceNameConvention(profile, interfaceElement).substring(SEALED_PREFIX.length());
    }

    /**
     * Constructs a string made of the qualified name of the class without the latest occurrence of ".class". <br>
     * If the provided name doesn't end with ".class", it is returned as is
     *
     * @param qualifiedName qualified name of the class or interface
     * @return qualified name of the class without the latest occurrence of ".class"
     */
    default String removeDotClass(String qualifiedName) {
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
     * @return string containing the largeInterface attribute value with
     */
    default String addQuotesToLargeInterfaceValue(String largeInterfaceAttributeRawString) {
        return largeInterfaceAttributeRawString
                .replace(LARGE_INTERFACE + EQUALS_SIGN, LARGE_INTERFACE + EQUALS_SIGN + ESCAPED_DOUBLE_QUOTES)
                .replace(DOT_CLASS, ESCAPED_DOUBLE_QUOTES);
    }

    /**
     * Constructs a string based on the provided profiles and a large interface {@link Element} instance, according to the naming convention:<br>
     * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br><br>
     *
     * @param profiles         {@link List} of profiles names
     * @param interfaceElement {@link Element} instance of the large interface to be segregated
     * @return a List of string literals following Jisel sealed interface naming convention
     */
    default List<String> sealedInterfaceNameConventionForList(List<String> profiles, Element interfaceElement) {
        final UnaryOperator<String> nameSuffixFunc = profile ->
                removeCommaSeparator(profile).equals(interfaceElement.getSimpleName().toString())
                        ? EMPTY_STRING
                        : interfaceElement.getSimpleName().toString();
        return profiles.stream()
                .map(profile ->
                        removeCommaSeparator(profile).startsWith(UNDERSCORE) || profile.contains(DOT)
                                ? removeCommaSeparator(profile)
                                : format(
                                "%s%s%s",
                                SEALED_PREFIX,
                                removeCommaSeparator(profile),
                                nameSuffixFunc.apply(profile)
                        )).toList();
    }

    /**
     * Constructs the java package name based on an {@link Element} instance of the large interface to be segregated.
     *
     * @param largeInterfaceElement {@link Element} instance of the large interface to be segregated
     * @return the package name if any
     */
    default Optional<String> generatePackageName(Element largeInterfaceElement) {
        var qualifiedClassName = largeInterfaceElement.toString();
        int lastDot = qualifiedClassName.lastIndexOf(DOT);
        return lastDot > 0 ? Optional.of(qualifiedClassName.substring(0, lastDot)) : Optional.empty();
    }
}