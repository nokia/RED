/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

class GeneralSettingsTableValidator implements ModelUnitValidator {

    private final Optional<RobotSettingsSection> settingSection;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    GeneralSettingsTableValidator(final Optional<RobotSettingsSection> settingSection) {
        this.settingSection = settingSection;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!settingSection.isPresent()) {
            return;
        }
        final IFile file = settingSection.get().getSuiteFile().getFile();
        final SettingTable settingsTable = (SettingTable) settingSection.get().getLinkedElement();
        reportUnrecognizedLibraries(file, settingsTable);
    }

    private void reportUnrecognizedLibraries(final IFile file, final SettingTable settingTable) {
        final RobotProject robotProject = RobotModelManager.getInstance()
                .getModel()
                .createRobotProject(file.getProject());
        final List<LibrarySpecification> libs = newArrayList();
        libs.addAll(robotProject.getStandardLibraries());
        libs.addAll(robotProject.getReferencedLibraries());

        final Set<String> libNames = newHashSet(Lists.transform(libs, new Function<LibrarySpecification, String>() {

            @Override
            public String apply(final LibrarySpecification lib) {
                return lib.getName();
            }
        }));

        for (final AImported imported : settingTable.getImports()) {
            if (imported instanceof LibraryImport) {
                final LibraryImport libImport = (LibraryImport) imported;
                final RobotToken pathOrName = libImport.getPathOrName();
                if (pathOrName == null) {
                    final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.MISSING_LIBRARY_NAME);
                    final RobotToken libToken = libImport.getDeclaration();
                    final ProblemPosition position = new ProblemPosition(libToken.getLineNumber(),
                            Range.closed(libToken.getStartOffset(),
                                    libToken.getStartOffset() + libToken.getText().toString().length()));
                    reporter.handleProblem(problem, file, position);
                } else {
                    final String name = pathOrName.getText().toString();
                    if (!libNames.contains(name)) {
                        final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNRECOGNIZED_LIBRARY)
                                .formatMessageWith(name);
                        final ProblemPosition position = new ProblemPosition(pathOrName.getLineNumber(),
                                Range.closed(pathOrName.getStartOffset(), pathOrName.getStartOffset() + name.length()));
                        reporter.handleProblem(problem, file, position);
                    }
                }
            }
        }

    }
}
