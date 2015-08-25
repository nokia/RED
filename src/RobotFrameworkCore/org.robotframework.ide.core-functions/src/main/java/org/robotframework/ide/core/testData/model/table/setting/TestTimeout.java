package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestTimeout extends AModelElement {

    private final RobotToken declaration;
    private RobotToken timeout;
    private final List<RobotToken> message = new LinkedList<>();

    private final List<RobotToken> comment = new LinkedList<>();


    public TestTimeout(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public RobotToken getTimeout() {
        return timeout;
    }


    public void setTimeout(RobotToken timeout) {
        this.timeout = timeout;
    }


    public List<RobotToken> getMessageArguments() {
        return message;
    }


    public void addMessageArgument(final RobotToken messageArgument) {
        this.message.add(messageArgument);
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.SUITE_TEST_TIMEOUT;
    }


    @Override
    public FilePosition getPosition() {
        return getDeclaration().getFilePosition();
    }
}
