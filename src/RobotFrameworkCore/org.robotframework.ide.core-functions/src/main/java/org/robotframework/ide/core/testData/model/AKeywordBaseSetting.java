package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public abstract class AKeywordBaseSetting extends AModelElement {

    private final RobotToken declaration;
    private RobotToken keywordName;
    private final List<RobotToken> arguments = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    protected AKeywordBaseSetting(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public RobotToken getKeywordName() {
        return keywordName;
    }


    public void setKeywordName(RobotToken keywordName) {
        this.keywordName = keywordName;
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


    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }
}
