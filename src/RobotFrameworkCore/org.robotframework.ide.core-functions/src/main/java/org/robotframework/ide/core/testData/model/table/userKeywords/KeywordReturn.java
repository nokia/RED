package org.robotframework.ide.core.testData.model.table.userKeywords;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class KeywordReturn extends AModelElement {

    private final RobotToken declaration;
    private final List<RobotToken> values = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    public KeywordReturn(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public List<RobotToken> getReturnValues() {
        return values;
    }


    public void addReturnValue(final RobotToken returnValue) {
        values.add(returnValue);
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }
}
