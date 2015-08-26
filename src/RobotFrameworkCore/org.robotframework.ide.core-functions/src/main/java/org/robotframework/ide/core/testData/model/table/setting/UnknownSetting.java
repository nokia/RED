package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UnknownSetting extends AModelElement {

    private final RobotToken declaration;
    private final List<RobotToken> trashs = new LinkedList<>();


    public UnknownSetting(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public List<RobotToken> getTrashs() {
        return trashs;
    }


    public void addTrash(final RobotToken trash) {
        trashs.add(trash);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.SETTINGS_UNKNOWN;
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
            tokens.addAll(getTrashs());
        }

        return tokens;
    }
}
