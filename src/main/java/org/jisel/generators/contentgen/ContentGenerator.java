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

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ContentGenerator {

    /**
     * Generates content of the final class generated for the provided large interface
     *
     * @param largeInterfaceElement         {@link Element} instance of the large interface being segregated
     * @param unSeal                        if 'true', indicates that additionally to generating the sealed interfaces hierarchy, also generate the classic (non-sealed) interfaces hierarchy.
     *                                      If 'false', only generate the sealed interfaces' hierarchy
     * @param sealedInterfacesToGenerateMap {@link Map} instance containing information about the sealed interface to be generated
     * @param sealedInterfacesPermitsMap    {@link Map} containing information about the subtypes permitted by each one of the sealed interfaces to be generated
     * @return the requested class or interface string content
     */
    String generateContent(Element largeInterfaceElement,
                           boolean unSeal,
                           Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                           Map<String, List<String>> sealedInterfacesPermitsMap);
}