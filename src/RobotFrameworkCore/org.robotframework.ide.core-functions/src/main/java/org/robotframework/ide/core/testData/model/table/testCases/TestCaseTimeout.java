package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCaseTimeout extends AModelElement {

    private final RobotToken declaration;
    private RobotToken timeout;
    private final List<RobotToken> unexpectedTrashArguments = new LinkedList<>();

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


    public List<RobotToken> getUnexpectedTrashArguments() {
        return unexpectedTrashArguments;
    }


    public void addUnexpectedTrashArgument(final RobotToken trashArgument) {
        this.unexpectedTrashArguments.add(trashArgument);
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }
}
