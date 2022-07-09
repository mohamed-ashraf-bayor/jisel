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
import static java.util.Arrays.stream;

/**
 * A bunch of String literals and commonly needed string handling functions
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
    String EQUALS_SIGN = "=";

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
     * "<"
     */
    String INF_SIGN = "<";

    /**
     * ">"
     */
    String SUP_SIGN = ">";

    /**
     * Placeholder string to be later replaced with another string
     */
    String TMP_PLACEHOLDER = UNDERSCORE + UNDERSCORE;

    /**
     * "\""
     */
    String DOUBLE_QUOTES = "\"";

    /**
     * "\""
     */
    String ESCAPED_DOUBLE_QUOTES = "\\\"";

    /**
     * "Sealed"
     */
    String SEALED_PREFIX = "Sealed";

    /**
     * "UnSealed"
     */
    String UNSEALED = "UnSealed";

    /**
     * "Detached"
     */
    String DETACHED = "Detached";

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
    String DOT_CLASS = DOT + CLASS;

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
     * "\n"
     */
    String NEW_LINE = format("%n");

    /**
     * "\\\\n" (escaped "\n")
     */
    String ESCAPED_NEW_LINE = "\\\\n";

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
     * "Detach"
     */
    String DETACH_ALL = "DetachAll";

    /**
     * "all"
     */
    String ALL = "all";

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
     * "profile" attribute used in @Detach annotation
     */
    String DETACH_PROFILE = PROFILE;

    /**
     * "rename" attribute used in @Detach annotation
     */
    String DETACH_RENAME = "rename";

    /**
     * "superInterfaces" attribute used in @Detach annotation
     */
    String DETACH_SUPERINTERFACES = "superInterfaces";

    /**
     * "firstSuperInterfaceGenerics" attribute used in @Detach annotation
     */
    String DETACH_FIRST_SUPERINTERFACE_GENERICS = "firstSuperInterfaceGenerics";

    /**
     * "secondSuperInterfaceGenerics" attribute used in @Detach annotation
     */
    String DETACH_SECOND_SUPERINTERFACE_GENERICS = "secondSuperInterfaceGenerics";

    /**
     * "thirdSuperInterfaceGenerics" attribute used in @Detach annotation
     */
    String DETACH_THIRD_SUPERINTERFACE_GENERICS = "thirdSuperInterfaceGenerics";

    /**
     * "applyAnnotations" attribute used in @Detach annotation
     */
    String DETACH_APPLYANNOTATIONS = "applyAnnotations";

    /**
     * Map key used to store the collection of abstract methods for the detached interface
     */
    String DETACH_METHODS = "methods";

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
     * Message displayed during compilation when 1 or many provided profiles are not found in the provided parent interfaces.
     */
    String ADD_TO_REPORT_PROFILES_NOT_FOUND_MSG = "1 or many provided profiles are not found in the provided parent interfaces. " +
            "Check your profiles and/or parent interfaces names. " +
            "Also check the use of @TopLevel in the provided large interfaces. ";

    /**
     * Message displayed during compilation when &#64;TopLevel is not found within the provided large interface
     */
    String TOP_LEVEL_REPORT_NOT_FOUND_MSG = "@TopLevel annotation not found. Check your mappings. ";

    /**
     * Message displayed during compilation when there might be an issue with &#64;TopLevel and/or &#64;SealFor mappings
     */
    String TOP_LEVEL_AND_SEAL_FOR_REPORT_CHECK_MSG = "Check your @TopLevel and/or @SealFor mappings. ";

    /**
     * Message displayed during compilation when &#64;TopLevel is not found within the provided large interface
     */
    String UNSEAL_REPORT_NO_TOPLEVEL_MSG = "@UnSeal annotation applied on interfaces not making use of @TopLevel. Check your mappings. ";

    /**
     * Message displayed during compilation when the provided profiles to detach are not found in the large interface &#64;SealFor mappings
     */
    String DETACH_REPORT_PROFILES_NOT_FOUND_MSG = "1 or many provided profiles are not found in the @SealFor mappings. ";

    /**
     * Keyword used internally by Jisel to indicate that @DetachAll was used on a large interface
     */
    String JISEL_KEYWORD_ALL = "(all)";

    /**
     * Keyword which may be used to indicate to "detach" the toplevel profile
     */
    String JISEL_KEYWORD_TOPLEVEL = "(toplevel)";

    /**
     * Regex expression to identify the use of (toplevel) case-insensitive
     */
    String JISEL_KEYWORD_TOPLEVEL_CI_REGEX = "(?i)(toplevel)";

    /**
     * Keyword used intenally by Jisel during annotation information parsing to replace "(toplevel)"
     */
    String JISEL_KEYWORD_TOPLEVEL_REPLACEMENT = "_toplevel_";

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
    static String sealedInterfaceNameConvention(String profile, Element interfaceElement) {
        var nameSuffixFunc = profile.equals(interfaceElement.getSimpleName().toString())
                ? EMPTY_STRING
                : interfaceElement.getSimpleName().toString();
        // any profile name starting w _ (final classes names) or containing a dot (classes annotated with @Addto) is returned as is
        return profile.startsWith(UNDERSCORE) || profile.contains(DOT)
                ? profile
                : format(
                "%s%s%s",
                SEALED_PREFIX,
                profile,
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
    static String unSealedInterfaceNameConvention(String profile, Element interfaceElement) {
        return sealedInterfaceNameConvention(profile, interfaceElement).substring(SEALED_PREFIX.length());
    }

    /**
     * Constructs a string based on the provided profiles and a large interface {@link Element} instance, according to the naming convention:<br>
     * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br><br>
     *
     * @param profiles         {@link List} of profiles names
     * @param interfaceElement {@link Element} instance of the large interface to be segregated
     * @return a List of string literals following Jisel sealed interface naming convention
     */
    static List<String> sealedInterfaceNameConventionForList(List<String> profiles, Element interfaceElement) {
        UnaryOperator<String> nameSuffixFunc = profile ->
                profile.equals(interfaceElement.getSimpleName().toString())
                        ? EMPTY_STRING
                        : interfaceElement.getSimpleName().toString();
        return profiles.stream()
                .map(profile ->
                        profile.startsWith(UNDERSCORE) || profile.contains(DOT)
                                ? profile
                                : format(
                                "%s%s%s",
                                SEALED_PREFIX,
                                profile,
                                nameSuffixFunc.apply(profile)
                        ))
                .toList();
    }

    /**
     * Constructs a string made of the qualified name of the class without the latest occurrence of ".class". <br>
     * If the provided name doesn't end with ".class", it is returned as is
     *
     * @param qualifiedName qualified name of the class or interface
     * @return qualified name of the class without the latest occurrence of ".class"
     */
    static String removeDotClass(String qualifiedName) {
        return qualifiedName.contains(DOT_CLASS)
                ? qualifiedName.substring(0, qualifiedName.lastIndexOf(DOT_CLASS))
                : qualifiedName;
    }

    /**
     * Constructs the java package name based on an {@link Element} instance of the large interface to be segregated.
     *
     * @param largeInterfaceElement {@link Element} instance of the large interface to be segregated
     * @return the package name if any
     */
    static Optional<String> generatePackageName(Element largeInterfaceElement) {
        return extractPackageName(largeInterfaceElement.toString());
    }

    /**
     * Extracts package name from the provided qualified class name
     *
     * @param qualifiedClassName qualified name of the class or interface
     * @return the package name extracted from the provided qualified class or interface name
     */
    static Optional<String> extractPackageName(String qualifiedClassName) {
        int lastDot = qualifiedClassName.lastIndexOf(DOT);
        return lastDot > 0 ? Optional.of(qualifiedClassName.substring(0, lastDot)) : Optional.empty();
    }

    /**
     * Extracts the simple name from the provided qualified class or interface name
     *
     * @param qualifiedClassName qualified name of the class or interface
     * @return the simple name extracted from the provided qualified class or interface name
     */
    static Optional<String> extractSimpleName(String qualifiedClassName) {
        int lastDot = qualifiedClassName.lastIndexOf(DOT);
        return lastDot > -1 ? Optional.of(qualifiedClassName.substring(lastDot + 1)) : Optional.empty();
    }

    /**
     * Checks whether the provided profile name is one of the Jisel keywords
     *
     * @param profile the profile name
     * @return true if the provided profile name is one of the Jisel keywords
     */
    static boolean isJiselKeyword(String profile) {
        var keywordsArray = new String[]{JISEL_KEYWORD_ALL, JISEL_KEYWORD_TOPLEVEL, JISEL_KEYWORD_TOPLEVEL_REPLACEMENT};
        return stream(keywordsArray).anyMatch(keyword -> keyword.equalsIgnoreCase(profile));
    }

    /**
     * Removes strings from the beginning and end of the provided string
     *
     * @param providedString      the string to remove trailing strings from
     * @param leftTrailingString  string to be removed from the beginning of the provided string
     * @param rightTrailingString string to be removed from the end of the provided string
     * @return the provided string without the left and right trailing strings
     */
    static String removeTrailingStrings(String providedString, String leftTrailingString, String rightTrailingString) {
        UnaryOperator<String> removeLQuoteOp = string -> string.startsWith(leftTrailingString) ? string.substring(1) : string;
        UnaryOperator<String> removeRQuoteOp = string -> string.endsWith(rightTrailingString) ? string.substring(0, string.strip().length() - 1) : string;
        var strippedString = providedString.strip();
        if (!strippedString.isBlank()) {
            return removeLQuoteOp.andThen(removeRQuoteOp).apply(strippedString);
        }
        return strippedString;
    }
}