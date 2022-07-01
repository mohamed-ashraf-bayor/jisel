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

import org.jisel.generators.StringGenerator;
import org.jisel.generators.codegen.impl.InterfaceExtendsGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.jisel.generators.StringGenerator.CLASS;
import static org.jisel.generators.StringGenerator.COMMA_SEPARATOR;
import static org.jisel.generators.StringGenerator.EXTENDS;
import static org.jisel.generators.StringGenerator.IMPLEMENTS;
import static org.jisel.generators.StringGenerator.INTERFACE;
import static org.jisel.generators.StringGenerator.WHITESPACE;

/**
 * Exposes contract to be fulfilled by a class generating the "extends" clause of a sealed interface definition, along with
 * the list of the parent classes or interfaces
 */
public sealed interface ExtendsGenerator extends CodeGenerator permits InterfaceExtendsGenerator {

    /**
     * Generates the "extends" clause of a sealed interface being generated, along with the list of parent interfaces, based on
     * a provided {@link Map} containing parents/subtypes information (the permits Map) and the name of the profile for which the
     * sealed interface will be generated
     *
     * @param sealedInterfaceContent {@link StringBuilder} object containing the sealed interface code being generated
     * @param permitsMap             {@link Map} containing parents/subtypes information. The Map key is the profile name whose generated
     *                               sealed interface will be a parent interface, while the value is the list of profiles names whose
     *                               sealed interfaces will be generated as subtypes
     * @param processedProfile       name of the profile whose sealed interface is being generated
     * @param largeInterfaceElement  {@link Element} instance of the large interface being segregated
     */
    void generateExtendsClauseFromPermitsMapAndProcessedProfile(StringBuilder sealedInterfaceContent,
                                                                Map<String, List<String>> permitsMap,
                                                                String processedProfile,
                                                                Element largeInterfaceElement,
                                                                boolean unSeal);

    @Override
    default void generateCode(StringBuilder classOrInterfaceContent, List<String> params) {
        classOrInterfaceContent.append(format(
                " %s %s ",
                isInterface(classOrInterfaceContent.toString()) ? EXTENDS : IMPLEMENTS,
                params.stream().map(StringGenerator::removeCommaSeparator).collect(joining(COMMA_SEPARATOR + WHITESPACE))
        ));
    }

    private boolean isInterface(String classOrInterfaceContent) {
        return classOrInterfaceContent.contains(INTERFACE + WHITESPACE) && !classOrInterfaceContent.contains(CLASS + WHITESPACE);
    }
}