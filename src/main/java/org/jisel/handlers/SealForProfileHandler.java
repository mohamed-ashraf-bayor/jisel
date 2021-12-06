package org.jisel.handlers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

public final class SealForProfileHandler implements JiselAnnotationHandler {

    @Override
    public Map<Element, String> handleAnnotatedElements(final ProcessingEnvironment processingEnv,
                                                        final Set<Element> allAnnotatedElements,
                                                        final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                                        final Map<Element, Map<String, List<String>>> sealedInterfacesPermits) {
        new SealForProfileInfoCollectionHandler().populateSealedInterfacesMap(processingEnv, allAnnotatedElements, sealedInterfacesToGenerate);
        var statusReport = new SealForProfileUniqueParentInterfaceHandler().checkAndHandleUniqueParentInterface(sealedInterfacesToGenerate);
        new SealForProfileParentChildInheritanceHandler().buildInheritanceRelations(sealedInterfacesToGenerate, sealedInterfacesPermits);
        System.out.println(" \n\n>>>$$>>>>>>$$$>>>>$$>>>>> sealedInterfacesPermits: " + sealedInterfacesPermits);
        System.out.println(" \n\n>>>>>>>>>>>>>>>>>> sealedInterfacesToGenerate: " + sealedInterfacesToGenerate);
        return statusReport;
    }
}

final class SealForProfileInfoCollectionHandler implements AnnotationInfoCollectionHandler {

    @Override
    public void populateSealedInterfacesMap(final ProcessingEnvironment processingEnv,
                                            final Set<Element> allAnnotatedElements,
                                            final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate) {

        var annotatedMethodsByInterface = allAnnotatedElements.stream()
                .filter(element -> ElementKind.METHOD.equals(element.getKind()))
                .filter(element -> ElementKind.INTERFACE.equals(element.getEnclosingElement().getKind()))
                .collect(groupingBy(Element::getEnclosingElement, toSet()));

        if (annotatedMethodsByInterface.isEmpty()) {
            return;
        }

        var annotatedMethodsByProfileByInterface = new HashMap<Element, Map<String, Set<Element>>>();
        annotatedMethodsByInterface.forEach(
                (interfaceElement, annotatedMethodsElements) -> annotatedMethodsElements.forEach(
                        annotatedMethod -> processingEnv.getElementUtils().getAllAnnotationMirrors(annotatedMethod).forEach(
                                annotationMirror -> annotationMirror.getElementValues().entrySet().forEach(
                                        entry -> extractProfilesAndPopulateMaps(interfaceElement, entry.getValue().toString(), annotatedMethod, annotatedMethodsByProfileByInterface)
                                )
                        )
                )
        );

        createParentInterfacesBasedOnCommonMethods(annotatedMethodsByProfileByInterface, sealedInterfacesToGenerate);
    }

    // annotationValueAsString: sample values: singl value "profile1name", array: {@org.jisel.SealForProfile("profile2name"), @org.jisel.SealForProfile("profile3name"),...}
    private void extractProfilesAndPopulateMaps(final Element interfaceElement,
                                                final String annotationValueAsString,
                                                final Element annotatedMethod,
                                                final Map<Element, Map<String, Set<Element>>> annotatedMethodsByProfileByInterface) {
        var matcher = Pattern.compile("\"([^\"]*)\"").matcher(annotationValueAsString);
        while (matcher.find()) {
            var profile = matcher.group(1).trim();
            if (profile.isBlank()) {
                continue;
            }
            if (annotatedMethodsByProfileByInterface.containsKey(interfaceElement)) {
                annotatedMethodsByProfileByInterface.get(interfaceElement).merge(
                        profile,
                        new HashSet<>(Set.of(annotatedMethod)),
                        (currentSet, newSet) -> concat(currentSet.stream(), newSet.stream()).collect(toSet())
                );
            } else {
                annotatedMethodsByProfileByInterface.put(interfaceElement, new HashMap<>(Map.of(profile, new HashSet<>(Set.of(annotatedMethod)))));
            }
        }
    }
}

final class SealForProfileParentChildInheritanceHandler implements ParentChildInheritanceHandler {

    @Override
    public void buildInheritanceRelations(final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate,
                                          final Map<Element, Map<String, List<String>>> sealedInterfacesPermits) {

        sealedInterfacesToGenerate.keySet().forEach(interfaceElement -> {

            sealedInterfacesPermits.put(interfaceElement, new HashMap<>()); // start with initializing sealedInterfacesPermits with empty mutable maps

            // promote profiles with empty methods to parent level
            var allProfilesToRemove = new HashSet<String>();
            sealedInterfacesToGenerate.get(interfaceElement).keySet().forEach(concatenatedProfiles -> {
                var profilesArray = concatenatedProfiles.split(SEPARATOR);
                if (profilesArray.length > 1) {
                    for (var profile : profilesArray) {
                        var profileMethodsOpt = Optional.ofNullable(sealedInterfacesToGenerate.get(interfaceElement).get(profile));
                        if (profileMethodsOpt.isPresent() && profileMethodsOpt.get().isEmpty()) {
                            sealedInterfacesToGenerate.get(interfaceElement).put(profile, sealedInterfacesToGenerate.get(interfaceElement).get(concatenatedProfiles));
                            sealedInterfacesPermits.get(interfaceElement).put(profile, Arrays.stream(profilesArray).filter(profileName -> !profile.equals(profileName)).toList());
                            allProfilesToRemove.add(concatenatedProfiles);
                            break;
                        }
                    }
                }
            });

            allProfilesToRemove.forEach(sealedInterfacesToGenerate.get(interfaceElement)::remove);

            // and completing building sealedInterfacesPermits map
            buildSealedInterfacesPermitsMap(interfaceElement, sealedInterfacesToGenerate, sealedInterfacesPermits);
        });
    }
}

final class SealForProfileUniqueParentInterfaceHandler implements UniqueParentInterfaceHandler {

    @Override
    public Map<Element, String> checkAndHandleUniqueParentInterface(final Map<Element, Map<String, Set<Element>>> sealedInterfacesToGenerate) {
        var statusReport = new HashMap<Element, String>();
        Map<Element, Optional<String>> longestConcatenedProfilesStringOptByInterface = checkUniqueParentInterfacePresence(sealedInterfacesToGenerate);
        sealedInterfacesToGenerate.keySet().forEach(interfaceElement -> {
            var longestConcatenedProfilesStringOpt = longestConcatenedProfilesStringOptByInterface.get(interfaceElement);
            if (longestConcatenedProfilesStringOptByInterface.containsKey(interfaceElement) && longestConcatenedProfilesStringOpt.isPresent()) {
                sealedInterfacesToGenerate.get(interfaceElement).put(interfaceElement.getSimpleName().toString(), sealedInterfacesToGenerate.get(interfaceElement).get(longestConcatenedProfilesStringOpt.get()));
                sealedInterfacesToGenerate.get(interfaceElement).remove(longestConcatenedProfilesStringOpt.get());
            } else {
                statusReport.put(interfaceElement, REPORT_MSG);
            }
        });
        return statusReport;
    }
}