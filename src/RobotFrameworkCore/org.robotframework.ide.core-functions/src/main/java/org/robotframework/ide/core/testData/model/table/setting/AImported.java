package org.robotframework.ide.core.testData.model.table.setting;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
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
        return Collections.unmodifiableList(comment);
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


    public ModelType getModelType() {
        ModelType modelType = ModelType.UNKNOWN;

        if (type == Type.LIBRARY) {
            modelType = ModelType.LIBRARY_IMPORT_SETTING;
        } else if (type == Type.RESOURCE) {
            modelType = ModelType.RESOURCE_IMPORT_SETTING;
        } else if (type == Type.VARIABLES) {
            modelType = ModelType.VARIABLES_IMPORT_SETTING;
        }

        return modelType;
    }


    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }
}
