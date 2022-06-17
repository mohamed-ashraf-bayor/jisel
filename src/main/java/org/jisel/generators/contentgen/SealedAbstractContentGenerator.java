package org.jisel.generators.contentgen;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.codegen.CodeGenerator;
import org.jisel.generators.codegen.ExtendsGenerator;
import org.jisel.generators.codegen.JavaxGeneratedGenerator;
import org.jisel.generators.codegen.MethodsGenerator;
import org.jisel.generators.codegen.PermitsGenerator;
import org.jisel.generators.codegen.InterfaceExtendsGenerator;
import org.jisel.generators.codegen.InterfaceMethodsGenerator;
import org.jisel.generators.codegen.SealedInterfacePermitsGenerator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO jdoc

/**
 * Generates content of the final class generated for the provided large interface
 * ... ...
 */
public sealed abstract class SealedAbstractContentGenerator implements StringGenerator permits FinalClassContentGenerator, ReportContentGenerator, InterfaceContentGenerator {

    protected final CodeGenerator javaxGeneratedGenerator;
    protected final ExtendsGenerator extendsGenerator;
    protected final PermitsGenerator permitsGenerator;
    protected final MethodsGenerator methodsGenerator;

    /**
     *
     */
    protected SealedAbstractContentGenerator() {
        this.javaxGeneratedGenerator = new JavaxGeneratedGenerator();
        this.extendsGenerator = new InterfaceExtendsGenerator();
        this.permitsGenerator = new SealedInterfacePermitsGenerator();
        this.methodsGenerator = new InterfaceMethodsGenerator();
    }

    /**
     * Generates content of the final class generated for the provided large interface
     *
     * @param processingEnvironment         {@link ProcessingEnvironment} object, needed to access low-level information regarding the used annotations
     * @param largeInterfaceElement         {@link Element} instance of the large interface being segregated
     * @param unSeal                        if true, indicates that additionally to generating the sealed interfaces hierarchy, also generate the classic (non-sealed) interfaces hierarchy.
     *                                      If 'false', only generate the sealed interfaces' hierarchy
     * @param sealedInterfacesToGenerateMap {@link Map} instance containing information about the sealed interface to be generated
     * @param sealedInterfacesPermitsMap    {@link Map} containing information about the subtypes permitted by each one of the sealed interfaces to be generated
     * @return the requested class or interface string content
     */
    public abstract String generateContent(ProcessingEnvironment processingEnvironment,
                                           Element largeInterfaceElement,
                                           boolean unSeal,
                                           Map<String, Set<Element>> sealedInterfacesToGenerateMap,
                                           Map<String, List<String>> sealedInterfacesPermitsMap);
}