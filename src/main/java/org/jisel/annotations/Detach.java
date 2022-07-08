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

import static org.jisel.generators.StringGenerator.EMPTY_STRING;

/**
 * // TODO jdoc
 * ...
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(Detach.Detachs.class)
public @interface Detach {

    /**
     * ... Jisel keyword expected: "(toplevel)"
     *
     * @return the profile name to detach
     */
    String profile();

    String rename() default EMPTY_STRING;

    /**
     * allows specifying a list of interfaces to be extended by the detached interface
     *
     * @return an array of .class values
     */
    Class<?>[] superInterfaces() default {};

    Class<?>[] firstSuperInterfaceGenerics() default {};

    Class<?>[] secondSuperInterfaceGenerics() default {};

    Class<?>[] thirdSuperInterfaceGenerics() default {};

    String applyAnnotations() default EMPTY_STRING;

    /**
     * Internal annotation allowing &#64;Detach to be repeatable
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface Detachs {
        /**
         * array attribute allowing &#64;{@link Detach} to be repeatable
         *
         * @return array of &#64;Detach instances
         */
        Detach[] value();
    }
}