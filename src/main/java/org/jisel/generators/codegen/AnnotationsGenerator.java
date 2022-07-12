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

import org.jisel.generators.UnescapeJavaString;
import org.jisel.generators.codegen.impl.AnnotationsGeneratorImpl;

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

/**
 * Exposes contract to be fulfilled by a class generating annotations, along with commonly needed default and static functions
 */
public sealed interface AnnotationsGenerator extends CodeGenerator permits AnnotationsGeneratorImpl {

    /**
     * Generates existing annotations of the provided {@link Element} instance and appends them to the provided {@link StringBuilder} instance
     *
     * @param classOrInterfaceContent StringBuilder object containing the code of the interface or class being generated
     * @param element                 {@link Element} instance of the interface, class, method or parameter to generate the existing annotations for
     */
    void generateExistingAnnotations(StringBuilder classOrInterfaceContent, Element element);

    /**
     * Generates the {@link javax.annotation.processing.Generated} annotation section at the top of the generated interfaces or
     * classes along with the attributes: value, date and comments
     *
     * @param classOrInterfaceContent      StringBuilder object containing the code of the interface or class being generated
     * @param annotationProcessorClassname Qualified name of the Java annotation processor, displayed as the "value" attribute within
     *                                     the &#64;{@link javax.annotation.processing.Generated} annotation
     * @param appVersion                   Current version of the app, displayed as part of the "comments" attribute within the
     *                                     &#64;{@link javax.annotation.processing.Generated} annotation
     */
    default void generateJavaxGeneratedAnnotation(StringBuilder classOrInterfaceContent, String annotationProcessorClassname, String appVersion) {
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

    /**
     * Finds all existing annotations (excluding Jisel annotations) of the provided {@link Element} instance and joins them
     * with the provided separator string value
     *
     * @param element   {@link Element} instance of the interface, class, method or parameter to generate the existing annotations for
     * @param separator String used to join the annotations
     * @return String containing the existing annotations joined by the provided separator String
     */
    static String buildExistingAnnotations(Element element, String separator) {
        var existingAnnotations = element.getAnnotationMirrors().stream()
                .map(Object::toString)
                .filter(annotationString -> !annotationString.contains(JISEL_ANNOTATIONS_PACKAGE))
                .collect(joining(separator));
        return existingAnnotations.isEmpty() ? EMPTY_STRING : existingAnnotations + separator;
    }

    /**
     * Cleans up the annotations provided through the 'applyAnnotations' attribute of the &#64;{@link org.jisel.annotations.Detach}
     * annotation and appends the result to the provided {@link StringBuilder} instance
     *
     * @param classOrInterfaceContent  StringBuilder object containing the code of the interface or class being generated
     * @param applyAnnotationsRawValue String containing the provided 'applyAnnotations' attribute value
     */
    default void applyAnnotations(StringBuilder classOrInterfaceContent, String applyAnnotationsRawValue) {
        var strippedApplyAnnotationsRawValue = applyAnnotationsRawValue.strip();
        if (!strippedApplyAnnotationsRawValue.isBlank()) {
            generateCode(
                    classOrInterfaceContent,
                    stream(strippedApplyAnnotationsRawValue.split(ESCAPED_NEW_LINE))
                            .map(String::strip)
                            .map(UnescapeJavaString::unescapeJavaString)
                            .toList()
            );
        }
    }

    @Override
    default void generateCode(StringBuilder classOrInterfaceContent, List<String> params) {
        classOrInterfaceContent.append(params.stream().collect(joining(NEW_LINE)));
    }
}