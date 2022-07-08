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
package org.jisel.handlers;

import org.jisel.annotations.AddTo;
import org.jisel.handlers.impl.AddToHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.jisel.generators.StringGenerator.ADD_TO_REGEX;
import static org.jisel.generators.StringGenerator.ANNOTATION_STRING_VALUE_REGEX;
import static org.jisel.generators.StringGenerator.CLOSING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generators.StringGenerator.DOT_CLASS;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.EQUALS_SIGN;
import static org.jisel.generators.StringGenerator.DOUBLE_QUOTES;
import static org.jisel.generators.StringGenerator.LARGE_INTERFACE;
import static org.jisel.generators.StringGenerator.LARGE_INTERFACE_ATTRIBUTE_REGEX;
import static org.jisel.generators.StringGenerator.OPENING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.PROFILES;
import static org.jisel.generators.StringGenerator.removeDotClass;

public abstract sealed class AbstractSealedAddToHandler implements JiselAnnotationHandler permits AddToHandler {

    protected final ProcessingEnvironment processingEnvironment;

    protected AbstractSealedAddToHandler(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    /**
     * For a specified class or interface annotated with &#64;{@link AddTo}, constructs a Map storing a Set of all the provided
     * profiles names (as the Map value) for each one of the large interfaces names (as the Map key) provided through &#64;AddTo.
     *
     * @param annotatedClassOrInterface {@link Element} instance representing the annotated class or interface
     * @return a Map storing a Set of all the provided profiles names (as the Map value) for each one of the large interfaces names (as the Map key)
     */
    protected Map<String, Set<String>> buildAddToProvidedProfilesMap(Element annotatedClassOrInterface) {
        var providedProfilesMap = new HashMap<String, Set<String>>();
        var annotationRawValueAsString = annotatedClassOrInterface.getAnnotationMirrors().stream()
                .map(Object::toString)
                .collect(joining(COMMA_SEPARATOR));
        // sample values for annotationRawValueAsString:
        // @org.jisel.annotations.AddTo(profiles={"Student", "Worker"}, largeInterface=com.bayor.jisel.annotation.client.data.Sociable.class),@org.jisel.annotations.AddTo(largeInterface=com.bayor.jisel.annotation.client.data.Sociable.class)
        var addToMatcher = Pattern.compile(ADD_TO_REGEX).matcher(annotationRawValueAsString);
        while (addToMatcher.find()) {
            var attributesWithValues = addToMatcher.group(1).strip(); // profiles={"ActiveWorker", "Student"}, largeInterface=com.bayor.jisel.annotation.client.data.Sociable.class
            var profilesSet = new HashSet<String>();
            if (attributesWithValues.contains(PROFILES + EQUALS_SIGN)) {
                var commaSeparatedProfiles = attributesWithValues.substring(attributesWithValues.indexOf(OPENING_CURLY_BRACE) + 1, attributesWithValues.indexOf(CLOSING_CURLY_BRACE));
                var profilesNamesMatcher = Pattern.compile(ANNOTATION_STRING_VALUE_REGEX).matcher(commaSeparatedProfiles);
                while (profilesNamesMatcher.find()) {
                    profilesSet.add(profilesNamesMatcher.group(1).strip());
                }
            }
            updateProvidedProfilesMapBasedOnProfilesSet(providedProfilesMap, unmodifiableSet(profilesSet), addQuotesToLargeInterfaceValue(attributesWithValues));
        }
        return providedProfilesMap;
    }

    /**
     * Adds double quotes to the largeInterface attribute value and removes the ".class" string.<br>
     * To be called while processing largeInterface attribute values provided though &#64;{@link AddTo}
     *
     * @param largeInterfaceAttributeRawString the toString representation of the provided largeInterface attribute value<br>
     *                                         ex: largeInterface=com.bayor.Drivable.class
     * @return string containing the largeInterface attribute value with
     */
    protected String addQuotesToLargeInterfaceValue(String largeInterfaceAttributeRawString) {
        return largeInterfaceAttributeRawString
                .replace(LARGE_INTERFACE + EQUALS_SIGN, LARGE_INTERFACE + EQUALS_SIGN + DOUBLE_QUOTES)
                .replace(DOT_CLASS, DOUBLE_QUOTES);
    }

    private void updateProvidedProfilesMapBasedOnProfilesSet(Map<String, Set<String>> providedProfilesMap, Set<String> profilesSet, String attributesWithValues) {
        var largeInterfaceAttributeMatcher = Pattern.compile(LARGE_INTERFACE_ATTRIBUTE_REGEX).matcher(attributesWithValues);
        providedProfilesMap.merge(
                largeInterfaceAttributeMatcher.find() ? removeDotClass(largeInterfaceAttributeMatcher.group(1).strip()) : EMPTY_STRING,
                profilesSet.isEmpty() ? new HashSet<>(Set.of(EMPTY_STRING)) : profilesSet,
                (currentSet, newSet) -> concat(currentSet.stream(), newSet.stream()).collect(toSet())
        );
    }
}