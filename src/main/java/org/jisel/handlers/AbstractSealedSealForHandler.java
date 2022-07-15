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
package org.jisel.handlers;

import org.jisel.handlers.impl.SealForAnnotationInfoCollectionHandler;
import org.jisel.handlers.impl.SealForHandler;
import org.jisel.handlers.impl.SealForParentChildInheritanceHandler;

/**
 * Creates and encapsulates needed instances of {@link AbstractSealedAnnotationInfoCollectionHandler} and {@link AbstractSealedParentChildInheritanceHandler}
 */
public abstract sealed class AbstractSealedSealForHandler implements JiselAnnotationHandler permits SealForHandler {

    /**
     * {@link AbstractSealedAnnotationInfoCollectionHandler} instance needed to collect info provided through the
     * &#64;{@link org.jisel.annotations.SealFor} annotation
     */
    protected final AbstractSealedAnnotationInfoCollectionHandler annotationInfoCollectionHandler;

    /**
     * {@link AbstractSealedParentChildInheritanceHandler} instance needed to build info regarding inheritance relationships between
     * the interfaces to be generated
     */
    protected final AbstractSealedParentChildInheritanceHandler parentChildInheritanceHandler;

    /**
     * Instanciates needed instances of {@link AbstractSealedAnnotationInfoCollectionHandler} and {@link AbstractSealedParentChildInheritanceHandler}
     */
    protected AbstractSealedSealForHandler() {
        this.annotationInfoCollectionHandler = new SealForAnnotationInfoCollectionHandler();
        this.parentChildInheritanceHandler = new SealForParentChildInheritanceHandler();
    }
}