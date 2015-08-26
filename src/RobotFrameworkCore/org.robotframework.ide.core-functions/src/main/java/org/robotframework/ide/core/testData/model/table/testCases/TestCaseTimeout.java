package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCaseTimeout extends AModelElement {

    private final RobotToken declaration;
    private RobotToken timeout;
    private final List<RobotToken> message = new LinkedList<>();

    private final List<RobotToken> comment = new LinkedList<>();


    public TestCaseTimeout(final RobotToken declaration) {
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


    public List<RobotToken> getMessage() {
        return Collections.unmodifiableList(message);
    }


    public void addMessagePart(final RobotToken messagePart) {
        this.message.add(messagePart);
    }


    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_TIMEOUT;
    }


    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            if (getTimeout() != null) {
                tokens.add(getTimeout());
            }
            tokens.addAll(getMessage());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
