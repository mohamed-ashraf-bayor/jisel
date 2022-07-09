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
import org.jisel.generators.codegen.impl.AnnotationsGeneratorImpl;
import org.jisel.generators.codegen.impl.DeclarationGeneratorImpl;
import org.jisel.generators.codegen.impl.ExtendsGeneratorImpl;
import org.jisel.generators.codegen.impl.MethodsGeneratorImpl;
import org.jisel.generators.codegen.impl.PermitsGeneratorImpl;
import org.jisel.generators.contentgen.impl.FinalClassSourceContentGenerator;
import org.jisel.generators.contentgen.impl.InterfaceSourceContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;

import static org.jisel.generators.AppInfoGenerator.APPLICATION_PROPERTIES_FILENAME;
import static org.jisel.generators.AppInfoGenerator.DEFAULT_APP_VERSION;
import static org.jisel.generators.AppInfoGenerator.INFO_APP_VERSION_PROPERTY_NAME;
import static org.jisel.generators.AppInfoGenerator.JISEL_ANNOTATION_PROCESSOR_CLASSNAME;
import static org.jisel.generators.AppInfoGenerator.getPropertyValueFromPropsFile;

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
        this.annotationsGenerator = new AnnotationsGeneratorImpl();
        this.extendsGenerator = new ExtendsGeneratorImpl(this.processingEnvironment);
        this.permitsGenerator = new PermitsGeneratorImpl();
        this.methodsGenerator = new MethodsGeneratorImpl();
        this.declarationGenerator = new DeclarationGeneratorImpl();
    }

    protected void buildJavaxGeneratedAnnotation(StringBuilder classOrInterfaceContent) {
        annotationsGenerator.generateJavaxGeneratedAnnotation(
                classOrInterfaceContent,
                JISEL_ANNOTATION_PROCESSOR_CLASSNAME,
                getPropertyValueFromPropsFile(APPLICATION_PROPERTIES_FILENAME, INFO_APP_VERSION_PROPERTY_NAME, DEFAULT_APP_VERSION)
        );
    }
}