package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class DictionaryVariable extends AVariable {

    private final List<DictionaryKeyValuePair> items = new LinkedList<>();


    public DictionaryVariable(String name, RobotToken declaration) {
        super(VariableType.DICTIONARY, name, declaration);
    }


    public void put(final RobotToken key, final RobotToken value) {
        items.add(new DictionaryKeyValuePair(key, value));
    }


    public List<DictionaryKeyValuePair> getItems() {
        return items;
    }


    @Override
    public boolean isPresent() {
        return true;
    }

    public static class DictionaryKeyValuePair {

        private RobotToken key;
        private RobotToken value;


        public DictionaryKeyValuePair(final RobotToken key,
                final RobotToken value) {
            this.key = key;
            this.value = value;
        }


        public RobotToken getKey() {
            return key;
        }


        public void setKey(RobotToken key) {
            this.key = key;
        }


        public RobotToken getValue() {
            return value;
        }


        public void setValue(RobotToken value) {
            this.value = value;
        }
    }
}
