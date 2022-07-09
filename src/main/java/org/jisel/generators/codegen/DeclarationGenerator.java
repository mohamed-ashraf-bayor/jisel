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

import org.jisel.generators.codegen.impl.DeclarationGeneratorImpl;

import javax.lang.model.element.Element;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.jisel.generators.StringGenerator.WHITESPACE;

/**
 * Exposes contract to be fulfilled by a class generating the interface or class declaration section (modifiers + name)
 */
public sealed interface DeclarationGenerator extends CodeGenerator permits DeclarationGeneratorImpl {

    /**
     * Generates the interface or class declaration section
     *
     * @param interfaceContent      {@link StringBuilder} object containing the interface or class code being generated
     * @param profile               name of the profile whose interface is being generated
     * @param largeInterfaceElement {@link Element} instance of the large interface being segregated
     * @param unSeal                indicates whether the interface declaration should include "sealed"
     */
    void generateModifiersAndName(StringBuilder interfaceContent, String profile, Element largeInterfaceElement, boolean unSeal);

    @Override
    default void generateCode(StringBuilder classOrInterfaceContent, List<String> params) {
        classOrInterfaceContent.append(format(
                "%s",
                params.stream().collect(joining(WHITESPACE))
        ));
    }
}