package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.listener.IRobotFile;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.Range;

public class RobotSuiteFileValidator extends RobotFileCommonValidator {

    public RobotSuiteFileValidator(final IFile file) {
        super(file);
    }

    @Override
    protected void validate(final IRobotFile fileModel, final IProgressMonitor monitor) throws CoreException {
        validateFileName(fileModel, monitor);

        super.validate(fileModel, monitor);
    }

    private void validateFileName(final IRobotFile fileModel, final IProgressMonitor monitor) {
        if ("__init__".equals(getSimpleName(file))) {
            final ProblemPosition position = getTestCaseTableHeaderPosition(fileModel.getTestCaseTable());
            reporter.handleProblem(RobotProblem.causedBy(SuiteFileProblem.SUITE_FILE_IS_NAMED_INIT), file,
                    position);
        }
    }

    private ProblemPosition getTestCaseTableHeaderPosition(final TestCaseTable testCaseTable) {
        // TODO : this can be done using some nice API on parser side
        final List<TableHeader> headers = testCaseTable.getHeaders();
        if (!headers.isEmpty()) {
            final TableHeader header = headers.get(0);
            final int line = header.getBeginPosition().getLine();
            final int beginOffset = header.getBeginPosition().getOffset();
            final int endOffset = header.getEndPosition().getOffset();

            return new ProblemPosition(line, Range.closed(beginOffset, endOffset));
        }
        return new ProblemPosition(1);
    }

    private static String getSimpleName(final IFile file) {
        return file.getName().substring(0, file.getName().length() - file.getFileExtension().length() - 1);
    }
}