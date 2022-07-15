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
package org.jisel.generators.filegen;

import javax.lang.model.element.Element;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exposes contract to fulfill by classes generating Java source files
 */
public sealed interface SourceFileGenerator permits AbstractSealedSourceFileGenerator {

    /**
     * Creates source files based on provided parameters and returns a {@link List} of the generated interfaces and classes qualified names
     *
     * @param sealedInterfacesToGenerateByLargeInterface   {@link Map} containing information about the sealed interfaces to
     *                                                     be generated for each large interface
     * @param sealedInterfacesPermitsByLargeInterface      {@link Map} containing information about the subtypes permitted by
     *                                                     each one of the sealed interfaces to be generated for each large interface
     * @param unSealValueByLargeInterface                  {@link Map} storing 'unSeal' boolean value for each large interface
     * @param detachedInterfacesToGenerateByLargeInterface {@link Map} containing information about the detached interfaces to
     *                                                     be generated for each large interface
     * @return {@link List} of the generated interfaces and classes qualified names
     * @throws IOException if an I/O error occured
     */
    List<String> createSourceFiles(Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerateByLargeInterface,
                                   Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByLargeInterface,
                                   Map<Element, Boolean> unSealValueByLargeInterface,
                                   Map<Element, Map<String, Map<String, Object>>> detachedInterfacesToGenerateByLargeInterface) throws IOException;
}