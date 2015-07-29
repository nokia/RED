package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class Settings {

    private final List<LibrarySpecification> toImport;
    private final List<LibrarySpecification> imported;
    private final List<IPath> importedResources;

    static Settings create(final RobotSuiteFile fileModel) {
        final List<LibrarySpecification> standardLibraries = fileModel.getProject().getStandardLibraries();
        final List<LibrarySpecification> referencedLibraries = fileModel.getProject().getReferencedLibraries();

        final List<LibrarySpecification> imported = fileModel.getImportedLibraries();
        final List<LibrarySpecification> toImport = newArrayList(standardLibraries);
        toImport.addAll(referencedLibraries);
        toImport.removeAll(imported);
        final List<IPath> importedResources = fileModel.getResourcesPaths();
        return new Settings(toImport, imported, importedResources);
    }

    private Settings(final List<LibrarySpecification> toImport, final List<LibrarySpecification> imported,
            final List<IPath> importedResources) {
        this.toImport = toImport;
        this.imported = imported;
        this.importedResources = importedResources;
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

}