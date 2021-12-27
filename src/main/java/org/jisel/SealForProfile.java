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
package org.jisel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be applied only on top of abstract methods of an interface you intend to segregate.<br><br>
 * For the specified profile name, a sealed interface will be generated following the naming convention:
 * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b>.<br><br>
 * <b>&#60;LargeInterfaceSimpleName&#62;</b> corresponds to the simplename of the interface being segregated.<br><br>
 * See &#64;SealForProfile attributes documentation.<br><br>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@Repeatable(SealForProfile.SealForProfilez.class)
public @interface SealForProfile {

    /**
     * Profile name to use while segregation the large interface. A sealed interface file will be generated following the naming convention:
     * <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b>
     *
     * @return a string containing the profile name
     */
    String value();

    /**
     * Internal annotation allowing &#64;SealForProfile to be repeatable
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    @interface SealForProfilez {
        /**
         * array attribute allowing &#64;SealForProfile to be repeatable
         *
         * @return array of &#64;SealForProfile instances
         */
        SealForProfile[] value();
    }
}