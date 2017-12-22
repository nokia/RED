/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.Range;

public class RobotSuiteFileValidator extends RobotFileValidator {

    public RobotSuiteFileValidator(final ValidationContext context, final IFile file,
            final ProblemsReportingStrategy reporter) {
        super(context, file, reporter);
    }

    @Override
    public void validate(final RobotSuiteFile fileModel, final FileValidationContext validationContext)
            throws CoreException {
        super.validate(fileModel, validationContext);

        validateFileName(fileModel);
    }

    private void validateFileName(final RobotSuiteFile fileModel) {
        if (RobotFile.INIT_NAMES.stream().anyMatch(file.getName()::equalsIgnoreCase)) {
            final ProblemPosition position = getTestCaseTableHeaderPosition(
                    fileModel.findSection(RobotCasesSection.class));
            reporter.handleProblem(RobotProblem.causedBy(SuiteFileProblem.SUITE_FILE_IS_NAMED_INIT), file, position);
        }
    }

    private ProblemPosition getTestCaseTableHeaderPosition(final Optional<RobotCasesSection> section) {
        // TODO : this can be done using some nice API on parser side
        if (!section.isPresent()) {
            return new ProblemPosition(1);
        }
        final TestCaseTable table = section.get().getLinkedElement();
        final List<TableHeader<? extends ARobotSectionTable>> headers = table.getHeaders();
        if (!headers.isEmpty()) {
            final TableHeader<? extends ARobotSectionTable> header = headers.get(0);
            final int line = header.getBeginPosition().getLine();
            final int beginOffset = header.getBeginPosition().getOffset();
            final int endOffset = header.getEndPosition().getOffset();

            return new ProblemPosition(line, Range.closed(beginOffset, endOffset));
        }
        return new ProblemPosition(1);
    }

}
