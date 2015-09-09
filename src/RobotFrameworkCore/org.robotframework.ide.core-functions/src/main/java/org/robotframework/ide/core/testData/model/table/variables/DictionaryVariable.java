/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.variables;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class DictionaryVariable extends AVariable {

    private final List<DictionaryKeyValuePair> items = new LinkedList<>();


    public DictionaryVariable(String name, RobotToken declaration) {
        super(VariableType.DICTIONARY, name, declaration);
    }


    public void put(final RobotToken raw, final RobotToken key,
            final RobotToken value) {
        items.add(new DictionaryKeyValuePair(raw, key, value));
    }


    public List<DictionaryKeyValuePair> getItems() {
        return Collections.unmodifiableList(items);
    }


    @Override
    public boolean isPresent() {
        return (getDeclaration() != null);
    }

    public static class DictionaryKeyValuePair {

        private RobotToken raw;
        private RobotToken key;
        private RobotToken value;


        public DictionaryKeyValuePair(final RobotToken raw,
                final RobotToken key, final RobotToken value) {
            this.raw = raw;
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


        public RobotToken getRaw() {
            return raw;
        }


        public void setRaw(RobotToken raw) {
            this.raw = raw;
        }

    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            for (DictionaryKeyValuePair p : items) {
                if (p.getRaw() != null) {
                    tokens.add(p.getRaw());
                }
            }
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
