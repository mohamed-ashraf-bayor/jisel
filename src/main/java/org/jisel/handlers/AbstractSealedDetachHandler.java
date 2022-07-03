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

import org.jisel.handlers.impl.DetachHandler;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.jisel.generators.StringGenerator.AT_SIGN;
import static org.jisel.generators.StringGenerator.CLOSING_PARENTHESIS;
import static org.jisel.generators.StringGenerator.DETACH_APPLYANNOTATIONS;
import static org.jisel.generators.StringGenerator.DETACH_APPLYANNOTATIONS_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_FIRST_SUPERINTERFACE_GENERICS;
import static org.jisel.generators.StringGenerator.DETACH_FIRST_SUPERINTERFACE_GENERICS_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_PROFILE;
import static org.jisel.generators.StringGenerator.DETACH_PROFILE_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_RENAME;
import static org.jisel.generators.StringGenerator.DETACH_RENAME_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_REPORT_MSG;
import static org.jisel.generators.StringGenerator.DETACH_SECOND_SUPERINTERFACE_GENERICS;
import static org.jisel.generators.StringGenerator.DETACH_SECOND_SUPERINTERFACE_GENERICS_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_SUPERINTERFACES;
import static org.jisel.generators.StringGenerator.DETACH_SUPERINTERFACES_REGEX;
import static org.jisel.generators.StringGenerator.DETACH_THIRD_SUPERINTERFACE_GENERICS;
import static org.jisel.generators.StringGenerator.DETACH_THIRD_SUPERINTERFACE_GENERICS_REGEX;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_ALL;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL_TRANSFORMED;
import static org.jisel.generators.StringGenerator.OPENING_PARENTHESIS;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACH;
import static org.jisel.generators.StringGenerator.ORG_JISEL_DETACHALL;
import static org.jisel.generators.StringGenerator.TOP_LEVEL_AND_SEAL_FOR_REPORT_MSG;
import static org.jisel.generators.StringGenerator.VALUE;
import static org.jisel.generators.StringGenerator.annotationAttributeValueWithoutQuotes;
import static org.jisel.generators.StringGenerator.isJiselKeyword;

/**
 * // TODO jdoc all ...
 */
public abstract sealed class AbstractSealedDetachHandler
        implements JiselAnnotationHandler
        permits DetachHandler {

    protected final ProcessingEnvironment processingEnvironment;

    protected AbstractSealedDetachHandler(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public abstract Map<Element, String> handleDetachAnnotatedElements(Set<Element> allAnnotatedElements,
                                                                       Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                                       Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                                                       Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface);

    /**
     * // TODO jdoc...
     *
     * @param detachedInterfacesToGenerateByLargeInterface
     * @param largeInterfaceElement
     */
    protected String handleDetachAllAnnotation(Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
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
     * @param sealedInterfacesToGenerateByLargeInterface
     */
    protected String handleSingleDetachAnnotation(Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
                                                  Element largeInterfaceElement,
                                                  Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        var detachAnnotationMirrorOpt = largeInterfaceElement.getAnnotationMirrors().stream()
                .filter(annotationMirror -> !annotationMirror.toString().contains(ORG_JISEL_DETACHALL))
                .filter(annotationMirror -> annotationMirror.toString().contains(ORG_JISEL_DETACH))
                .findFirst();
        // sample value: Optional[@org.jisel.annotations.Detach(profile="(toplevel)", superInterfaces={com.bayor.jisel.annotation.client.hierarchical.Sociable.class}, applyAnnotations={"@Deprecated"}, rename="newName")]
        if (detachAnnotationMirrorOpt.isPresent()) {
            var attributesMap = processingEnvironment.getElementUtils().getElementValuesWithDefaults(detachAnnotationMirrorOpt.get())
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
            var profile = attributesMap.get(DETACH_PROFILE).toString();
            var rename = attributesMap.get(DETACH_RENAME).toString();
            var detachedProfileUniqueKey = profile + AT_SIGN + rename;
            if (!isJiselKeyword(profile) && !sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).containsKey(profile)) {
                return DETACH_REPORT_MSG;
            }
            updateDetachedInterfacesToGenerateByLargeInterface(
                    detachedInterfacesToGenerateByLargeInterface,
                    largeInterfaceElement,
                    Map.of(detachedProfileUniqueKey, attributesMap)
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
    protected String handleMultipleDetachAnnotations(Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface,
                                                     Element largeInterfaceElement,
                                                     AnnotationMirror detachsAnnotationMirror,
                                                     Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface) {
        var detachsAnnotationRawContentString = detachsAnnotationMirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().toString().equals(VALUE + OPENING_PARENTHESIS + CLOSING_PARENTHESIS))
                .toList()
                .get(0).getValue().getValue().toString();
        // detachsAnnotationRawContentString sample value:
        // @org.jisel.annotations.Detach(profile="(toplevel)", superInterfaces={com.bayor.Sociable.class, com.bayor.Processor.class}, applyAnnotations={"@Deprecated", @Annot2}, rename="newName"),@org.jisel.annotations.Detach(profile="PRo1", ...)
        var detachMatcher = Pattern.compile(DETACH_REGEX).matcher(detachsAnnotationRawContentString.replace(JISEL_KEYWORD_TOPLEVEL, JISEL_KEYWORD_TOPLEVEL_TRANSFORMED));
        while (detachMatcher.find()) {
            var detachAnnotationRawContent = detachMatcher.group(1).strip();
            // detachAnnotationRawContent sample value:
            // profile="_toplevel_", superInterfaces={com.bayor.Sociable.class,...}, applyAnnotations={"@Deprecated",...}, rename="newName"
            var profile = extractDetachAttributeValue(detachAnnotationRawContent, DETACH_PROFILE_REGEX);
            var rename = extractDetachAttributeValue(detachAnnotationRawContent, DETACH_RENAME_REGEX);
            if (!sealedInterfacesToGenerateByLargeInterface.containsKey(largeInterfaceElement)) {
                return TOP_LEVEL_AND_SEAL_FOR_REPORT_MSG;
            }
            if (!isJiselKeyword(profile) && !sealedInterfacesToGenerateByLargeInterface.get(largeInterfaceElement).containsKey(profile)) {
                return DETACH_REPORT_MSG;
            }
            updateDetachedInterfacesToGenerateByLargeInterface(
                    detachedInterfacesToGenerateByLargeInterface,
                    largeInterfaceElement,
                    Map.of(profile + AT_SIGN + rename, // unique map key made of <profile> + @ + <rename>
                            new HashMap<>(Map.of(
                                    DETACH_PROFILE, profile,
                                    DETACH_RENAME, rename,
                                    DETACH_SUPERINTERFACES, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_SUPERINTERFACES_REGEX),
                                    DETACH_FIRST_SUPERINTERFACE_GENERICS, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_FIRST_SUPERINTERFACE_GENERICS_REGEX),
                                    DETACH_SECOND_SUPERINTERFACE_GENERICS, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_SECOND_SUPERINTERFACE_GENERICS_REGEX),
                                    DETACH_THIRD_SUPERINTERFACE_GENERICS, extractDetachAttributeValue(detachAnnotationRawContent, DETACH_THIRD_SUPERINTERFACE_GENERICS_REGEX),
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
        // when the combination [<profile> + @ + <rename>] is repeated in 2 different @Detach annotations, the following merge triggers:
        // Fatal error compiling: java.lang.IllegalStateException: Duplicate key [<profile> + @ + <rename>] (attempted merging values ...)
        return detachedInterfacesToGenerateByLargeInterface.merge(
                largeInterfaceElement,
                detachedInterfacesToGenerate,
                (currentMap, newMap) -> concat(currentMap.entrySet().stream(), newMap.entrySet().stream())
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    @Override
    public Map<Element, String> handleAnnotatedElements(Set<Element> allAnnotatedElements,
                                                        Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                                        Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface) {
        return handleDetachAnnotatedElements(allAnnotatedElements, sealedInterfacesToGenerateByLargeInterface, sealedInterfacesPermitsByLargeInterface, new HashMap<>());
    }
}