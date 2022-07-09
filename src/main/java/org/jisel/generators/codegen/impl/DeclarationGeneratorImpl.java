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

import org.jisel.generators.codegen.DeclarationGenerator;

import javax.lang.model.element.Element;
import java.util.List;

import static org.jisel.generators.StringGenerator.PUBLIC_INTERFACE;
import static org.jisel.generators.StringGenerator.PUBLIC_SEALED_INTERFACE;
import static org.jisel.generators.StringGenerator.sealedInterfaceNameConvention;
import static org.jisel.generators.StringGenerator.unSealedInterfaceNameConvention;

/**
 * Generates the interface declaration section (modifiers + name)
 */
public final class DeclarationGeneratorImpl implements DeclarationGenerator {
    @Override
    public void generateModifiersAndName(StringBuilder interfaceContent, String profile, Element largeInterfaceElement, boolean unSeal) {
        generateCode(
                interfaceContent,
                List.of(
                        unSeal ? PUBLIC_INTERFACE
                                : PUBLIC_SEALED_INTERFACE,
                        unSeal ? unSealedInterfaceNameConvention(profile, largeInterfaceElement)
                                : sealedInterfaceNameConvention(profile, largeInterfaceElement)
                )
        );
    }
}