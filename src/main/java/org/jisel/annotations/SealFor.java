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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be applied only on top of abstract methods of an interface you intend to segregate.<br>
 * Picked up and processed <b>ONLY</b> if at least 1 of the abstract methods of the large interface has been annotated with &#64;TopLevel.<br>
 * Ignored if combined with &#64;{@link TopLevel} on the same abstract method.<br><br>
 * Expects an array of String values corresponding to the list of profiles you want to seal the method for.<br><br>
 * For each one of the specified profile names, a sealed interface will be generated following the naming convention:
 * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b><br>
 * (<b>&#60;LargeInterfaceSimpleName&#62;</b> corresponds to the simplename of the interface being segregated).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@Repeatable(SealFor.SealFors.class)
public @interface SealFor {

    /**
     * array of String values, each one specifying the profile names to use while segregating the large interface. A sealed interface file will be generated following the naming convention:
     * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b>
     *
     * @return array of profiles names
     */
    String[] value();

    /**
     * Internal annotation allowing &#64;{@link SealFor} to be repeatable
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    @interface SealFors {
        /**
         * array attribute allowing &#64;{@link SealFor} to be repeatable
         *
         * @return array of &#64;SealFor instances
         */
        SealFor[] value();
    }
}