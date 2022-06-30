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
import org.jisel.annotations.SealFor;
import org.jisel.handlers.impl.AddToHandler;
import org.jisel.handlers.impl.DetachHandler;
import org.jisel.handlers.impl.SealForHandler;
import org.jisel.handlers.impl.TopLevelHandler;
import org.jisel.handlers.impl.UnSealHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.jisel.generators.StringGenerator.ADD_TO_REGEX;
import static org.jisel.generators.StringGenerator.ANNOTATION_STRING_VALUE_REGEX;
import static org.jisel.generators.StringGenerator.CLOSING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.CLOSING_PARENTHESIS;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generators.StringGenerator.DETACH_APPLYANNOTATIONS;
import static org.jisel.generators.StringGenerator.DETACH_APPLYANNOTATIONS_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_FIRSTSUPERINTERFACEGENERICS;
import static org.jisel.generators.StringGenerator.DETACH_FIRSTSUPERINTERFACEGENERICS_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_PROFILE;
import static org.jisel.generators.StringGenerator.DETACH_PROFILE_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_RENAME;
import static org.jisel.generators.StringGenerator.DETACH_RENAME_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_REPORT_MSG;
import static org.jisel.generators.StringGenerator.DETACH_SECONDSUPERINTERFACEGENERICS;
import static org.jisel.generators.StringGenerator.DETACH_SECONDSUPERINTERFACEGENERICS_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_SUPERINTERFACES;
import static org.jisel.generators.StringGenerator.DETACH_SUPERINTERFACES_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_THIRDSUPERINTERFACEGENERICS;
import static org.jisel.generators.StringGenerator.DETACH_THIRDSUPERINTERFACEGENERICS_REGEX;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.EQUALS_SIGN;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_ALL;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL_TRANSFORMED;
import static org.jisel.generators.StringGenerator.LARGE_INTERFACE_ATTRIBUTE_REGEX;
import static org.jisel.generators.StringGenerator.OPENING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.OPENING_PARENTHESIS;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACH;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACHALL;
import static org.jisel.generators.StringGenerator.PROFILE;
import static org.jisel.generators.StringGenerator.PROFILES;
import static org.jisel.generators.StringGenerator.TOP_LEVEL_AND_SEAL_FOR_REPORT_MSG;
import static org.jisel.generators.StringGenerator.VALUE;
import static org.jisel.generators.StringGenerator.addQuotesToLargeInterfaceValue;
import static org.jisel.generators.StringGenerator.annotationAttributeValueWithoutQuotes;
import static org.jisel.generators.StringGenerator.isJiselKeyword;
import static org.jisel.generators.StringGenerator.removeDotClass;

/**
 * Exposes contract to fulfill by any class handling all elements annotated with Jisel annotations
 */
public sealed interface JiselAnnotationHandler
        permits SealForHandler, AddToHandler, TopLevelHandler, UnSealHandler, DetachHandler, AnnotationInfoCollectionHandler, ParentChildInheritanceHandler {

    /**
     * Reads values of all attributes provided through the use of Jisel annotations and populates the provided Map arguments
     *
     * @param processingEnv                                {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param allAnnotatedElements                         {@link Set} of {@link Element} instances representing all classes annotated with Jisel annotations
     * @param sealedInterfacesToGenerateByLargeInterface   Map containing information about the sealed interfaces to be generated.
     *                                                     To be populated and/or modified if needed. The key represents the Element instance of
     *                                                     each one of the large interfaces to be segregated, while the associated value is
     *                                                     a Map of profile name as the key and a Set of Element instances as the value.
     *                                                     The Element instances represent each one of the abstract methods to be
     *                                                     added to the generated sealed interface corresponding to a profile.
     * @param sealedInterfacesPermitsByLargeInterface      Map containing information about the subtypes permitted by each one of the sealed interfaces to be generated.
     *                                                     To be populated and/or modified if needed. The key represents the Element instance of
     *                                                     each one of the large interfaces to be segregated, while the associated value is
     *                                                     a Map of profile name as the key and a List of profiles names as the value.
     * @param detachedInterfacesToGenerateByLargeInterface // TODO map content: Set<Element> methodsSet | String rename() | Class<?>[] superInterfaces | Class<?>[] firstSuperInterfaceGenerics | Class<?>[] secondSuperInterfaceGenerics | Class<?>[] thirdSuperInterfaceGenerics | String[] applyAnnotations
     * @return a status report as a string value for each one of the large interfaces to be segregated
     */
    Map<Element, String> handleAnnotatedElements(ProcessingEnvironment processingEnv,
                                                 Set<Element> allAnnotatedElements,
                                                 Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                 Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                 Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface);

    /**
     * For a specified large interface abstract method annotated with #64;{@link SealFor}, constructs a Set storing
     * all the provided profiles names
     *
     * @param processingEnv   {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param annotatedMethod {@link Element} instance representing the annotated method of the large interface
     * @return a Set storing all the provided profiles names
     */
    default Set<String> buildSealForProvidedProfilesSet(ProcessingEnvironment processingEnv, Element annotatedMethod) {
        var providedProfilesSet = new HashSet<String>();
        processingEnv.getElementUtils().getAllAnnotationMirrors(annotatedMethod).stream()
                .flatMap(annotationMirror -> annotationMirror.getElementValues().entrySet().stream())
                .map(entry -> entry.getValue().toString())
                .forEach(annotationRawValueAsString -> {
                    // sample values for annotationRawValueAsString:
                    // single value: "profile1name"
                    // multiple: @org.jisel.annotations.SealFor("profile2name"), @org.jisel.annotations.SealFor("profile3name"),...
                    var matcher = Pattern.compile(ANNOTATION_STRING_VALUE_REGEX).matcher(annotationRawValueAsString);
                    while (matcher.find()) {
                        var profile = matcher.group(1).strip();
                        if (profile.isBlank()) { // blank profiles ignored
                            continue;
                        }
                        providedProfilesSet.add(profile);
                    }
                });
        return providedProfilesSet;
    }

    /**
     * For a specified class or interface annotated with &#64;{@link AddTo}, constructs a Map storing a Set of all the provided
     * profiles names (as the Map value) for each one of the large interfaces names (as the Map key) provided through &#64;AddTo.
     *
     * @param annotatedClassOrInterface {@link Element} instance representing the annotated class or interface
     * @return a Map storing a Set of all the provided profiles names (as the Map value) for each one of the large interfaces names (as the Map key)
     */
    default Map<String, Set<String>> buildAddToProvidedProfilesMap(Element annotatedClassOrInterface) {
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

    private void updateProvidedProfilesMapBasedOnProfilesSet(Map<String, Set<String>> providedProfilesMap, Set<String> profilesSet, String attributesWithValues) {
        var largeInterfaceAttributeMatcher = Pattern.compile(LARGE_INTERFACE_ATTRIBUTE_REGEX).matcher(attributesWithValues);
        providedProfilesMap.merge(
                largeInterfaceAttributeMatcher.find() ? removeDotClass(largeInterfaceAttributeMatcher.group(1).strip()) : EMPTY_STRING,
                profilesSet.isEmpty() ? new HashSet<>(Set.of(EMPTY_STRING)) : profilesSet,
                (currentSet, newSet) -> concat(currentSet.stream(), newSet.stream()).collect(toSet())
        );
    }

    /**
     * // TODO jdoc...
     *
     * @param detachedInterfacesToGenerateByLargeInterface
     * @param largeInterfaceElement
     */
    default String handleDetachAllAnnotation(Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
                                             Element largeInterfaceElement,
                                             Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        if (!sealedInterfacesToGenerateByLargeInterface.containsKey(largeInterfaceElement)
                || sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).isEmpty()) {
            return TOP_LEVEL_AND_SEAL_FOR_REPORT_MSG;
        }
        largeInterfaceElement.getAnnotationMirrors().stream()
                .filter(annotationMirror -> annotationMirror.toString().contains(ORG_JISEL_DETACHALL))
                .findFirst() // sample value: Optional[@org.jisel.annotations.DetachAll]
                .ifPresent(annotationMirror ->
                        updateDetachedInterfacesToGenerateByLargeInterface(
                                detachedInterfacesToGenerateByLargeInterface,
                                largeInterfaceElement,
                                Map.of(JISEL_KEYWORD_ALL, Map.of())
                        )
                );
        return EMPTY_STRING;
    }

    /**
     * // TODO jdoc...
     *
     * @param detachedInterfacesToGenerateByLargeInterface
     * @param largeInterfaceElement
     * @param processingEnv
     * @param sealedInterfacesToGenerateByLargeInterface
     */
    default String handleSingleDetachAnnotation(Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
                                                Element largeInterfaceElement,
                                                ProcessingEnvironment processingEnv,
                                                Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        var detachAnnotationMirrorOpt = largeInterfaceElement.getAnnotationMirrors().stream()
                .filter(annotationMirror -> !annotationMirror.toString().contains(ORG_JISEL_DETACHALL))
                .filter(annotationMirror -> annotationMirror.toString().contains(ORG_JISEL_DETACH))
                .findFirst();
        // sample value: Optional[@org.jisel.annotations.Detach(profile="(toplevel)", superInterfaces={com.bayor.jisel.annotation.client.hierarchical.Sociable.class}, applyAnnotations={"@Deprecated"}, rename="newName")]
        if (detachAnnotationMirrorOpt.isPresent()) {
            var attributesMap = processingEnv.getElementUtils().getElementValuesWithDefaults(detachAnnotationMirrorOpt.get())
                    // ElementValues sample:
                    // {profile()="(toplevel)", superInterfaces()={com.bayor.Sociable.class}, applyAnnotations()={"@Deprecated"}, rename()="newName", firstSuperInterfaceGenerics()={}, ...
                    .entrySet().stream()
                    .collect(toMap( // mutable map
                            entry -> entry.getKey().toString().substring(0, entry.getKey().toString().indexOf(OPENING_PARENTHESIS)),
                            entry -> (Object) annotationAttributeValueWithoutQuotes(entry.getValue().toString())
                    ));
            if (!sealedInterfacesToGenerateByLargeInterface.containsKey(largeInterfaceElement)) {
                return TOP_LEVEL_AND_SEAL_FOR_REPORT_MSG;
            }
            var profile = attributesMap.get(PROFILE).toString();
            if (!isJiselKeyword(profile) && !sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).containsKey(profile)) {
                return DETACH_REPORT_MSG;
            }
            updateDetachedInterfacesToGenerateByLargeInterface(
                    detachedInterfacesToGenerateByLargeInterface,
                    largeInterfaceElement,
                    Map.of(profile, attributesMap)
            );
        }
        return EMPTY_STRING;
    }

    /**
     * // TODO jdoc...
     *
     * @param detachedInterfacesToGenerateByLargeInterface
     * @param largeInterfaceElement
     * @param detachsAnnotationMirror
     * @param sealedInterfacesToGenerateByLargeInterface
     */
    default String handleMultipleDetachAnnotations(Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
                                                   Element largeInterfaceElement,
                                                   AnnotationMirror detachsAnnotationMirror,
                                                   Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        var detachsAnnotationRawContentString = detachsAnnotationMirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().toString().equals(VALUE + OPENING_PARENTHESIS + CLOSING_PARENTHESIS))
                .toList().get(0).getValue().getValue().toString();
        // detachsAnnotationRawContentString sample value:
        // @org.jisel.annotations.Detach(profile="(toplevel)", superInterfaces={com.bayor.Sociable.class, com.bayor.Processor.class}, applyAnnotations={"@Deprecated", @Annot2}, rename="newName"),@org.jisel.annotations.Detach(profile="PRo1", ...)
        var detachMatcher = Pattern.compile(DETACH_REGEX).matcher(detachsAnnotationRawContentString.replace(JISEL_KEYWORD_TOPLEVEL, JISEL_KEYWORD_TOPLEVEL_TRANSFORMED));
        while (detachMatcher.find()) {
            var detachAnnotationRawContent = detachMatcher.group(1).strip();
            // detachAnnotationRawContent sample value:
            // profile="_toplevel_", superInterfaces={com.bayor.Sociable.class,...}, applyAnnotations={"@Deprecated",...}, rename="newName"
            String profile = extractDetachAttributeValue(detachAnnotationRawContent, DETACH_PROFILE_REGEX);
            if (!sealedInterfacesToGenerateByLargeInterface.containsKey(largeInterfaceElement)) {
                return TOP_LEVEL_AND_SEAL_FOR_REPORT_MSG;
            }
            if (!isJiselKeyword(profile) && !sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).containsKey(profile)) {
                return DETACH_REPORT_MSG;
            }
            updateDetachedInterfacesToGenerateByLargeInterface(
                    detachedInterfacesToGenerateByLargeInterface,
                    largeInterfaceElement,
                    Map.of(profile, new HashMap<>(Map.of(
                                    DETACH_PROFILE, profile,
                                    DETACH_RENAME, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_RENAME_REGEX),
                                    DETACH_SUPERINTERFACES, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_SUPERINTERFACES_REGEX),
                                    DETACH_FIRSTSUPERINTERFACEGENERICS, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_FIRSTSUPERINTERFACEGENERICS_REGEX),
                                    DETACH_SECONDSUPERINTERFACEGENERICS, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_SECONDSUPERINTERFACEGENERICS_REGEX),
                                    DETACH_THIRDSUPERINTERFACEGENERICS, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_THIRDSUPERINTERFACEGENERICS_REGEX),
                                    DETACH_APPLYANNOTATIONS, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_APPLYANNOTATIONS_REGEX)
                            ))
                    )
            );
        }
        return EMPTY_STRING;
    }

    private String extractDetachAttributeValue(String detachAnnotationRawContentString, String detachAttribRegex) {
        var matcher = Pattern.compile(detachAttribRegex).matcher(detachAnnotationRawContentString);
        if (matcher.find()) {
            return matcher.group(1).strip();
        }
        return EMPTY_STRING;
    }

    private Map<String, Map<String, Object>> updateDetachedInterfacesToGenerateByLargeInterface(Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
                                                                                                Element largeInterfaceElement,
                                                                                                Map<String, Map<String, Object>> detachedInterfacesToGenerate) {
        // when a profile name is repeated in 2 different @Detach annotations, the following merge triggers:
        // Fatal error compiling: java.lang.IllegalStateException: Duplicate key <profilename> (attempted merging values ...)
        return detachedInterfacesToGenerateByLargeInterface.merge(
                largeInterfaceElement,
                detachedInterfacesToGenerate,
                (currentMap, newMap) -> concat(currentMap.entrySet().stream(), newMap.entrySet().stream())
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    /**
     * // TODO jdoc
     *
     * @param profile
     * @param sealedInterfacesToGenerateByLargeInterface
     * @param sealedInterfacesPermitsByLargeInterface
     * @param largeInterfaceElement
     * @return
     */
    static Set<Element> findAllAbstractMethodsForProfile(String profile,
                                                         Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                         Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                         Element largeInterfaceElement) {
        var methodsElementSet = new HashSet<Element>();
        if (profile.equals(JISEL_KEYWORD_TOPLEVEL) || profile.equals(JISEL_KEYWORD_TOPLEVEL_TRANSFORMED)) {
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(largeInterfaceElement.getSimpleName().toString()));
        } else {
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(profile));
            addMethodsFromParentProfiles(profile, methodsElementSet, largeInterfaceElement, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        }
        return methodsElementSet;
    }

    /**
     * TODO jdoc...
     *
     * @param processedProfile
     * @param methodsElementSet
     * @param largeInterfaceElement
     * @param sealedInterfacesToGenerateByLargeInterface
     * @param sealedInterfacesPermitsByLargeInterface
     */
    private static void addMethodsFromParentProfiles(String processedProfile,
                                                     Set<Element> methodsElementSet,
                                                     Element largeInterfaceElement,
                                                     Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                     Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        Function<String, Optional<Map.Entry<String, List<String>>>> findProfileAsChild = profile -> sealedInterfacesPermitsByLargeInterface.get(largeInterfaceElement).entrySet().stream()
                .filter(mapEntry -> mapEntry.getValue().contains(profile))
                .findFirst();
        var profileOpt = findProfileAsChild.apply(processedProfile);
        if (profileOpt.isPresent()) {
            var parentProfile = profileOpt.get().getKey();
            methodsElementSet.addAll(sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).get(parentProfile));
            addMethodsFromParentProfiles(parentProfile, methodsElementSet, largeInterfaceElement, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface);
        }
    }
}

