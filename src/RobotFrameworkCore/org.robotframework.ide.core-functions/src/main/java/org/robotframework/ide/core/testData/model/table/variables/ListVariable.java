package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ListVariable extends AVariable {

    public final List<RobotToken> items = new LinkedList<>();


    public ListVariable(String name, RobotToken declaration) {
        super(VariableType.LIST, name, declaration);
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
