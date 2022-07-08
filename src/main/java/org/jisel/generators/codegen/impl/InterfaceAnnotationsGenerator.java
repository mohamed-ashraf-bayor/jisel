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

import org.jisel.generators.codegen.AnnotationsGenerator;

import javax.lang.model.element.Element;
import java.util.List;

import static org.jisel.generators.AppInfoGenerator.APPLICATION_PROPERTIES_FILENAME;
import static org.jisel.generators.AppInfoGenerator.DEFAULT_APP_VERSION;
import static org.jisel.generators.AppInfoGenerator.INFO_APP_VERSION_PROPERTY_NAME;
import static org.jisel.generators.AppInfoGenerator.JISEL_ANNOTATION_PROCESSOR_CLASSNAME;
import static org.jisel.generators.AppInfoGenerator.getPropertyValueFromPropsFile;
import static org.jisel.generators.StringGenerator.NEW_LINE;

/**
 * TODO rwrte jdoc
 * Generates the {@link javax.annotation.processing.Generated} annotation section at the top of the generated interfaces or
 * classes with the attributes: value, date and comments
 */
public final class InterfaceAnnotationsGenerator implements AnnotationsGenerator {

    @Override
    public void buildJavaxGeneratedAnnotationSection(StringBuilder classOrInterfaceContent) {
        buildJavaxGeneratedAnnotationSection(
                classOrInterfaceContent,
                JISEL_ANNOTATION_PROCESSOR_CLASSNAME,
                getPropertyValueFromPropsFile(APPLICATION_PROPERTIES_FILENAME, INFO_APP_VERSION_PROPERTY_NAME, DEFAULT_APP_VERSION)
        );
    }

    @Override
    public void buildExistingAnnotations(StringBuilder classOrInterfaceContent, Element element) {
        generateCode(classOrInterfaceContent, List.of(AnnotationsGenerator.buildExistingAnnotations(element, NEW_LINE)));
    }
}