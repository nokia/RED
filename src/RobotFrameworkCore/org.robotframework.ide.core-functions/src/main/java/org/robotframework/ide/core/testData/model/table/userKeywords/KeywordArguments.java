package org.robotframework.ide.core.testData.model.table.userKeywords;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class KeywordArguments extends AModelElement {

    private final RobotToken declaration;
    private final List<RobotToken> arguments = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    public KeywordArguments(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public List<RobotToken> getArguments() {
        return arguments;
    }


    public void addArgument(final RobotToken argument) {
        arguments.add(argument);
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }
}
