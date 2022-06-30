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
package org.jisel.generators.codegen.impl;

import org.jisel.JiselAnnotationProcessor;
import org.jisel.generators.codegen.AnnotationsGenerator;

import javax.lang.model.element.Element;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.NEW_LINE;

/**
 * TODO rwrte jdoc
 * Generates the {@link javax.annotation.processing.Generated} annotation section at the top of the generated interfaces or
 * classes with the attributes: value, date and comments<br>
 */
public final class InterfaceAnnotationsGenerator implements AnnotationsGenerator {

    private static final String DEFAULT_APP_VERSION = "1.2.0";

    private static final String JISEL_ANNOTATION_PROCESSOR_CLASSNAME = JiselAnnotationProcessor.class.getName();

    @Override
    public void buildJavaxGeneratedAnnotationSection(StringBuilder classOrInterfaceContent) {
        buildJavaxGeneratedAnnotationSection(
                classOrInterfaceContent,
                JISEL_ANNOTATION_PROCESSOR_CLASSNAME,
                getPropertyValueFromFile("application.properties", "info.app.version", DEFAULT_APP_VERSION)
        );
    }

    @Override
    public void buildJavaxGeneratedAnnotationSection(StringBuilder classOrInterfaceContent, String annotationProcessorClassname, String appVersion) {
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
    public void buildExistingAnnotations(StringBuilder classOrInterfaceContent, Element element) {
        var existingAnnotations = AnnotationsGenerator.buildExistingAnnotations(element, NEW_LINE);
        generateCode(classOrInterfaceContent, List.of(existingAnnotations.isEmpty() ? EMPTY_STRING : existingAnnotations + NEW_LINE));
    }

    // TODO move to static func in utils intrfc
    private String getPropertyValueFromFile(String fileNameWithExt, String property, String defaultValue) {
        var properties = new Properties();
        var in = this.getClass().getClassLoader().getResourceAsStream(fileNameWithExt);
        try {
            properties.load(in);
        } catch (IOException e) {
            return defaultValue;
        }
        return properties.getProperty(property);
    }
}