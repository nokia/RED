package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class VariablesImport extends AImported {

    private final List<RobotToken> arguments = new LinkedList<>();


    public VariablesImport(final RobotToken variablesDeclaration) {
        super(Type.VARIABLES, variablesDeclaration);
    }


    public List<RobotToken> getArguments() {
        return arguments;
    }


    public void addArgument(final RobotToken argument) {
        this.arguments.add(argument);
    }


    @Override
    public boolean isPresent() {
        return true; // TODO: check if correct imported
    }
}
