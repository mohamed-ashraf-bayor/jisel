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

import org.jisel.generators.contentgen.impl.DetachedInterfaceSourceContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exposes contract to fulfill by classes generating content of detached interfaces
 */
public abstract sealed class AbstractSealedDetachedInterfaceSourceContentGenerator extends AbstractSealedSourceContentGenerator
        permits DetachedInterfaceSourceContentGenerator {

    /**
     * Passes through the received {@link ProcessingEnvironment} instance to the super constructor
     *
     * @param processingEnvironment {@link ProcessingEnvironment} instance needed for report content generation
     */
    protected AbstractSealedDetachedInterfaceSourceContentGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    /**
     * Generates the detached interface String content
     *
     * @param detachedInterfaceQualifiedName qualified name of the detached interface being generated
     * @param detachAttribs                  {@link Map} storing all attributes passed through the &#64;{@link org.jisel.annotations.Detach} annotation
     *                                       additionally to the abstract methods of the detached interface being generated
     * @param largeInterfaceElement          {@link Element} instance of the large interface to process
     * @return the detached interface String content
     */
    public abstract String generateDetachedInterfaceSourceContent(String detachedInterfaceQualifiedName,
                                                                  Map<String, Object> detachAttribs,
                                                                  Element largeInterfaceElement);

    @Override
    public String generateSourceContent(Element largeInterfaceElement,
                                        boolean unSeal,
                                        Map.Entry<String, Set<Element>> sealedInterfaceToGenerate,
                                        Map<String, List<String>> sealedInterfacesPermitsMap) {
        throw new UnsupportedOperationException("Call generateDetachedInterfaceSourceContent(String, Map<String, Object>, Element) method instead");
    }
}