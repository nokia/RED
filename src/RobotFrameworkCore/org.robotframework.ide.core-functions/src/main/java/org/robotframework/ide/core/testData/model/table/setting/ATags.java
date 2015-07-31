package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
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
}
