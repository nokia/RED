package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ScalarVariable extends AVariable {

    private final List<RobotToken> values = new LinkedList<>();


    public ScalarVariable(String name, RobotToken declaration) {
        super(VariableType.SCALAR, name, declaration);
    }


    public void addValue(final RobotToken value) {
        values.add(value);
    }


    public List<RobotToken> getValues() {
        return values;
    }


    @Override
    public boolean isPresent() {
        return true;
    }


    @Override
    public VariableType getType() {
        if (values.size() >= 2) {
            this.type = VariableType.SCALAR_AS_LIST;
        } else {
            this.type = VariableType.SCALAR;
        }

        return type;
    }
}
