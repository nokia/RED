package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordDocumentationMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_DOCUMENTATION;
    }

    @Override
    public TestDocumentation insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        final KeywordDocumentation kwDocumentation = (KeywordDocumentation) modelElement;
        
        final TestDocumentation caseDocumentation = testCase.newDocumentation();
        for (final RobotToken txt : kwDocumentation.getDocumentationText()) {
            caseDocumentation.addDocumentationText(txt);
        }
        for (final RobotToken comment : kwDocumentation.getComment()) {
            caseDocumentation.addCommentPart(comment);
        }
        return caseDocumentation;
    }
}
