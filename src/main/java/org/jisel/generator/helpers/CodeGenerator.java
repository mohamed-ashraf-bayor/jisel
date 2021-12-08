/**
 * Copyright (c) 2021-2022 Mohamed Ashraf Bayor.
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
package org.jisel.generator.helpers;

import java.util.Map;

/**
 * Exposes contract for a CodeGenerator class to fulfill
 */
public sealed interface CodeGenerator permits JavaxGeneratedGenerator {

    // List of the parameters expected in the params Map object of the generateCode method:

    /**
     * parameter name: "qualifiedClassName", expected type: String
     */
    String QUALIFIED_CLASS_NAME = "qualifiedClassName";

    /**
     * parameter name: "fieldName", expected type: String
     */
    String FIELD_NAME = "fieldName";

    /**
     * parameter name: "getterReturnType", expected type: String
     */
    String GETTER_RETURN_TYPE = "getterReturnType";

    /**
     * parameter name: "getterAsString", expected type: String
     */
    String GETTER_AS_STRING = "getterAsString";

    /**
     * parameter name: "gettersList", expected type: List&lt;? extends javax.lang.model.element.Element&gt;, ex:[getLastname(), getAge(), getMark(), getGrade(), getSchool()]
     */
    String GETTERS_LIST = "gettersList";

    /**
     * parameter name: "gettersMap", expected type: Map&lt;String, String&gt;, ex: {getAge=int, getSchool=org.froporec.data1.School, getLastname=java.lang.String}
     */
    String GETTERS_MAP = "gettersMap";

    /**
     * Generates piece of code requested, based on the parameters provided in the params object and appends it to the provided recordClassContent param
     * @param recordClassContent Stringbuilder object containing the record class code being generated
     * @param params expected parameters. restricted to what is expected by the implementing class. the expected parameters names are defined as constants in the CodeGenerator interface.
     */
    void generateCode(StringBuilder recordClassContent, Map<String, Object> params);
}