package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordReturnMorphOperation extends UserKeywordElementMorphOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_RETURN;
    }

    @Override
    public void insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        final KeywordReturn returnToken = (KeywordReturn) modelElement;
        
        final TestCaseUnknownSettings unkownSetting = testCase.newUnknownSettings();
        unkownSetting.getDeclaration().setText(returnToken.getDeclaration().getText());
        for (final RobotToken value : returnToken.getReturnValues()) {
            unkownSetting.addArgument(value.getText());
        }
        for (final RobotToken comment : returnToken.getComment()) {
            unkownSetting.addCommentPart(comment);
        }
    }
}
