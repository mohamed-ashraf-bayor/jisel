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

import org.jisel.generators.contentgen.AbstractSealedSourceContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.jisel.generators.StringGenerator.CLOSING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.FINAL_CLASS_SUFFIX;
import static org.jisel.generators.StringGenerator.OPENING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.OPENING_PARENTHESIS;
import static org.jisel.generators.StringGenerator.PACKAGE;
import static org.jisel.generators.StringGenerator.PUBLIC_FINAL_CLASS;
import static org.jisel.generators.StringGenerator.UNDERSCORE;
import static org.jisel.generators.StringGenerator.generatePackageName;
import static org.jisel.generators.StringGenerator.removeDoubleSpaceOccurrences;

/**
 * Generates content for a final class.<br>
 * A final class is always generated by Jisel for the bottom-level generated sealed interfaces.<br>
 * The generated final class implements the provided large interface and implements all its methods by providing default return values for each.
 */
public final class FinalClassSourceContentGenerator extends AbstractSealedSourceContentGenerator {

    /**
     * Array of methods to exclude while pulling the list of all inherited methods of a class or interface
     */
    private static final String[] METHODS_TO_EXCLUDE = {"getClass", "wait", "notifyAll", "hashCode", "equals", "notify", "toString"};

    /**
     * TODO jdoc...
     *
     * @param processingEnvironment
     */
    public FinalClassSourceContentGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public String generateSourceContent(Element largeInterfaceElement,
                                        boolean unSeal,
                                        Map.Entry<String, Set<Element>> sealedInterfaceToGenerate,
                                        Map<String, List<String>> sealedInterfacesPermitsMap) {
        var finalClassName = UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX;
        var finalClassContent = new StringBuilder();
        // package name
        generatePackageName(largeInterfaceElement).ifPresent(name -> finalClassContent.append(format("%s %s;%n%n", PACKAGE, name)));
        // javaxgenerated
        buildJavaxGeneratedAnnotation(finalClassContent);
        // public final class
        finalClassContent.append(format(
                "%s %s ",
                PUBLIC_FINAL_CLASS,
                finalClassName
        ));
        // list of extends
        extendsGenerator.generateExtendsClauseFromPermitsMapAndProcessedProfile(
                finalClassContent,
                sealedInterfacesPermitsMap,
                finalClassName,
                largeInterfaceElement,
                false // unSeal = false. Final classes are generated only while building a sealed hierarchy
        );
        // opening bracket after permits list
        finalClassContent.append(format(" %s%n ", OPENING_CURLY_BRACE));
        // list of methods
        methodsGenerator.generateEmptyConcreteMethodsFromElementsSet(
                finalClassContent,
                processingEnvironment.getElementUtils().getAllMembers((TypeElement) largeInterfaceElement).stream()
                        .filter(element -> ElementKind.METHOD.equals(element.getKind()))
                        .filter(element -> asList(METHODS_TO_EXCLUDE).stream()
                                .noneMatch(excludedMeth -> element.toString().contains(excludedMeth + OPENING_PARENTHESIS)))
                        .collect(toSet())
        );
        // closing bracket
        finalClassContent.append(CLOSING_CURLY_BRACE);
        //
        return removeDoubleSpaceOccurrences(finalClassContent.toString());
    }
}