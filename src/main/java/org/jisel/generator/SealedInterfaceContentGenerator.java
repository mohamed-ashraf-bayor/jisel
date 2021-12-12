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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

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

    public String generateSealedInterfaceContent(final ProcessingEnvironment processingEnvironment,
                                                 final Map.Entry<String, Set<Element>> sealedInterfacesToGenerateMapEntrySet,
                                                 final Element largeInterfaceElement,
                                                 final Map<String, List<String>> sealedInterfacesPermitsMap) {
        var sealedInterfaceContent = new StringBuilder();
        // package name
        generatePackageName(largeInterfaceElement).ifPresent(name -> sealedInterfaceContent.append(format("%s %s;%n%n", PACKAGE, name)));
        // javaxgenerated
        javaxGeneratedGenerator.generateCode(sealedInterfaceContent, null);
        // public sealed interface
        var profile = sealedInterfacesToGenerateMapEntrySet.getKey();
        sealedInterfaceContent.append(format(
                "%s %s ",
                PUBLIC_SEALED_INTERFACE,
                sealedInterfaceNameConvention(profile, largeInterfaceElement)
        ));
        // list of extends
        extendsGenerator.generateExtendsClauseFromPermitsMapAndProcessedProfile(processingEnvironment, sealedInterfaceContent, sealedInterfacesPermitsMap, profile, largeInterfaceElement);
        // list of permits
        permitsGenerator.generatePermitsClauseFromPermitsMapAndProcessedProfile(sealedInterfaceContent, sealedInterfacesPermitsMap, profile, largeInterfaceElement);
        // opening bracket after permits list
        sealedInterfaceContent.append(format(" %s%n ", OPENING_BRACKET));
        // list of methods
        methodsGenerator.generateAbstractMethodsFromElementsSet(sealedInterfaceContent, sealedInterfacesToGenerateMapEntrySet.getValue());
        // closing bracket
        sealedInterfaceContent.append(CLOSING_BRACKET);
        //
        return removeDoubleSpaceOccurrences(sealedInterfaceContent.toString());
    }
}

final class JiselExtendsGenerator implements ExtendsGenerator {
    @Override
    public void generateExtendsClauseFromPermitsMapAndProcessedProfile(final ProcessingEnvironment processingEnvironment,
                                                                       final StringBuilder sealedInterfaceContent,
                                                                       final Map<String, List<String>> permitsMap,
                                                                       final String processedProfile,
                                                                       final Element largeInterfaceElement) {
        Optional.ofNullable(permitsMap).ifPresent(nonNullPermitsMap -> {
            var parentList = nonNullPermitsMap.entrySet().stream()
                    .filter(permitsMapEntry -> permitsMapEntry.getValue().contains(processedProfile))
                    .map(permitsMapEntry -> sealedInterfaceNameConvention(permitsMapEntry.getKey(), largeInterfaceElement))
                    .toList();
            if (!parentList.isEmpty()) {
                generateCode(sealedInterfaceContent, parentList);
            } else {
                // only for largeInterface sealed interface generation, add interfaces it extends if any
                var superInterfacesList = processingEnvironment.getTypeUtils().directSupertypes(largeInterfaceElement.asType()).stream()
                        .map(Object::toString)
                        .filter(superType -> !superType.contains(JAVA_LANG_OBJECT))
                        .toList();
                if (largeInterfaceElement.getSimpleName().toString().equals(processedProfile) && !superInterfacesList.isEmpty()) {
                    generateCode(
                            sealedInterfaceContent,
                            superInterfacesList
                    );
                }
            }
        });
    }
}

final class JiselPermitsGenerator implements PermitsGenerator {
    @Override
    public void generatePermitsClauseFromPermitsMapAndProcessedProfile(final StringBuilder sealedInterfaceContent,
                                                                       final Map<String, List<String>> permitsMap,
                                                                       final String processedProfile,
                                                                       final Element largeInterfaceElement) {
        addFinalClassToPermitsMap(permitsMap, largeInterfaceElement);
        var permitsMapOpt = Optional.ofNullable(permitsMap);
        if (permitsMapOpt.isPresent() && !permitsMapOpt.get().isEmpty()) {
            Optional.ofNullable(permitsMapOpt.get().get(processedProfile)).ifPresent(
                    childrenList -> generateCode(sealedInterfaceContent, sealedInterfaceNameConventionForList(childrenList, largeInterfaceElement))
            );
        }
    }
}

final class JiselMethodsGenerator implements MethodsGenerator {

    @Override
    public void generateAbstractMethodsFromElementsSet(final StringBuilder sealedInterfaceContent, final Set<Element> methodsSet) {
        generateCode(
                sealedInterfaceContent,
                methodsSet.stream()
                        .map(element -> format(
                                "%s %s%s",
                                generateReturnType(element),
                                generateMethodNameAndParameters(element),
                                generateThrownExceptions(element).isEmpty()
                                        ? SEMICOLON
                                        : format(" throws %s", generateThrownExceptions(element) + SEMICOLON)
                        ))
                        .toList()
        );
    }

    @Override
    public void generateEmptyConcreteMethodsFromElementsSet(final StringBuilder sealedInterfaceContent, final Set<Element> methodsSet) {
        generateCode(
                sealedInterfaceContent,
                methodsSet.stream()
                        .map(methodElement -> format(
                                "public %s %s %s",
                                generateReturnType(methodElement),
                                generateMethodNameAndParameters(methodElement),
                                generateThrownExceptions(methodElement).isEmpty()
                                        ? OPENING_BRACKET + generateDefaultReturnValueForMethod(methodElement) + SEMICOLON + CLOSING_BRACKET
                                        : format("throws %s", generateThrownExceptions(methodElement) + WHITESPACE + OPENING_BRACKET + generateDefaultReturnValueForMethod(methodElement) + SEMICOLON + CLOSING_BRACKET)
                        ))
                        .toList()
        );
    }
}