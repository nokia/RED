/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class DictionaryVariableModelUpdaterTest {

    @Test
    public void test_ableToHandle_dictionaryVariable_shouldReturn_TRUE() {
        assertThat(new DictionaryVariableModelUpdater()
                .ableToHandle(new DictionaryVariable("", RobotToken.create(""), VariableScope.GLOBAL))).isTrue();
    }

    @Test
    public void test_ableToHandle_scalarVariable_shouldReturn_FALSE() {
        assertThat(new DictionaryVariableModelUpdater()
                .ableToHandle(new ScalarVariable("", RobotToken.create(""), VariableScope.GLOBAL))).isFalse();
    }

    @Test
    public void test_ableToHandle_NULL_shouldReturn_FALSE() {
        assertThat(new DictionaryVariableModelUpdater().ableToHandle(null)).isFalse();
    }

    @Test
    public void test_addOrSet_elementOutOfCurrentListSize_emptyList_shouldAddEmptyValuesAndThenNext() {
        // prepare
        final DictionaryVariable dictVar = new DictionaryVariable("", RobotToken.create(""), VariableScope.GLOBAL);
        final List<DictionaryKeyValuePair> toAdd = new ArrayList<>();
        toAdd.add(DictionaryKeyValuePair.createFromRaw("key=value"));

        // execute
        new DictionaryVariableModelUpdater().addOrSet(dictVar, 1, toAdd);

        // verify
        final List<DictionaryKeyValuePair> items = dictVar.getItems();
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getRaw().getText()).isEmpty();
        assertThat(items.get(1).getRaw().getText()).isEqualTo("key=value");
    }

    @Test
    public void test_addOrSet_elementOutOfCurrentListSize_oneElementList_shouldAddEmptyValuesAndThenNext() {
        // prepare
        final DictionaryVariable dictVar = new DictionaryVariable("", RobotToken.create(""), VariableScope.GLOBAL);
        dictVar.put(RobotToken.create("key1=value1"), RobotToken.create("key1"), RobotToken.create("value1"));
        final List<DictionaryKeyValuePair> toAdd = new ArrayList<>();
        toAdd.add(DictionaryKeyValuePair.createFromRaw("key=value"));

        // execute
        new DictionaryVariableModelUpdater().addOrSet(dictVar, 2, toAdd);

        // verify
        final List<DictionaryKeyValuePair> items = dictVar.getItems();
        assertThat(items).hasSize(3);
        assertThat(items.get(0).getRaw().getText()).isEqualTo("key1=value1");
        assertThat(items.get(1).getRaw().getText()).isEmpty();
        assertThat(items.get(2).getRaw().getText()).isEqualTo("key=value");
    }

    @Test
    public void test_addOrSet_elementInCurrentListSize_oneElementList_shouldOverrideValuesAndThenAddNext() {
        // prepare
        final DictionaryVariable dictVar = new DictionaryVariable("", RobotToken.create(""), VariableScope.GLOBAL);
        dictVar.put(RobotToken.create("key1=value1"), RobotToken.create("key1"), RobotToken.create("value1"));
        final List<DictionaryKeyValuePair> toAdd = new ArrayList<>();
        toAdd.add(DictionaryKeyValuePair.createFromRaw("key=value"));
        toAdd.add(DictionaryKeyValuePair.createFromRaw("key2=value2"));

        // execute
        new DictionaryVariableModelUpdater().addOrSet(dictVar, 0, toAdd);

        // verify
        final List<DictionaryKeyValuePair> items = dictVar.getItems();
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getRaw().getText()).isEqualTo("key=value");
        assertThat(items.get(1).getRaw().getText()).isEqualTo("key2=value2");
    }
}
