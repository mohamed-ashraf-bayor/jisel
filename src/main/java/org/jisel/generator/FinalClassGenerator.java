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
package org.jisel.generator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

public class FinalClassGenerator implements StringGenerator {

    private final CodeGenerator javaxGeneratedGenerator;
    private final ExtendsGenerator extendsGenerator;
    private final MethodsGenerator methodsGenerator;

    public FinalClassGenerator() {
        this.javaxGeneratedGenerator = new JavaxGeneratedGenerator();
        this.extendsGenerator = new SealedInterfaceExtendsGenerator();
        this.methodsGenerator = new SealedInterfaceMethodsGenerator();
    }

    public String generateFinalClassContent(final ProcessingEnvironment processingEnvironment, final Element largeInterfaceElement, final Map<String, List<String>> sealedInterfacesPermitsMap) {
        var finalClassName = UNDERSCORE + largeInterfaceElement.getSimpleName().toString() + FINAL_CLASS_SUFFIX;
        var finalClassContent = new StringBuilder();
        // package name
        generatePackageName(largeInterfaceElement).ifPresent(name -> finalClassContent.append(format("%s %s;%n%n", PACKAGE, name)));
        // javaxgenerated
        javaxGeneratedGenerator.generateCode(finalClassContent, null);
        // public final class
        finalClassContent.append(format(
                "%s %s ",
                PUBLIC_FINAL_CLASS,
                finalClassName
        ));
        // list of implements
        extendsGenerator.generateExtendsClauseFromPermitsMapAndProcessedProfile(processingEnvironment, finalClassContent, sealedInterfacesPermitsMap, finalClassName, largeInterfaceElement);
        // opening bracket after permits list
        finalClassContent.append(format(" %s%n ", OPENING_BRACKET));
        // list of methods
        methodsGenerator.generateEmptyConcreteMethodsFromElementsSet(
                finalClassContent,
                processingEnvironment.getElementUtils().getAllMembers((TypeElement) largeInterfaceElement).stream()
                        .filter(element -> ElementKind.METHOD.equals(element.getKind()))
                        .filter(element -> asList(METHODS_TO_EXCLUDE).stream().noneMatch(excludedMeth -> element.toString().contains(excludedMeth + OPENING_PARENTHESIS)))
                        .collect(toSet())
        );
        // closing bracket
        finalClassContent.append(CLOSING_BRACKET);
        //
        return removeDoubleSpaceOccurrences(finalClassContent.toString());
    }
}
