/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.settings.LibraryAliasRecognizerOld;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.collect.Range;

class LibraryAliasNotInUpperCaseValidator31 extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final SettingTable table;

    private final ValidationReportingStrategy reporter;

    LibraryAliasNotInUpperCaseValidator31(final IFile file, final SettingTable table,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.table = table;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 1));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final List<LibraryImport> libraryImports = table.getLibraryImports();
        for (final LibraryImport libImport : libraryImports) {
            final LibraryAlias alias = libImport.getAlias();
            if (!alias.isPresent()) {
                final List<RobotToken> args = libImport.getArguments();
                if (args.size() > 1) {
                    final RobotToken possiblyLowerCasedWithName = args.get(args.size() - 2);
                    if (LibraryAliasRecognizerOld.EXPECTED.matcher(possiblyLowerCasedWithName.getText()).matches()) {
                        reporter.handleProblem(RobotProblem.causedBy(
                                GeneralSettingsProblem.LIBRARY_WITH_NAME_NOT_UPPER_CASE_COMBINATION_NOT_RECOGNIZED)
                                .formatMessageWith(possiblyLowerCasedWithName.getText()), file,
                                possiblyLowerCasedWithName);
                    }
                }
            }
        }
    }
}
