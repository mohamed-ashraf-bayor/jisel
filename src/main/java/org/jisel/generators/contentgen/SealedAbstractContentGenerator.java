package org.jisel.generators.contentgen;

import org.jisel.generators.StringGenerator;
import org.jisel.generators.codegen.CodeGenerator;
import org.jisel.generators.codegen.ExtendsGenerator;
import org.jisel.generators.codegen.JavaxGeneratedGenerator;
import org.jisel.generators.codegen.MethodsGenerator;
import org.jisel.generators.codegen.PermitsGenerator;
import org.jisel.generators.codegen.SealedInterfaceExtendsGenerator;
import org.jisel.generators.codegen.SealedInterfaceMethodsGenerator;
import org.jisel.generators.codegen.SealedInterfacePermitsGenerator;

// TODO jdoc
public sealed abstract class SealedAbstractContentGenerator implements StringGenerator permits FinalClassContentGenerator, ReportContentGenerator, SealedInterfaceContentGenerator {

    protected final CodeGenerator javaxGeneratedGenerator;
    protected final ExtendsGenerator extendsGenerator;
    protected final PermitsGenerator permitsGenerator;
    protected final MethodsGenerator methodsGenerator;

    protected SealedAbstractContentGenerator() {
        this.javaxGeneratedGenerator = new JavaxGeneratedGenerator();
        this.extendsGenerator = new SealedInterfaceExtendsGenerator();
        this.permitsGenerator = new SealedInterfacePermitsGenerator();
        this.methodsGenerator = new SealedInterfaceMethodsGenerator();
    }
}