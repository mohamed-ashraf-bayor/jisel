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
package org.jisel.generator;

import javax.lang.model.element.Element;
import javax.lang.model.type.ExecutableType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public final class SealedInterfaceContentGenerator implements StringGenerator {

    private final CodeGenerator javaxGeneratedGenerator;
    private final ExtendsGenerator extendsGenerator;
    private final PermitsGenerator permitsGenerator;
    private final MethodsGenerator methodsGenerator;

    public SealedInterfaceContentGenerator() {
        this.javaxGeneratedGenerator = new JavaxGeneratedGenerator();
        this.extendsGenerator = new JiselExtendsGenerator();
        this.permitsGenerator = new JiselPermitsGenerator();
        this.methodsGenerator = new JiselMethodsGenerator();
    }

    public String generateContent(final Map.Entry<String, Set<Element>> sealedInterfacesToGenerateEntrySet, final Element bloatedInterfaceElement,
                                  final Map<Element, Map<String, List<String>>> sealedInterfacesPermitsByBloatedInterface) {
        var sealedInterfaceContent = new StringBuilder();
        // package name
        generatePackageName(bloatedInterfaceElement).ifPresent(name -> sealedInterfaceContent.append(format("%s %s;%n%n", PACKAGE, name)));
        // javaxgenerated
        javaxGeneratedGenerator.generateCode(sealedInterfaceContent, null);
        // public sealed interface
        var profile = sealedInterfacesToGenerateEntrySet.getKey();
        sealedInterfaceContent.append(format(
                "%s %s ",
                PUBLIC_SEALED_INTERFACE,
                sealedInterfaceNameConvention(profile, bloatedInterfaceElement)
        ));
        // list of extends
        extendsGenerator.generateExtendsClauseFromPermitsMapAndProcessedProfile(sealedInterfaceContent, sealedInterfacesPermitsByBloatedInterface.get(bloatedInterfaceElement), profile, bloatedInterfaceElement);
        // list of permits
        permitsGenerator.generatePermitsClauseFromPermitsMapAndProcessedProfile(sealedInterfaceContent, sealedInterfacesPermitsByBloatedInterface.get(bloatedInterfaceElement), profile, bloatedInterfaceElement);
        // TODO generate permits for Final classes
        // opening bracket after permits list
        sealedInterfaceContent.append(format(" %s%n ", OPENING_BRACKET));
        // list of methods
        methodsGenerator.generateMethodsFromElementsSet(sealedInterfaceContent, sealedInterfacesToGenerateEntrySet.getValue());
        // closing bracket
        sealedInterfaceContent.append(CLOSING_BRACKET);
        //
        return removeDoubleSpaceOccurrences(sealedInterfaceContent.toString());
    }
}

final class JiselExtendsGenerator implements ExtendsGenerator {
    @Override
    public void generateExtendsClauseFromPermitsMapAndProcessedProfile(final StringBuilder sealedInterfaceContent, final Map<String, List<String>> permitsMap, final String processedProfile, final Element bloatedInterfaceElement) {
        Optional.ofNullable(permitsMap).ifPresent(nonNullPermitsMap -> {
            var childrenList = nonNullPermitsMap.entrySet().stream()
                    .filter(permitsMapEntry -> permitsMapEntry.getValue().contains(processedProfile))
                    .map(permitsMapEntry -> sealedInterfaceNameConvention(permitsMapEntry.getKey(), bloatedInterfaceElement))
                    .toList();
            if (!childrenList.isEmpty()) {
                generateCode(sealedInterfaceContent, childrenList);
            }
        });
    }
}

final class JiselPermitsGenerator implements PermitsGenerator {
    @Override
    public void generatePermitsClauseFromPermitsMapAndProcessedProfile(final StringBuilder sealedInterfaceContent, final Map<String, List<String>> permitsMap, final String processedProfile, final Element bloatedInterfaceElement) {
        var permitsMapOpt = Optional.ofNullable(permitsMap);
        if (permitsMapOpt.isPresent() && !permitsMapOpt.get().isEmpty()) {
            Optional.ofNullable(permitsMapOpt.get().get(processedProfile)).ifPresent(childrenList -> generateCode(sealedInterfaceContent, sealedInterfaceNameConventionForList(childrenList, bloatedInterfaceElement)));
        }
    }
}

final class JiselMethodsGenerator implements MethodsGenerator {

    @Override
    public void generateMethodsFromElementsSet(final StringBuilder sealedInterfaceContent, final Set<Element> methodsSet) {
        generateCode(
                sealedInterfaceContent,
                methodsSet.stream()
                        .map(element -> format(
                                "%s %s%s",
                                generateReturnType(element),
                                generateMethodNameAndParameters(element),
                                generateThrownExceptions(element).isEmpty() ? EMPTY_STRING : format(" throws %s", generateThrownExceptions(element))
                        ))
                        .toList()
        );
    }

    private String generateReturnType(final Element methodElement) {
        return ((ExecutableType) methodElement.asType()).getReturnType().toString();
    }

    private String generateMethodNameAndParameters(final Element methodElement) {
        var output = methodElement.toString();
        if (methodHasArguments(methodElement)) {
            int paramIdx = 0;
            while (output.contains(COMMA_SEPARATOR)) {
                output = output.replace(COMMA_SEPARATOR, WHITESPACE + PARAMETER_PREFIX + paramIdx + TEMP_PLACEHOLDER + WHITESPACE);
                paramIdx++;
            }
            output = output.replace(CLOSING_PARENTHESIS, WHITESPACE + PARAMETER_PREFIX + paramIdx + CLOSING_PARENTHESIS).replaceAll(TEMP_PLACEHOLDER, COMMA_SEPARATOR);
        }
        return output;
    }

    private String generateThrownExceptions(final Element methodElement) {
        return ((ExecutableType) methodElement.asType()).getThrownTypes().stream().map(Objects::toString).collect(joining(COMMA_SEPARATOR + WHITESPACE));
    }
}