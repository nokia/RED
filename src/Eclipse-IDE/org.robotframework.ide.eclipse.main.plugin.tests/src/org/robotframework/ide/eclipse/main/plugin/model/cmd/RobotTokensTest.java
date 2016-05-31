/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotTokensTest {

    @Test
    public void tokenCreationTest() {
        assertThat(RobotTokens.create("").getRaw()).isEqualTo("");
        assertThat(RobotTokens.create("").getText()).isEqualTo("");

        assertThat(RobotTokens.create("abc").getRaw()).isEqualTo("abc");
        assertThat(RobotTokens.create("abc").getText()).isEqualTo("abc");
    }

    @Test(expected = NullPointerException.class)
    public void cannotCreateTokenWithNullContent() {
        RobotTokens.create(null);
    }

    @Test
    public void keyValuePairCreationTest_1() {
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getRaw().getRaw()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getRaw().getText()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getRaw().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_VARIABLE_VALUE);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getKey().getRaw()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getKey().getText()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getKey().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_KEY);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getValue().getRaw()).isEqualTo("");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getValue().getText()).isEqualTo("");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a")).getValue().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_VALUE);
    }

    @Test
    public void keyValuePairCreationTest_2() {
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getRaw().getRaw()).isEqualTo("a=");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getRaw().getText()).isEqualTo("a=");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getRaw().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_VARIABLE_VALUE);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getKey().getRaw()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getKey().getText()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getKey().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_KEY);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getValue().getRaw()).isEqualTo("");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getValue().getText()).isEqualTo("");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=")).getValue().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_VALUE);
    }

    @Test
    public void keyValuePairCreationTest_3() {
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getRaw().getRaw()).isEqualTo("a=b");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getRaw().getText()).isEqualTo("a=b");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getRaw().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_VARIABLE_VALUE);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getKey().getRaw()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getKey().getText()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getKey().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_KEY);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getValue().getRaw()).isEqualTo("b");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getValue().getText()).isEqualTo("b");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b")).getValue().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_VALUE);
    }

    @Test
    public void keyValuePairCreationTest_4() {
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getRaw().getRaw()).isEqualTo("a=b=c");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getRaw().getText()).isEqualTo("a=b=c");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getRaw().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_VARIABLE_VALUE);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getKey().getRaw()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getKey().getText()).isEqualTo("a");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getKey().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_KEY);
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getValue().getRaw()).isEqualTo("b=c");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getValue().getText()).isEqualTo("b=c");
        assertThat(RobotTokens.toKeyValuePair(RobotTokens.create("a=b=c")).getValue().getTypes())
                .containsOnly(RobotTokenType.VARIABLES_DICTIONARY_VALUE);
    }

}
