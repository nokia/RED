package org.robotframework.ide.core.testData.text.reader.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.HashCommentRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


public class HashCommentRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;


    @Test
    public void test_threeHashsTheThridEscapedCommentSignsExists() {
        StringBuilder text = new StringBuilder("##\\#comment");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_threeHashsTheSecondEscapedCommentSignsExists() {
        StringBuilder text = new StringBuilder("#\\##comment");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_threeHashsCommentSignsExists() {
        StringBuilder text = new StringBuilder("###comment");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_twoHashsCommentSignsExists() {
        StringBuilder text = new StringBuilder("##comment");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleHashCommentExists() {
        StringBuilder text = new StringBuilder("#comment");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleHashCommentButEscapedExists() {
        StringBuilder text = new StringBuilder("\\#comment");

        assertThat(rec.hasNext(text, 1)).isFalse();
    }


    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo("^(?!\\\\)#.*$");
    }


    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(
                RobotTokenType.START_HASH_COMMENT);
    }


    @Before
    public void setUp() {
        rec = new HashCommentRecognizer();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
