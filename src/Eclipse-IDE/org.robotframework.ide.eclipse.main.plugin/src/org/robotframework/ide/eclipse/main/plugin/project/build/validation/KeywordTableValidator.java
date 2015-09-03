package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.collect.Range;

class KeywordTableValidator {

    private final IFile file;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    KeywordTableValidator(final IFile file) {
        this.file = file;
    }

    void validate(final KeywordTable keywordTable, final IProgressMonitor monitor) throws CoreException {
        if (!keywordTable.isPresent()) {
            return;
        }
        checkIfUniqueKeywordsAreDefined(keywordTable);
    }

    private void checkIfUniqueKeywordsAreDefined(final KeywordTable keywordTable) {
        for (final UserKeyword kw1 : keywordTable.getKeywords()) {
            for (final UserKeyword kw2 : keywordTable.getKeywords()) {
                if (kw1 != kw2) {
                    final RobotToken kw1Token = kw1.getKeywordName();
                    final String kw1Name = kw1Token.getText().toString();
                    final String kw2Name = kw2.getKeywordName().getText().toString();

                    if (kw1Name.equalsIgnoreCase(kw2Name)) {
                        final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DUPLICATED_KEYWORD)
                                .formatMessageWith(kw1Name);
                        final ProblemPosition position = new ProblemPosition(kw1Token.getLineNumber(),
                                Range.closed(kw1Token.getStartOffset(), kw1Token.getStartOffset() + kw1Name.length()));
                        reporter.handleProblem(problem, file, position);
                    }
                }
            }
        }
    }
}
