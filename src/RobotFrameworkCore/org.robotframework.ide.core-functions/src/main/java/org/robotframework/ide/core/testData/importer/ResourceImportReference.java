package org.robotframework.ide.core.testData.importer;

import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;


public class ResourceImportReference {

    private ResourceImport importDeclaration;
    private IRobotFileOutput reference;


    public ResourceImportReference(final ResourceImport importDeclaration,
            final IRobotFileOutput reference) {
        this.importDeclaration = importDeclaration;
        this.reference = reference;
    }


    public ResourceImport getImportDeclaration() {
        return importDeclaration;
    }


    public IRobotFileOutput getReference() {
        return reference;
    }
}
