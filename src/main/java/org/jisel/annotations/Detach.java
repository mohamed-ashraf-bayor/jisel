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
 * Repeatable annotation to apply on top of a large interface being segregated. <br>
 * Expects a mandatory <i>profile</i> attribute String value corresponding to one of the profiles provided using the <i>@SealFor</i> annotation. <br>
 * Result will be the generation of an (unsealed) interface for the specified profile. The generated interface contains all abstract methods which have been tagged for the specified profile (through the use of <i>@SealFor</i>).<br>
 * Also, as the generated interface is "detached" from the generated sealed hierarchy, no inheritance declaration clause (<i>extends</i>) is generated. <br>
 * Flexibility is offered, allowing to choose a new name for the generated interface, specify which superInterfaces (along with generics) the generated interface should extend, and list qualified names of annotations (along with their attributes/values) to be added on top of the generated interface. <br>
 * All generated detached interfaces are stored in the created <i>detached</i> sub-package.<br>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(Detach.Detachs.class)
public @interface Detach {

    /**
     * Allows to specify the name of the profile to be "detached" from the sealed hierarchy.<br>
     * In case you need to detach the Top Level profile, then use the Jisel keyword <b>"(toplevel)"</b>
     *
     * @return the profile name to detach
     */
    String profile();

    /**
     * Allows to rename the generated interface for the specified profile name.<br>
     * If empty or not provided, the interface is generated with the profile name.
     *
     * @return the interface name to be used
     */
    String rename() default EMPTY_STRING;

    /**
     * Allows to specify a list of interfaces to be extended by the generated detached interface
     *
     * @return an array of .class values
     */
    Class<?>[] superInterfaces() default {};

    /**
     * Allows to specify an array of .class values to be used as generics for the first superInterface specified though the superInterfaces attribute
     *
     * @return an array of .class values
     */
    Class<?>[] firstSuperInterfaceGenerics() default {};

    /**
     * Allows to specify an array of .class values to be used as generics for the second superInterface specified though the superInterfaces attribute
     *
     * @return an array of .class values
     */
    Class<?>[] secondSuperInterfaceGenerics() default {};

    /**
     * Allows to specify an array of .class values to be used as generics for the third superInterface specified though the superInterfaces attribute
     *
     * @return an array of .class values
     */
    Class<?>[] thirdSuperInterfaceGenerics() default {};

    /**
     * Allows to specify 1 or multiple annotations to be applied on top of the generated interface.<br>
     * In case multiple annotations need to be specified, the use of Java text blocks is recommended for clarity.
     *
     * @return a String containing all annotations to be applied
     */
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