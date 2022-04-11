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
package org.jisel.generator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.jisel.generator.StringGenerator.removeDoubleSpaceOccurrences;

/**
 * Generates the content of a sealed interface
 */
public final class SealedInterfaceContentGenerator implements StringGenerator {

    private final CodeGenerator javaxGeneratedGenerator;
    private final ExtendsGenerator extendsGenerator;
    private final PermitsGenerator permitsGenerator;
    private final MethodsGenerator methodsGenerator;

    /**
     * SealedInterfaceContentGenerator constructor. Instantiates needed instances of {@link JavaxGeneratedGenerator}, {@link SealedInterfaceExtendsGenerator},
     * {@link SealedInterfacePermitsGenerator} and {@link SealedInterfaceMethodsGenerator}
     */
    public SealedInterfaceContentGenerator() {
        this.javaxGeneratedGenerator = new JavaxGeneratedGenerator();
        this.extendsGenerator = new SealedInterfaceExtendsGenerator();
        this.permitsGenerator = new SealedInterfacePermitsGenerator();
        this.methodsGenerator = new SealedInterfaceMethodsGenerator();
    }

    /**
     * Generates the content of a sealed interface
     *
     * @param processingEnvironment                 {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param sealedInterfacesToGenerateMapEntrySet {@link java.util.Map.Entry} instance containing information about the sealed interfaces to be generated
     * @param largeInterfaceElement                 {@link Element} instance of the large interface being segregated
     * @param sealedInterfacesPermitsMap            Map containing information about the subtypes permitted by each one of the sealed interfaces to be generated
     * @return the string content of the sealed interface to generate
     */
    public String generateSealedInterfaceContent(ProcessingEnvironment processingEnvironment,
                                                 Map.Entry<String, Set<Element>> sealedInterfacesToGenerateMapEntrySet,
                                                 Element largeInterfaceElement,
                                                 Map<String, List<String>> sealedInterfacesPermitsMap) {
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
        sealedInterfaceContent.append(format(" %s%n ", OPENING_CURLY_BRACE));
        // list of methods
        methodsGenerator.generateAbstractMethodsFromElementsSet(sealedInterfaceContent, sealedInterfacesToGenerateMapEntrySet.getValue());
        // closing bracket
        sealedInterfaceContent.append(CLOSING_CURLY_BRACE);
        //
        return removeDoubleSpaceOccurrences(sealedInterfaceContent.toString());
    }
}

/**
 * Generates the "extends" clause of a sealed interface definition, along with the list of the parent interfaces
 */
final class SealedInterfaceExtendsGenerator implements ExtendsGenerator {
    @Override
    public void generateExtendsClauseFromPermitsMapAndProcessedProfile(ProcessingEnvironment processingEnvironment,
                                                                       StringBuilder sealedInterfaceContent,
                                                                       Map<String, List<String>> permitsMap,
                                                                       String processedProfile,
                                                                       Element largeInterfaceElement) {
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

/**
 * Generates the "permits" clause of a sealed interface definition, along with the list of the subtypes classes or
 * interfaces permitted by the sealed interface being generated
 */
final class SealedInterfacePermitsGenerator implements PermitsGenerator {
    @Override
    public void generatePermitsClauseFromPermitsMapAndProcessedProfile(StringBuilder sealedInterfaceContent,
                                                                       Map<String, List<String>> permitsMap,
                                                                       String processedProfile,
                                                                       Element largeInterfaceElement) {
        addFinalClassToPermitsMap(permitsMap, largeInterfaceElement);
        var permitsMapOpt = Optional.ofNullable(permitsMap);
        if (permitsMapOpt.isPresent() && !permitsMapOpt.get().isEmpty()) {
            Optional.ofNullable(permitsMapOpt.get().get(processedProfile)).ifPresent(
                    childrenList -> generateCode(sealedInterfaceContent, sealedInterfaceNameConventionForList(childrenList, largeInterfaceElement))
            );
        }
    }
}

/**
 * Generates the list of abstracts methods of a sealed interface being generated
 */
final class SealedInterfaceMethodsGenerator implements MethodsGenerator {

    @Override
    public void generateAbstractMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet) {
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
    public void generateEmptyConcreteMethodsFromElementsSet(StringBuilder sealedInterfaceContent, Set<Element> methodsSet) {
        generateCode(
                sealedInterfaceContent,
                methodsSet.stream()
                        .map(methodElement -> format(
                                "public %s %s %s",
                                generateReturnType(methodElement),
                                generateMethodNameAndParameters(methodElement),
                                generateThrownExceptions(methodElement).isEmpty()
                                        ? OPENING_CURLY_BRACE + generateDefaultReturnValueForMethod(methodElement) + SEMICOLON + CLOSING_CURLY_BRACE
                                        : format("throws %s",
                                        generateThrownExceptions(methodElement) + WHITESPACE + OPENING_CURLY_BRACE
                                                + generateDefaultReturnValueForMethod(methodElement) + SEMICOLON + CLOSING_CURLY_BRACE
                                )
                        ))
                        .toList()
        );
    }
}