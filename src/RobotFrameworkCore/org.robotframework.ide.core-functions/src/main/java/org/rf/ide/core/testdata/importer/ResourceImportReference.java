/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.importer;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;

public class ResourceImportReference {

    private ResourceImport importDeclaration;

    private RobotFileOutput reference;

    public ResourceImportReference(final ResourceImport importDeclaration, final RobotFileOutput reference) {
        this.importDeclaration = importDeclaration;
        this.reference = reference;
    }

    public ResourceImport getImportDeclaration() {
        return importDeclaration;
    }

    public RobotFileOutput getReference() {
        return reference;
    }

    public void updateReference(final RobotFileOutput reference) {
        this.reference = reference;
    }
}
