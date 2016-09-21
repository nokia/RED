package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordTimeoutMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TIMEOUT;
    }

    @Override
    public TestCaseTimeout insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        final KeywordTimeout kwTimeout = (KeywordTimeout) modelElement;
        
        final TestCaseTimeout caseTimeout = testCase.newTimeout();
        caseTimeout.getDeclaration().setText(kwTimeout.getDeclaration().getText());

        caseTimeout.setTimeout(kwTimeout.getTimeout().getText());
        for (final RobotToken msg : kwTimeout.getMessage()) {
            caseTimeout.addMessagePart(msg);
        }
        for (final RobotToken comment : kwTimeout.getComment()) {
            caseTimeout.addCommentPart(comment);
        }
        return caseTimeout;
    }
}
