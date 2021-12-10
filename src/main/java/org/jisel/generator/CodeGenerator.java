/**
 * Copyright (c) 2021-2022 Mohamed Ashraf Bayor.
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

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * Exposes contract for a CodeGenerator class to fulfill
 */
public sealed interface CodeGenerator permits JavaxGeneratedGenerator, ExtendsGenerator, PermitsGenerator, MethodsGenerator {

    /**
     * Generates piece of code requested, based on the parameters provided in the params object and appends it to the provided recordClassContent param
     *
     * @param sealedInterfaceContent Stringbuilder object containing the record class code being generated
     * @param params                 expected parameters. restricted to paremeters and values expected by the implementing class
     */
    void generateCode(StringBuilder sealedInterfaceContent, List<String> params);
}

sealed interface ExtendsGenerator extends CodeGenerator, StringGenerator permits JiselExtendsGenerator {

    void generateExtendsClauseFromPermitsMapAndProcessedProfile(StringBuilder sealedInterfaceContent, Map<String, List<String>> permitsMap, String processedProfile, Element bloatedInterfaceElement);

    @Override
    default void generateCode(final StringBuilder sealedInterfaceContent, final List<String> params) {
        sealedInterfaceContent.append(format(
                " %s %s ",
                EXTENDS,
                params.stream().map(listEntry -> removeSeparator(listEntry)).collect(joining(COMMA_SEPARATOR + WHITESPACE))
        ));
    }
}

sealed interface PermitsGenerator extends CodeGenerator, StringGenerator permits JiselPermitsGenerator {

    void generatePermitsClauseFromPermitsMapAndProcessedProfile(StringBuilder sealedInterfaceContent, Map<String, List<String>> permitsMap, String processedProfile, Element bloatedInterfaceElement);

    @Override
    default void generateCode(final StringBuilder sealedInterfaceContent, final List<String> params) {
        sealedInterfaceContent.append(format(
                " %s %s ",
                PERMITS,
                params.stream().map(listEntry -> removeSeparator(listEntry)).collect(joining(COMMA_SEPARATOR + WHITESPACE))
        ));
    }
}

sealed interface MethodsGenerator extends CodeGenerator, StringGenerator permits JiselMethodsGenerator {

    void generateMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet);

    @Override
    default void generateCode(final StringBuilder sealedInterfaceContent, final List<String> params) {
        params.forEach(methodDefinition -> sealedInterfaceContent.append(format("\t%s;%n", methodDefinition)));
    }

    default boolean methodHasArguments(final Element element) {
        return element.toString().indexOf(CLOSING_PARENTHESIS) - element.toString().indexOf(OPENING_PARENTHESIS) > 1;
    }
}