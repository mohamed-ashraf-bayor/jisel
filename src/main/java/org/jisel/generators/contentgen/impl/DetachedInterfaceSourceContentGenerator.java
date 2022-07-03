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

import org.jisel.generators.contentgen.AbstractSealedDetachedInterfaceSourceContentGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.jisel.generators.StringGenerator.CLOSING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.DOT;
import static org.jisel.generators.StringGenerator.OPENING_CURLY_BRACE;
import static org.jisel.generators.StringGenerator.PACKAGE;
import static org.jisel.generators.StringGenerator.UNSEALED;
import static org.jisel.generators.StringGenerator.generatePackageName;
import static org.jisel.generators.StringGenerator.removeDoubleSpaceOccurrences;

/**
 * // TODO jdoc all
 * Generates the content of a detached interface
 * ...
 */
public final class DetachedInterfaceSourceContentGenerator extends AbstractSealedDetachedInterfaceSourceContentGenerator {

    /**
     * TODO jdoc...
     *
     * @param processingEnvironment
     */
    public DetachedInterfaceSourceContentGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public String generateDetachedInterfaceSourceContent(Map<String, Object> detachAttribs) {
        // TODO ...
        return null;
    }
}