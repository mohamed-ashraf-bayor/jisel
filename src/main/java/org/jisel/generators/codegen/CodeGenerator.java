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

import java.util.List;

/**
 * Exposes contract to be fulfilled by a class generating the code of an interface or a class
 */
public sealed interface CodeGenerator permits AnnotationsGenerator, DeclarationGenerator, ExtendsGenerator, MethodsGenerator, PermitsGenerator {

    /**
     * Generates the piece of code requested, based on the parameters provided in the params object and appends it to the provided classOrInterfaceContent param
     *
     * @param classOrInterfaceContent StringBuilder object containing the code of the interface or class being generated
     * @param params                  Expected parameters to be used in the code generation process
     */
    void generateCode(StringBuilder classOrInterfaceContent, List<String> params);
}