package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class RobotExecutableRow extends AModelElement {

    private RobotToken action;
    private final List<RobotToken> arguments = new LinkedList<>();


    public RobotExecutableRow() {
        this.action = new RobotToken();
    }


    public RobotToken getAction() {
        return action;
    }


    public void setAction(RobotToken action) {
        this.action = action;
    }


    public List<RobotToken> getArguments() {
        return arguments;
    }


    public void addArgument(final RobotToken argument) {
        arguments.add(argument);
    }


    @Override
    public boolean isPresent() {
        return true;
    }


    @Override
    public ModelType getModelType() {
        ModelType type = ModelType.UNKNOWN;

        List<IRobotTokenType> types = getAction().getTypes();
        if (types.contains(RobotTokenType.TEST_CASE_ACTION_NAME)) {
            type = ModelType.TEST_CASE_EXECUTABLE_ROW;
        } else if (types.contains(RobotTokenType.KEYWORD_ACTION_NAME)) {
            type = ModelType.USER_KEYWORD_EXECUTABLE_ROW;
        }

        return type;
    }


    @Override
    public FilePosition getBeginPosition() {
        return getAction().getFilePosition();
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        tokens.add(getAction());
        tokens.addAll(getArguments());

        return tokens;
    }
}
