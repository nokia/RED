package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public abstract class AImported extends AModelElement {

    private final Type type;
    private final RobotToken declaration;
    private RobotToken pathOrName;
    private final List<RobotToken> comment = new LinkedList<>();


    protected AImported(final Type type, final RobotToken declaration) {
        this.type = type;
        this.declaration = declaration;
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


    public RobotToken getPathOrName() {
        return pathOrName;
    }


    public void setPathOrName(RobotToken pathOrName) {
        this.pathOrName = pathOrName;
    }

    public static enum Type {
        LIBRARY, RESOURCE, VARIABLES;
    }


    public Type getType() {
        return type;
    }
}
