/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class AKeywordBaseSettingTest {

    private RobotToken decToken;

    private ADummyKeywordBaseSettingForKeywords testable;

    @Before
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new ADummyKeywordBaseSettingForKeywords(decToken);
    }

    @Test
    public void test_ifKeywordNameWillBeTheSame_whenNoArgumentsExists() {
        // prepare
        final RobotToken keyToken = new RobotToken();
        testable.setKeywordName(keyToken);

        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(2);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1)).isSameAs(keyToken);
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
    }

    @Test
    public void test_ifKeywordNameWillBeNotCreated_whenNoArgumentsExists() {
        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(1);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
    }

    @Test
    public void test_ifKeywordNameWillBeTheSame_whenArgumentsExists() {
        // prepare
        final RobotToken keyToken = new RobotToken();
        final RobotToken argToken = new RobotToken();
        testable.setKeywordName(keyToken);
        testable.addArgument(argToken);

        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(3);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1)).isSameAs(keyToken);
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getArgumentType());
    }

    @Test
    public void test_ifKeywordNameWillBeCreatedArtifactal_whenArgumentsExists() {
        // prepare
        final RobotToken argToken = new RobotToken();
        testable.addArgument(argToken);

        // execute
        final List<RobotToken> elementTokens = testable.getElementTokens();

        // verify
        assertThat(elementTokens).hasSize(3);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getArgumentType());
    }

    @Test
    public void test_ifCellWillBeAdded_atKeywordPosition() {
        // prepare
        final RobotToken argToken = RobotToken.create("arg");
        testable.setKeywordName(argToken);

        // execute
        testable.insertValueAt("keyword", 1);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(3);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1).getText()).isEqualTo("keyword");
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
        assertThat(elementTokens.get(2)).isEqualToComparingFieldByField(argToken);
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getArgumentType());
    }

    @Test
    public void test_ifCellWillBeAdded_atArgumentPosition() {
        // prepare
        final RobotToken kwToken = RobotToken.create("kw");
        final RobotToken argToken = RobotToken.create("arg");
        testable.setKeywordName(kwToken);
        testable.addArgument(argToken);

        // execute
        testable.insertValueAt("argument", 2);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1)).isSameAs(kwToken);
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
        assertThat(elementTokens.get(2).getText()).isEqualTo("argument");
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getArgumentType());
        assertThat(elementTokens.get(3)).isEqualToComparingFieldByField(argToken);
        assertThat(elementTokens.get(3).getTypes()).contains(testable.getArgumentType());
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken kwToken = RobotToken.create("kw");
        final RobotToken argToken = RobotToken.create("arg");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.setKeywordName(kwToken);
        testable.addArgument(argToken);
        testable.setComment(cmtToken);

        // execute
        testable.insertValueAt("argument", 3);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(5);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationType());
        assertThat(elementTokens.get(1)).isSameAs(kwToken);
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getKeywordNameType());
        assertThat(elementTokens.get(2)).isSameAs(argToken);
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getArgumentType());
        assertThat(elementTokens.get(3).getText()).isEqualTo("argument");
        assertThat(elementTokens.get(3).getTypes()).contains(testable.getArgumentType());
        assertThat(elementTokens.get(4)).isSameAs(cmtToken);
        assertThat(elementTokens.get(4).getTypes()).contains(testable.getCommentType());
    }

    private class ADummyKeywordBaseSettingForKeywords extends AKeywordBaseSetting<Object> {

        private static final long serialVersionUID = 4570317373970770339L;

        public ADummyKeywordBaseSettingForKeywords(RobotToken declaration) {
            super(declaration);
        }

        @Override
        protected RobotTokenType getDeclarationType() {
            return RobotTokenType.SETTING_SUITE_SETUP_DECLARATION;
        }

        @Override
        protected List<AKeywordBaseSetting<Object>> getAllThisKindSettings() {
            return null;
        }

        @Override
        public IRobotTokenType getKeywordNameType() {
            return RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME;
        }

        @Override
        public IRobotTokenType getArgumentType() {
            return RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT;
        }

        public IRobotTokenType getCommentType() {
            return RobotTokenType.START_HASH_COMMENT;
        }

        @Override
        public ModelType getModelType() {
            return null;
        }
    }
}
