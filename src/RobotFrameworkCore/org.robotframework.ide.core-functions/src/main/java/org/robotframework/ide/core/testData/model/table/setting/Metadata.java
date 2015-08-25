package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class Metadata extends AModelElement {

    private final RobotToken declaration;
    private RobotToken key;
    private final List<RobotToken> values = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    public Metadata(final RobotToken declaration) {
        this.declaration = declaration;
    }


    public void setKey(final RobotToken key) {
        this.key = key;
    }


    public RobotToken getKey() {
        return key;
    }


    public void addValue(final RobotToken value) {
        this.values.add(value);
    }


    public List<RobotToken> getValues() {
        return values;
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


    @Override
    public boolean isPresent() {
        return true; // TODO: check if correct declaration and etc
    }


    @Override
    public ModelType getModelType() {
        return ModelType.METADATA_SETTING;
    }
}
