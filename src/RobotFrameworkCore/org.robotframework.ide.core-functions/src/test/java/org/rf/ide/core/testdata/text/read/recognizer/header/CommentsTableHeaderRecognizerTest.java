/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.header;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.test.helpers.CombinationGenerator;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class CommentsTableHeaderRecognizerTest {

    private final CommentsTableHeaderRecognizer rec = new CommentsTableHeaderRecognizer();

    @Test
    public void recognizerIsApplicable_whenRobotVersionIsAtLeast31() throws Exception {
        assertThat(rec.isApplicableFor(new RobotVersion(3, 0))).isFalse();
        assertThat(rec.isApplicableFor(new RobotVersion(3, 0, 9))).isFalse();
        assertThat(rec.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(rec.isApplicableFor(new RobotVersion(3, 1, 5))).isTrue();
        assertThat(rec.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void test_check_Comments_withAsterisk_atTheBeginAndEnd_spaceLetterT() {
        final String expectedToCut = " * Comments *";
        final StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_Comments_withAsterisk_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("T * Comments ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" * Comments ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceComments_withAsterisk_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder(" * Comments ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_Comments_withAsterisk_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("* Comments ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_Comments_withAsterisks_atTheBeginAndEnd_spaceLetterT() {
        final String expectedToCut = " *** Comments ***";
        final StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_Comments_withAsterisks_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("T *** Comments ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" *** Comments ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceComments_withAsterisks_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder(" *** Comments ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_Comments_withAsterisks_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("*** Comments ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_Comment_withAsterisk_atTheBeginAndEnd_spaceLetterT() {
        final String expectedToCut = " * Comment *";
        final StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_Comment_withAsterisk_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("T * Comment ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" * Comment ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceComment_withAsterisk_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder(" * Comment ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_Comment_withAsterisk_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("* Comment ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_Comment_withAsterisks_atTheBeginAndEnd_spaceLetterT() {
        final String expectedToCut = " *** Comment ***";
        final StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceLetterT_and_Comment_withAsterisks_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("T *** Comment ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" *** Comment ***");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_spaceComment_withAsterisks_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder(" *** Comment ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_CommentsAllPossibilities_withAsterisks_atTheBeginAndEnd() {
        assertAllCombinations("Comments");
    }

    @Test
    public void test_check_CommentAllPossibilities_withAsterisks_atTheBeginAndEnd() {
        assertAllCombinations("Comment");
    }

    private void assertAllCombinations(final String text) {
        final List<String> combinations = new CombinationGenerator().combinations(text);

        for (final String comb : combinations) {
            final StringBuilder textOfHeader = new StringBuilder("*** ").append(comb).append(" ***");

            assertThat(rec.hasNext(textOfHeader, 1, 0)).isTrue();
            final RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }

    @Test
    public void test_check_Comment_withAsterisks_atTheBeginAndEnd() {
        final StringBuilder text = new StringBuilder("*** Comment ***");

        assertThat(rec.hasNext(text, 1, 0)).isTrue();
        final RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }

    @Test
    public void test_check_Comment_withSpacesInside() {
        final StringBuilder text = new StringBuilder("*** Comme nt ***");

        assertThat(rec.hasNext(text, 1, 0)).isFalse();
    }

    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern())
                .isEqualTo("[ ]?([*][\\s]*)+[\\s]*(" + ATokenRecognizer.createUpperLowerCaseWord("Comments") + "|"
                        + ATokenRecognizer.createUpperLowerCaseWord("Comment") + ")([\\s]*[*])*");
    }

    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(RobotTokenType.COMMENTS_TABLE_HEADER);
    }
}
