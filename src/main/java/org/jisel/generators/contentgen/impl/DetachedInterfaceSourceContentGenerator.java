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
package org.jisel.generators.contentgen.impl;

import org.jisel.generators.contentgen.AbstractSealedDetachedInterfaceSourceContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.jisel.generators.StringGenerator.CLOSING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generators.StringGenerator.DETACH_APPLYANNOTATIONS;
import static org.jisel.generators.StringGenerator.DETACH_FIRST_SUPERINTERFACE_GENERICS;
import static org.jisel.generators.StringGenerator.DETACH_METHODS;
import static org.jisel.generators.StringGenerator.DETACH_PROFILE;
import static org.jisel.generators.StringGenerator.DETACH_SECOND_SUPERINTERFACE_GENERICS;
import static org.jisel.generators.StringGenerator.DETACH_SUPERINTERFACES;
import static org.jisel.generators.StringGenerator.DETACH_THIRD_SUPERINTERFACE_GENERICS;
import static org.jisel.generators.StringGenerator.EMPTY_STRING;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL;
import static org.jisel.generators.StringGenerator.JISEL_KEYWORD_TOPLEVEL_REPLACEMENT;
import static org.jisel.generators.StringGenerator.NEW_LINE;
import static org.jisel.generators.StringGenerator.OPENING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.PACKAGE;
import static org.jisel.generators.StringGenerator.PUBLIC_INTERFACE;
import static org.jisel.generators.StringGenerator.extractPackageName;
import static org.jisel.generators.StringGenerator.extractSimpleName;
import static org.jisel.generators.StringGenerator.removeDoubleSpaceOccurrences;

/**
 * Generates the String content of a detached interface
 */
public final class DetachedInterfaceSourceContentGenerator extends AbstractSealedDetachedInterfaceSourceContentGenerator {

    /**
     * Passes through the received {@link ProcessingEnvironment} instance to the super constructor
     *
     * @param processingEnvironment {@link ProcessingEnvironment} instance needed for report content generation
     */
    public DetachedInterfaceSourceContentGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String generateDetachedInterfaceSourceContent(String detachedInterfaceQualifiedName,
                                                         Map<String, Object> detachAttribs,
                                                         Element largeInterfaceElement) {
        var profile = detachAttribs.get(DETACH_PROFILE).toString();
        var superInterfacesRawValue = Optional.ofNullable(detachAttribs.get(DETACH_SUPERINTERFACES)).orElse(EMPTY_STRING).toString();
        var firstSuperInterfaceGenericsRawValue = Optional.ofNullable(detachAttribs.get(DETACH_FIRST_SUPERINTERFACE_GENERICS)).orElse(EMPTY_STRING).toString();
        var secondSuperInterfaceGenericsRawValue = Optional.ofNullable(detachAttribs.get(DETACH_SECOND_SUPERINTERFACE_GENERICS)).orElse(EMPTY_STRING).toString();
        var thirdSuperInterfaceGenericsRawValue = Optional.ofNullable(detachAttribs.get(DETACH_THIRD_SUPERINTERFACE_GENERICS)).orElse(EMPTY_STRING).toString();
        var applyAnnotationsRawValue = Optional.ofNullable(detachAttribs.get(DETACH_APPLYANNOTATIONS)).orElse(EMPTY_STRING).toString();
        var methods = (Set<Element>) Optional.ofNullable(detachAttribs.get(DETACH_METHODS)).orElse(Set.<Element>of());
        //
        var interfaceContent = new StringBuilder();
        // package name
        extractPackageName(detachedInterfaceQualifiedName).ifPresent(
                packageName -> interfaceContent.append(format("%s %s", PACKAGE, packageName))
        );
        interfaceContent.append(format(";%n%n"));
        // javaxgenerated
        buildJavaxGeneratedAnnotation(interfaceContent);
        // existing annotations
        if (JISEL_KEYWORD_TOPLEVEL.equalsIgnoreCase(profile)
                || JISEL_KEYWORD_TOPLEVEL_REPLACEMENT.equalsIgnoreCase(profile)
                || largeInterfaceElement.getSimpleName().toString().equals(profile)) {
            annotationsGenerator.generateExistingAnnotations(interfaceContent, largeInterfaceElement);
        }
        // apply provided annotations raw string values
        if (!applyAnnotationsRawValue.isBlank()) {
            // applyAnnotationsRawValue sample value:
            //  @Deprecated\n        @SuppressWarnings({\"unused\"})\n        @RequestMapping(value = \"/ex/foos/{fooid}/bar/{barid}\", method = GET)\n
            annotationsGenerator.applyAnnotations(interfaceContent, applyAnnotationsRawValue);
            interfaceContent.append(NEW_LINE);
        }
        // declaration: public interface
        interfaceContent.append(format(
                "%s %s ",
                PUBLIC_INTERFACE,
                extractSimpleName(detachedInterfaceQualifiedName).orElse(EMPTY_STRING)
        ));
        // list of extends
        if (!superInterfacesRawValue.isBlank()) {
            var superInterfacesArray = superInterfacesRawValue.split(COMMA_SEPARATOR);
            BiFunction<String[], Integer, String> elementAtIndex = (array, index) -> index < array.length ? array[index] : index.toString();
            extendsGenerator.generateExtendsClauseFromSuperInterfacesList(
                    interfaceContent,
                    asList(superInterfacesArray),
                    Map.of(
                            elementAtIndex.apply(superInterfacesArray, 0), firstSuperInterfaceGenericsRawValue.isBlank()
                                    ? List.of()
                                    : asList(firstSuperInterfaceGenericsRawValue.split(COMMA_SEPARATOR)),
                            elementAtIndex.apply(superInterfacesArray, 1), secondSuperInterfaceGenericsRawValue.isBlank()
                                    ? List.of()
                                    : asList(secondSuperInterfaceGenericsRawValue.split(COMMA_SEPARATOR)),
                            elementAtIndex.apply(superInterfacesArray, 2), thirdSuperInterfaceGenericsRawValue.isBlank()
                                    ? List.of()
                                    : asList(thirdSuperInterfaceGenericsRawValue.split(COMMA_SEPARATOR))
                    )
            );
        }
        interfaceContent.append(format(" %s%n ", OPENING_CURLY_BRACE)); // opening bracket after permits list
        // list of methods
        methodsGenerator.generateAbstractMethodsFromElementsSet(interfaceContent, methods);
        // closing bracket
        interfaceContent.append(CLOSING_CURLY_BRACE);
        //
        return removeDoubleSpaceOccurrences(interfaceContent.toString());
    }
}