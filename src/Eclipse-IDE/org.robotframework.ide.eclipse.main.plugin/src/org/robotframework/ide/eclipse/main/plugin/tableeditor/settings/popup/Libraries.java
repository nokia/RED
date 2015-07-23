package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class Libraries {

    private final List<LibrarySpecification> toImport;
    private final List<LibrarySpecification> imported;

    static Libraries create(final RobotSuiteFile fileModel) {
        final List<LibrarySpecification> standardLibraries = fileModel.getProject().getStandardLibraries();
        final List<LibrarySpecification> referencedLibraries = fileModel.getProject().getReferencedLibraries();

        final List<LibrarySpecification> imported = fileModel.getImportedLibraries();
        final List<LibrarySpecification> toImport = newArrayList(standardLibraries);
        toImport.addAll(referencedLibraries);
        toImport.removeAll(imported);
        return new Libraries(toImport, imported);
    }

    private Libraries(final List<LibrarySpecification> toImport, final List<LibrarySpecification> imported) {
        this.toImport = toImport;
        this.imported = imported;
    }

    List<LibrarySpecification> getLibrariesToImport() {
        return toImport;
    }

    List<LibrarySpecification> getImportedLibraries() {
        return imported;
    }
}