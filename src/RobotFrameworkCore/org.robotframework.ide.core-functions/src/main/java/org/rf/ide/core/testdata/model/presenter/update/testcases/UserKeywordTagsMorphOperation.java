package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ITestCaseTableElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordTagsMorphOperation implements ITestCaseTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TAGS;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return false;
    }

    @Override
    public AModelElement<?> create(final TestCase testCase, final String action, final List<String> args, final String comment) {
        throw new IllegalStateException();
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        throw new IllegalStateException();
    }

    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        throw new IllegalStateException();
    }

    @Override
    public void insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        final KeywordTags kwTags = (KeywordTags) modelElement;
        
        final TestCaseTags caseTags = testCase.newTags();
        for (final RobotToken tag : kwTags.getTags()) {
            caseTags.addTag(tag.getText());
        }
        for (final RobotToken comment : kwTags.getComment()) {
            caseTags.addCommentPart(comment);
        }
    }
}
