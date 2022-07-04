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
package org.jisel.generators.contentgen;

import org.jisel.generators.codegen.AnnotationsGenerator;
import org.jisel.generators.codegen.DeclarationGenerator;
import org.jisel.generators.codegen.ExtendsGenerator;
import org.jisel.generators.codegen.MethodsGenerator;
import org.jisel.generators.codegen.PermitsGenerator;
import org.jisel.generators.codegen.impl.InterfaceAnnotationsGenerator;
import org.jisel.generators.codegen.impl.InterfaceDeclarationGenerator;
import org.jisel.generators.codegen.impl.InterfaceExtendsGenerator;
import org.jisel.generators.codegen.impl.InterfaceMethodsGenerator;
import org.jisel.generators.codegen.impl.SealedInterfacePermitsGenerator;
import org.jisel.generators.contentgen.impl.FinalClassSourceContentGenerator;
import org.jisel.generators.contentgen.impl.InterfaceSourceContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;

// TODO jdoc

/**
 * Generates content of the final class generated for the provided large interface
 * ... ...
 */
public abstract sealed class AbstractSealedSourceContentGenerator implements SourceContentGenerator
        permits FinalClassSourceContentGenerator, InterfaceSourceContentGenerator, AbstractSealedReportContentGenerator,
        AbstractSealedDetachedInterfaceSourceContentGenerator {

    protected final ProcessingEnvironment processingEnvironment;
    protected final AnnotationsGenerator annotationsGenerator;
    protected final ExtendsGenerator extendsGenerator;
    protected final PermitsGenerator permitsGenerator;
    protected final MethodsGenerator methodsGenerator;
    protected final DeclarationGenerator declarationGenerator;

    /**
     * TODO jdoc...
     */
    protected AbstractSealedSourceContentGenerator(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.annotationsGenerator = new InterfaceAnnotationsGenerator();
        this.extendsGenerator = new InterfaceExtendsGenerator(this.processingEnvironment);
        this.permitsGenerator = new SealedInterfacePermitsGenerator();
        this.methodsGenerator = new InterfaceMethodsGenerator();
        this.declarationGenerator = new InterfaceDeclarationGenerator();
    }
}