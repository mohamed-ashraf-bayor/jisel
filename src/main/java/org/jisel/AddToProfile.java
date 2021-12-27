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

import static org.jisel.generator.StringGenerator.EMPTY_STRING;

/**
 * Annotation to be applied on top of a class, an interface or a record which is implementing or extending a sealed interface generated by Jisel.<br><br>
 * All sealed interfaces generated by Jisel follow the naming convention: <b>Sealed&#60;ProfileName&#62;&#60;LargeInterfaceSimpleName&#62;</b>.<br>
 * See &#64;AddToProfile attributes documentation.<br><br>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@Repeatable(AddToProfile.AddToProfilez.class)
public @interface AddToProfile {

    /**
     * <i>Not Required</i> - specifies the name of one of the profiles used with the &#64;SealForProfile annotations in the large interface definition.
     * Also corresponds to the <b>&#60;ProfileName&#62;</b> from the sealed interfaces naming convention.<br>
     * If not provided or empty, the annotated class, interface or record will be added to the permits list of the generated parent sealed interface.<br>
     * Also, the provided profile attribute value MUST be one of the profiles defined in the large interface definition using &#64;SealForProfile. If not,
     * the specified profile will be ignored and an informational message regarding provided incorrect profiles will be printed during the compilation.<br>
     *
     * @return the profile name
     */
    String profile() default EMPTY_STRING;

    /**
     * <i>Required</i> - MUST be the Fully Qualified Name of the large interface. That would be the <b>&#60;LargeInterfaceSimpleName&#62;</b> as seen in
     * the sealed interface name convention, preceded by the package name.<br>
     *
     * @return the large interface fully qualified name
     */
    String largeInterface();

    /**
     * Internal annotation allowing &#64;AddToProfile to be repeatable
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface AddToProfilez {
        /**
         * array attribute allowing &#64;AddToProfile to be repeatable
         *
         * @return array of &#64;AddToProfile instances
         */
        AddToProfile[] value();
    }
}