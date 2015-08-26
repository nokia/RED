package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public abstract class ATags extends AModelElement {

    private final RobotToken declaration;
    private final List<RobotToken> tags = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    protected ATags(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public List<RobotToken> getTags() {
        return tags;
    }


    public void addTag(final RobotToken tag) {
        tags.add(tag);
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    @Override
    public FilePosition getBeginPosition() {
        return declaration.getFilePosition();
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            tokens.addAll(getTags());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
