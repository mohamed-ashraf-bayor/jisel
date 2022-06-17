package org.jisel.generators.codegen;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generates the "permits" clause of a sealed interface definition, along with the list of the subtypes classes or
 * interfaces permitted by the sealed interface being generated
 */
public final class SealedInterfacePermitsGenerator implements PermitsGenerator {
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