/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.AImported.Type;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

public class LibraryAliasesDeclarationUpperCaseValidator implements ModelUnitValidator {

    private final IFile file;

    private final ValidationReportingStrategy reporter;

    private final RobotSettingsSection section;

    public LibraryAliasesDeclarationUpperCaseValidator(final IFile file, final ValidationReportingStrategy reporter,
            final RobotSettingsSection section) {
        this.file = file;
        this.reporter = reporter;
        this.section = section;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final SettingTable settingTable = section.getLinkedElement();
        if (settingTable.isPresent()) {
            for (final AImported imported : settingTable.getImports()) {
                if (imported.getType() == Type.LIBRARY) {
                    final LibraryImport libImport = (LibraryImport) imported;
                    final LibraryAlias alias = libImport.getAlias();
                    if (alias.isPresent()) {
                        final RobotToken declaration = alias.getDeclaration();
                        final String raw = declaration.getRaw();
                        if (!isUpperCaseAliasesWITH_NAME(raw)) {
                            reporter.handleProblem(RobotProblem
                                    .causedBy(GeneralSettingsProblem.LIBRARY_WITH_NAME_NOT_UPPER_CASE_COMBINATION)
                                    .formatMessageWith(raw), file, declaration);
                        }
                    }
                }
            }
        }
    }

    private final boolean isUpperCaseAliasesWITH_NAME(final String aliasesText) {
        boolean result = true;
        final char[] aliasTextCA = aliasesText.toCharArray();
        for (final char c : aliasTextCA) {
            if (Character.isLetter(c)) {
                if (Character.isLowerCase(c)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}
