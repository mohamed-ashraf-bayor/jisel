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
package org.jisel.generators.codegen;

import org.jisel.generators.codegen.impl.InterfaceAnnotationsGenerator;

import javax.lang.model.element.Element;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.ESCAPED_NEW_LINE;
import static org.jisel.generators.StringGenerator.JISEL_ANNOTATIONS_PACKAGE;
import static org.jisel.generators.StringGenerator.NEW_LINE;
import static org.jisel.generators.UnescapeJavaString.unescapeJavaString;

/**
 * // TODO jdoc entire clss
 * Exposes contract to be fulfilled by a class generating annotations
 */
public sealed interface AnnotationsGenerator extends CodeGenerator permits InterfaceAnnotationsGenerator {

    void buildJavaxGeneratedAnnotationSection(StringBuilder classOrInterfaceContent);

    void buildExistingAnnotations(StringBuilder classOrInterfaceContent, Element element);

    /**
     * ... excluding all jisel annots..
     *
     * @param element
     * @param separator
     * @return
     */
    static String buildExistingAnnotations(Element element, String separator) {
        String existingAnnotations = element.getAnnotationMirrors().stream()
                .map(Object::toString)
                .filter(annotationString -> !annotationString.contains(JISEL_ANNOTATIONS_PACKAGE))
                .collect(joining(separator));
        return existingAnnotations.isEmpty() ? EMPTY_STRING : existingAnnotations + separator;
    }

    static String[] splitApplyAnnotationsRawValue(String applyAnnotationsRawValue) {
        var strippedApplyAnnotationsRawValue = applyAnnotationsRawValue.strip();
        if (!applyAnnotationsRawValue.isBlank()) {
            return stream(applyAnnotationsRawValue.split(ESCAPED_NEW_LINE))
                    .map(String::strip)
                    .toList()
                    .toArray(String[]::new);
        }
        return new String[]{strippedApplyAnnotationsRawValue};
    }

    /**
     * ...
     *
     * @param classOrInterfaceContent
     * @param annotationsRawStrings
     */
    default void applyAnnotations(StringBuilder classOrInterfaceContent, String[] annotationsRawStrings) {
        generateCode(
                classOrInterfaceContent,
                stream(annotationsRawStrings).map(AnnotationsGenerator::cleanUpApplyAnnotation).toList()
        );
    }

    /**
     * ...
     *
     * @param annotationString
     * @return
     */
    private static String cleanUpApplyAnnotation(String annotationString) {
        var strippedString = annotationString.strip();
        if (!strippedString.isBlank()) {
            return unescapeJavaString(annotationString);
        }
        return strippedString;
    }

    /**
     * todo ...
     *
     * @param classOrInterfaceContent
     * @param annotationProcessorClassname
     * @param appVersion
     */
    default void buildJavaxGeneratedAnnotationSection(StringBuilder classOrInterfaceContent, String annotationProcessorClassname, String appVersion) {
        generateCode(
                classOrInterfaceContent,
                List.of(
                        format("""
                                        @javax.annotation.processing.Generated(
                                            value = "%s",
                                            date = "%s",
                                            comments = "version: %s"
                                        )
                                        """,
                                annotationProcessorClassname,
                                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                appVersion
                        )
                ));
    }

    @Override
    default void generateCode(StringBuilder classOrInterfaceContent, List<String> params) {
        classOrInterfaceContent.append(params.stream().collect(joining(NEW_LINE)));
    }
}