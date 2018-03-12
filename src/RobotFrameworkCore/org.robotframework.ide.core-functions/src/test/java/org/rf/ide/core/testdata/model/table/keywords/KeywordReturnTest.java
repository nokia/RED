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

public class KeywordReturnTest {

    private RobotToken decToken;
    private KeywordReturn testable;

    @Before
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new KeywordReturn(decToken);
    }

    @Test
    public void test_ifCellWillBeAdded_atReturnValuePosition() {
        // prepare
        final RobotToken returnToken = RobotToken.create("val");
        testable.addReturnValue(returnToken);

        // execute
        testable.insertValueAt("value", 1);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(3);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_RETURN);
        assertThat(elementTokens.get(1).getText()).isEqualTo("value");
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_RETURN_VALUE);
        assertThat(elementTokens.get(2)).isSameAs(returnToken);
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_RETURN_VALUE);
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken returnToken = RobotToken.create("val");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addReturnValue(returnToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("value", 2);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_RETURN);
        assertThat(elementTokens.get(1)).isSameAs(returnToken);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_RETURN_VALUE);
        assertThat(elementTokens.get(2).getText()).isEqualTo("value");
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_SETTING_RETURN_VALUE);
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.START_HASH_COMMENT);
    }
}
