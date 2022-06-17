package org.jisel.generators.codegen;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generates the "extends" clause of a sealed interface definition, along with the list of the parent interfaces
 */
public final class SealedInterfaceExtendsGenerator implements ExtendsGenerator {
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
