package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.model.table.variables.ScalarVariable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class VariableTable extends ARobotSectionTable {

    private List<IVariableHolder> variables = new LinkedList<>();


    public VariableTable(String uuid) {
        super(uuid);
    }


    public List<IVariableHolder> getVariables() {
        return Collections.unmodifiableList(variables);
    }


    public void addVariable(final IVariableHolder variable) {
        variable.setFileUUID(getFileUUID());
        variables.add(variable);
    }


    public void createScalarVariable(final int index, final String name,
            final List<String> values, final String comment) {
        ScalarVariable scalar = new ScalarVariable(name, null);
        for (String v : values) {
            RobotToken t = new RobotToken();
            t.setText(new StringBuilder(v));
            t.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);
            scalar.addValue(t);
        }

        scalar.setFileUUID(getFileUUID());
        variables.add(index, scalar);
    }


    public void createListVariable(final int index, final String name,
            final List<String> values, final String comment) {
        ListVariable list = new ListVariable(name, null);
        for (String v : values) {
            RobotToken t = new RobotToken();
            t.setText(new StringBuilder(v));
            t.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);
            list.addItem(t);
        }

        list.setFileUUID(getFileUUID());
        variables.add(index, list);
    }


    public void createDictionaryVariable(final int index, final String name,
            final Map<String, String> items, final String comment) {
        DictionaryVariable dict = new DictionaryVariable(name, null);
        Set<String> keySet = items.keySet();
        for (String key : keySet) {
            RobotToken keyT = new RobotToken();
            keyT.setText(new StringBuilder(key));
            keyT.setType(RobotTokenType.VARIABLES_DICTIONARY_KEY);

            String value = items.get(key);
            RobotToken valueT = new RobotToken();
            valueT.setText(new StringBuilder(value));
            valueT.setType(RobotTokenType.VARIABLES_DICTIONARY_VALUE);

            dict.put(null, keyT, valueT);
        }

        dict.setFileUUID(getFileUUID());
        variables.add(index, dict);
    }


    public boolean isEmpty() {
        return (variables.isEmpty());
    }
}
