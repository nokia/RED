package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ITestCaseTableElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordDocumentationMorphOperation implements ITestCaseTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_DOCUMENTATION;
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
        final KeywordDocumentation kwDocumentation = (KeywordDocumentation) modelElement;
        
        final TestDocumentation caseDocumentation = testCase.newDocumentation();
        for (final RobotToken txt : kwDocumentation.getDocumentationText()) {
            caseDocumentation.addDocumentationText(txt);
        }
        for (final RobotToken comment : kwDocumentation.getComment()) {
            caseDocumentation.addCommentPart(comment);
        }
    }
}
