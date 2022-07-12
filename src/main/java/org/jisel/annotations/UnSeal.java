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
package org.jisel.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be applied only on top of large interfaces to segregate.<br>
 * Generates a classic pre-java 17 interfaces hierarchy, which is basically the Interface Segregation Principle applied
 * without sealing the hierarchy. The unsealed hierarchy interfaces are generated additionally to the sealed hierarchy generated files,
 * and stored in the created <i>unsealed</i> sub-package.<br>
 * Each one of the generated interfaces follows the naming convention: <b>&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br>
 * (<b>&#60;LargeInterfaceSimpleName&#62;</b> is the simplename of the large interface being segregated).<br><br>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface UnSeal {

    /**
     * if set to false the "un-sealed" interfaces hierarchy is not generated
     *
     * @return true (default value) if requested to generate the unsealed hierarchy, false if not
     */
    boolean value() default true;
}