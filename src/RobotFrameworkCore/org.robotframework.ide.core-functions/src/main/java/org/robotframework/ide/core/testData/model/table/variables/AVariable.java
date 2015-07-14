package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;


public abstract class AVariable<T> {

    private VariableType type;
    private List<T> elements = new LinkedList<>();


    public AVariable(final VariableType type) {
        this.type = type;
    }

    public static enum VariableType {
        SCALAR, LIST, DICTIONARY
    }
}
