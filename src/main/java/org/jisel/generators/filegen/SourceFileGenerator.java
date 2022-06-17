package org.jisel.generators.filegen;

import org.jisel.generators.StringGenerator;

public sealed interface SourceFileGenerator extends StringGenerator permits SealedInterfaceSourceFileGenerator {
}
