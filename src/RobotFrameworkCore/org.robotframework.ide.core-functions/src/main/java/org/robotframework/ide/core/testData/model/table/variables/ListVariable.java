package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ListVariable extends AVariable {

    private final List<RobotToken> items = new LinkedList<>();


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
        return (getDeclaration() != null);
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            tokens.addAll(getItems());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
