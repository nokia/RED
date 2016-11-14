/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Function;
import com.google.common.base.Optional;

class Settings {

    private final List<LibrarySpecification> toImport;
    private final List<LibrarySpecification> imported;

    private final List<IPath> importedResources;

    private final List<ImportArguments> importedVariables;

    static Settings create(final RobotSuiteFile fileModel) {
        final List<LibrarySpecification> imported = newArrayList(fileModel.getImportedLibraries().keySet());
        final List<LibrarySpecification> toImport = newArrayList(fileModel.getProject().getLibrariesSpecifications());
        toImport.removeAll(imported);
        final List<IPath> importedResources = transform(fileModel.getResourcesPaths(), new Function<String, IPath>() {
            @Override
            public IPath apply(final String path) {
                return new Path(path);
            }
        });
        final List<ImportArguments> importedVariables = getImportedVariables(fileModel);
        return new Settings(toImport, imported, importedResources, importedVariables);
    }

    private static List<ImportArguments> getImportedVariables(final RobotSuiteFile fileModel) {
        final Optional<RobotSettingsSection> section = fileModel.findSection(RobotSettingsSection.class);
        final List<ImportArguments> alreadyImported = newArrayList();
        if (section.isPresent()) {
            for (final RobotElement element : section.get().getVariablesSettings()) {
                final RobotSetting setting = (RobotSetting) element;
                alreadyImported.add(new ImportArguments(setting.getArguments()));
            }
            return alreadyImported;
        }
        return newArrayList();
    }

    private Settings(final List<LibrarySpecification> toImport, final List<LibrarySpecification> imported,
            final List<IPath> importedResources, final List<ImportArguments> importedVariables) {
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

    public List<ImportArguments> getImportedVariablesArguments() {
        return importedVariables;
    }

    public static class ImportArguments {

        private List<String> args;

        public ImportArguments(final List<String> args) {
            this.args = args;
        }

        public List<String> getArgs() {
            return args;
        }

        public void setArgs(final List<String> args) {
            this.args = args;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == ImportArguments.class) {
                final ImportArguments other = (ImportArguments) obj;
                return Objects.equals(args, other.args);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return args.hashCode();
        }
    }
}