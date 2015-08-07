package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UnknownVariable extends AVariable {

    private final List<RobotToken> items = new LinkedList<>();


    public UnknownVariable(String name, RobotToken declaration) {
        super(VariableType.INVALID, name, declaration);
    }


    public void addItem(final RobotToken item) {
        items.add(item);
    }


    public List<RobotToken> getItems() {
        return items;
    }


    @Override
    public boolean isPresent() {
        return true;
    }
}
