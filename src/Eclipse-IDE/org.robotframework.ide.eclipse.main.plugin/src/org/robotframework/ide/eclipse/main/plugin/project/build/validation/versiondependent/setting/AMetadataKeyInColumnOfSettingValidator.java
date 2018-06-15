/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;

public abstract class AMetadataKeyInColumnOfSettingValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSettingsSection section;

    private final ValidationReportingStrategy reporter;

    public AMetadataKeyInColumnOfSettingValidator(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.section = section;
        this.reporter = reporter;
    }

    public abstract IProblemCause getSettingProblemId();

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final SettingTable table = section.getLinkedElement();

        final List<Metadata> metadatas = table.getMetadatas();
        for (final Metadata metadata : metadatas) {
            if (isOldSyntax(metadata, table)) {
                final RobotToken settingDeclaration = metadata.getDeclaration();
                reporter.handleProblem(
                        RobotProblem.causedBy(getSettingProblemId()).formatMessageWith(settingDeclaration.getText()),
                        file, settingDeclaration);
            }
        }
    }

    private boolean isOldSyntax(final Metadata metadata, final SettingTable settings) {
        final RobotToken settingDeclaration = metadata.getDeclaration();
        final String settingText = settingDeclaration.getText();
        if ("meta:".equalsIgnoreCase(settingText.trim())) {
            if (settingDeclaration.getEndColumn() + 1 == metadata.getKey().getStartColumn()) {
                final RobotFile model = settings.getParent();
                final Optional<Integer> robotLineIndexBy = model
                        .getRobotLineIndexBy(metadata.getBeginPosition().getOffset());
                if (robotLineIndexBy.isPresent()) {
                    final RobotLine robotLine = model.getFileContent().get(robotLineIndexBy.get());
                    final Optional<Integer> elementPositionInLine = robotLine
                            .getElementPositionInLine(settingDeclaration);
                    if (elementPositionInLine.isPresent()) {
                        final Integer metaDeclarationPos = elementPositionInLine.get();
                        final List<IRobotLineElement> lineElements = robotLine.getLineElements();
                        return metaDeclarationPos < lineElements.size()
                                && lineElements.get(metaDeclarationPos + 1).getTypes().contains(
                                        RobotTokenType.PRETTY_ALIGN_SPACE);
                    }
                }
            }
        }
        return false;
    }
}
