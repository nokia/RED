/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

public class DeprecatedSettingHeaderAlias implements ModelUnitValidator {

    private final IFile file;

    private final ValidationReportingStrategy reporter;

    private final RobotSettingsSection section;

    public DeprecatedSettingHeaderAlias(final IFile file, final ValidationReportingStrategy reporter,
            final RobotSettingsSection section) {
        this.file = file;
        this.reporter = reporter;
        this.section = section;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final SettingTable settings = section.getLinkedElement();
        if (settings.isPresent()) {
            for (final TableHeader<? extends ARobotSectionTable> th : settings.getHeaders()) {
                final RobotToken declaration = th.getDeclaration();
                final String text = declaration.getText();
                final String textWithoutWhiteSpaces = text.toLowerCase().replaceAll("\\s", "");
                if (textWithoutWhiteSpaces.toLowerCase().contains("metadata")) {
                    reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.METADATA_TABLE_HEADER_SYNONYM)
                            .formatMessageWith(text), file, declaration);
                }
            }
        }
    }
}
