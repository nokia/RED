/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.table.keywords;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordTimeoutTest {

    private RobotToken decToken;
    private KeywordTimeout testable;

    @Before
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new KeywordTimeout(decToken);
    }

    @Test
    public void test_ifCellWillBeAdded_atTimeoutValuePosition() {
        // prepare
        final RobotToken timeoutToken = RobotToken.create("1");
        final RobotToken msgToken = RobotToken.create("msg");
        testable.setTimeout(timeoutToken);
        testable.addMessagePart(msgToken);

        // execute
        testable.insertValueAt("value", 1);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT);
        assertThat(elementTokens.get(1).getText()).isEqualTo("value");
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE);
        assertThat(elementTokens.get(2)).isSameAs(timeoutToken);
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
        assertThat(elementTokens.get(3)).isSameAs(msgToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
    }

    @Test
    public void test_ifCellWillBeAdded_atTimeoutMessagePosition() {
        // prepare
        final RobotToken timeoutToken = RobotToken.create("1");
        final RobotToken msgToken = RobotToken.create("msg");
        testable.setTimeout(timeoutToken);
        testable.addMessagePart(msgToken);

        // execute
        testable.insertValueAt("value", 2);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT);
        assertThat(elementTokens.get(1)).isSameAs(timeoutToken);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE);
        assertThat(elementTokens.get(2).getText()).isEqualTo("value");
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
        assertThat(elementTokens.get(3)).isSameAs(msgToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken timeoutToken = RobotToken.create("1");
        final RobotToken msgToken = RobotToken.create("msg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.setTimeout(timeoutToken);
        testable.addMessagePart(msgToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 3);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(5);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT);
        assertThat(elementTokens.get(1)).isSameAs(timeoutToken);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE);
        assertThat(elementTokens.get(2)).isSameAs(msgToken);
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
        assertThat(elementTokens.get(3).getText()).isEqualTo("value");
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
        assertThat(elementTokens.get(4)).isSameAs(cmtToken);
        assertThat(elementTokens.get(4).getTypes()).contains(RobotTokenType.START_HASH_COMMENT);
    }
}
