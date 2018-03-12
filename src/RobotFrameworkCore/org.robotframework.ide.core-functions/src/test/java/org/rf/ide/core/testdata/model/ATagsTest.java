/*
* Copyright 2017 Nokia Solutions and Networks
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

public class ATagsTest {

    private RobotToken decToken;

    private ADummyTagForKeywords testable;

    @Before
    public void setUp() {
        this.decToken = new RobotToken();
        this.testable = new ADummyTagForKeywords(decToken);
    }

    @Test
    public void test_ifCellWillBeAdded_atTagPosition() {
        // prepare
        final RobotToken tagToken = RobotToken.create("tag1");
        testable.addTag(tagToken);

        // execute
        testable.insertValueAt("tag0", 1);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(3);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationTagType());
        assertThat(elementTokens.get(1).getText()).isEqualTo("tag0");
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getTagType());
        assertThat(elementTokens.get(2)).isSameAs(tagToken);
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getTagType());
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken tagToken = RobotToken.create("tag1");
        final RobotToken cmtToken = RobotToken.create("#cmt");
        testable.addTag(tagToken);
        testable.addCommentPart(cmtToken);

        // execute
        testable.insertValueAt("tag2", 2);

        // verify
        final List<RobotToken> elementTokens = testable.getElementTokens();
        assertThat(elementTokens).hasSize(4);
        assertThat(elementTokens.get(0)).isSameAs(decToken);
        assertThat(elementTokens.get(0).getTypes()).contains(testable.getDeclarationTagType());
        assertThat(elementTokens.get(1)).isSameAs(tagToken);
        assertThat(elementTokens.get(1).getTypes()).contains(testable.getTagType());
        assertThat(elementTokens.get(2).getText()).isEqualTo("tag2");
        assertThat(elementTokens.get(2).getTypes()).contains(testable.getTagType());
        assertThat(elementTokens.get(3)).isSameAs(cmtToken);
        assertThat(elementTokens.get(3).getTypes()).contains(testable.getCommentType());
    }

    private class ADummyTagForKeywords extends ATags<Object> {

        private static final long serialVersionUID = 4556308733304191479L;

        public ADummyTagForKeywords(RobotToken declaration) {
            super(declaration);
        }

        @Override
        public ModelType getModelType() {
            return null;
        }

        @Override
        public IRobotTokenType getTagType() {
            return RobotTokenType.KEYWORD_SETTING_TAGS;
        }

        @Override
        public IRobotTokenType getDeclarationTagType() {
            return RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME;
        }

        public IRobotTokenType getCommentType() {
            return RobotTokenType.START_HASH_COMMENT;
        }
    }
}
