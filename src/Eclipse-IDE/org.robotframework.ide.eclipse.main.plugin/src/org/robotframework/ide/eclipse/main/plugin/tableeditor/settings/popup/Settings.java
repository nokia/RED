/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile.ImportedVariablesFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class Settings {

    private final List<LibrarySpecification> toImport;
    private final List<LibrarySpecification> imported;
    private final List<IPath> importedResources;
    private final List<ImportedVariablesFile> importedVariables;

    static Settings create(final RobotSuiteFile fileModel) {
        final List<LibrarySpecification> imported = fileModel.getImportedLibraries();
        final List<LibrarySpecification> toImport = newArrayList(fileModel.getProject().getLibrariesSpecifications());
        toImport.removeAll(imported);
        final List<IPath> importedResources = fileModel.getResourcesPaths();
        final List<ImportedVariablesFile> importedVariables = fileModel.getImportedVariables();
        return new Settings(toImport, imported, importedResources, importedVariables);
    }

    private Settings(final List<LibrarySpecification> toImport, final List<LibrarySpecification> imported,
            final List<IPath> importedResources, final List<ImportedVariablesFile> importedVariables) {
        this.toImport = toImport;
        this.imported = imported;
        this.importedResources = importedResources;
        this.importedVariables = importedVariables;
    }

    List<LibrarySpecification> getLibrariesToImport() {
        return toImport;
    }

    List<LibrarySpecification> getImportedLibraries() {
        return imported;
    }

    public List<IPath> getImportedResources() {
        return importedResources;
    }

    public List<ImportedVariablesFile> getImportedVariables() {
        return importedVariables;
    }

}