/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariablesValueConverterTest {

    @Test
    public void test_toRobotToken_convert() {
        // prepare
        final String text = "ok";

        // execute
        final RobotToken tok = VariablesValueConverter.toRobotToken(text);

        // verify
        assertThat(tok.getText()).isEqualTo(text);
    }

    @Test
    public void test_conversion_fromString_keyValueExists() {
        // prepare
        final String dictText = "key=value";

        // execute
        final DictionaryKeyValuePair dict = VariablesValueConverter.fromString(dictText);

        // verify
        assertThat(dict.getKey().getText()).isEqualTo("key");
        assertThat(dict.getValue().getText()).isEqualTo("value");
        assertThat(dict.getRaw().getText()).isEqualTo(dictText);
    }

    @Test
    public void test_conversion_fromRobotToken_keyValueExists() {
        // prepare
        final String dictText = "key=value";
        final RobotToken tok = new RobotToken();
        tok.setText(dictText);

        // execute
        final DictionaryKeyValuePair dict = VariablesValueConverter.fromRobotToken(tok);

        // verify
        assertThat(dict.getKey().getText()).isEqualTo("key");
        assertThat(dict.getValue().getText()).isEqualTo("value");
        assertThat(dict.getRaw().getText()).isEqualTo(dictText);
    }

    @Test
    public void test_conversion_fromDictionaryKeyValuePair_keyValueExists() {
        // prepare
        final RobotToken raw = RobotToken.create("key=value");
        final RobotToken key = RobotToken.create("key");
        final RobotToken value = RobotToken.create("value");
        final DictionaryKeyValuePair keyValuePair = new DictionaryKeyValuePair(raw, key, value);

        // execute
        final RobotToken tok = VariablesValueConverter.fromDictionaryKeyValuePair(keyValuePair);

        // verify
        assertThat(tok.getText()).isEqualTo("key=value");
    }

    @Test
    public void test_conversion_convertAll_to_RobotToken_allTypes() {
        // prepare
        final List<Object> objs = new ArrayList<>();
        final RobotToken tok = RobotToken.create("text");
        objs.add(tok);
        objs.add("text2");
        objs.add(DictionaryKeyValuePair.createFromRaw("new=ok"));

        // execute
        final List<RobotToken> converted = VariablesValueConverter.convert(objs, RobotToken.class);

        // verify
        assertThat(converted).hasSize(3);
        assertThat(converted.get(0)).isSameAs(tok);
        assertThat(converted.get(1).getText()).isEqualTo("text2");
        assertThat(converted.get(2).getText()).isEqualTo("new=ok");
    }

    @Test
    public void test_converstion_convertAll_to_DictionaryKeyValuePair_allTypes() {
        // prepare
        final List<Object> objs = new ArrayList<>();
        final DictionaryKeyValuePair pair = DictionaryKeyValuePair.createFromRaw("key=value");
        objs.add("key1=value2");
        objs.add(RobotToken.create("key2=value3"));
        objs.add(pair);

        // execute
        List<DictionaryKeyValuePair> converted = VariablesValueConverter.convert(objs, DictionaryKeyValuePair.class);

        // verify
        assertThat(converted).hasSize(3);
        assertKeyValuePair(converted.get(0), "key1", "value2");
        assertKeyValuePair(converted.get(1), "key2", "value3");
        assertThat(converted.get(2)).isSameAs(pair);
    }

    @Test(expected = ClassCastException.class)
    public void test_wrongClassToConvert() {
        // prepare
        final List<Object> objs = new ArrayList<>();
        objs.add(new String("xyz"));

        // execute
        List<Double> c = VariablesValueConverter.convert(objs, Double.class);

        // verify
        Assert.fail("Shouldn't reach here.");
    }

    private void assertKeyValuePair(final DictionaryKeyValuePair pair, final String key, final String value) {
        assertThat(pair.getRaw().getText()).isEqualTo(key + "=" + value);
        assertThat(pair.getKey().getText()).isEqualTo(key);
        assertThat(pair.getValue().getText()).isEqualTo(value);
    }
}
