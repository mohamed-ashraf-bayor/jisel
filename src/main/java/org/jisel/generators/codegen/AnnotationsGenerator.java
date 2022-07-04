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
import static org.jisel.generators.StringGenerator.BACKSLASH;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.JISEL_ANNOTATIONS_PACKAGE;
import static org.jisel.generators.StringGenerator.NEW_LINE;

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
        return element.getAnnotationMirrors().stream()
                .map(Object::toString)
                .filter(annotationString -> !annotationString.contains(JISEL_ANNOTATIONS_PACKAGE))
                .collect(joining(separator));
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
                stream(annotationsRawStrings).map(AnnotationsGenerator::cleanUpAppliedAnnotation).toList()
        );
    }

    /**
     * ...
     *
     * @param annotationString
     * @return
     */
    static String cleanUpAppliedAnnotation(String annotationString) {
        var withoutBackslashes = annotationString.replace(BACKSLASH, EMPTY_STRING);
        return withoutBackslashes.substring(1, withoutBackslashes.length() - 1);

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
        classOrInterfaceContent.append(params.stream().map(String::strip).collect(joining(NEW_LINE)));
    }
}