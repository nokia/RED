package org.robotframework.ide.core.testData.text.reader.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.PreviousLineContinueRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


public class PreviousLineContinueRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;


    @Test
    public void test_ThreeDotsAndFoobarWord() {
        StringBuilder text = new StringBuilder("...foobar");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(3);
        assertThat(token.getText().toString()).isEqualTo("...");
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_FourDots() {
        StringBuilder text = new StringBuilder("....");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_ThreeDots() {
        StringBuilder text = new StringBuilder("...");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_TwoDots() {
        StringBuilder text = new StringBuilder("..");

        assertThat(rec.hasNext(text, 1)).isFalse();
    }


    @Test
    public void test_singleDot() {
        StringBuilder text = new StringBuilder(".");

        assertThat(rec.hasNext(text, 1)).isFalse();
    }


    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo("^[.]{3,}");
    }


    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(
                RobotTokenType.PREVIOUS_LINE_CONTINUE);
    }


    @Before
    public void setUp() {
        rec = new PreviousLineContinueRecognizer();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
