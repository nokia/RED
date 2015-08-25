package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestDocumentation extends AModelElement {

    private final RobotToken declaration;
    private final List<RobotToken> text = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    public TestDocumentation(final RobotToken declaration) {
        this.declaration = declaration;
    }


    public void addDocumentationText(RobotToken token) {
        text.add(token);
    }


    public List<RobotToken> getDocumentationText() {
        return text;
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    @Override
    public boolean isPresent() {
        return true; // TODO: check if correct declaration and etc
    }


    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_DOCUMENTATION;
    }
}
