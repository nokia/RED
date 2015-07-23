package org.robotframework.ide.core.testData.text.reader.recognizer.header;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.header.VariablesTableHeaderRecognizer;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


public class VariablesTableHeaderRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;


    @Test
    public void test_check_Variables_withAsterisk_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " * Variables *";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceLetterT_and_Variables_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T * Variables ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" * Variables ***");
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceVariables_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" * Variables ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_Variables_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("* Variables ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_Variables_withAsterisks_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " *** Variables ***";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceLetterT_and_Variables_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T *** Variables ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" *** Variables ***");
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceVariables_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" *** Variables ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_Variables_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("*** Variables ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_Variable_withAsterisk_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " * Variable *";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceLetterT_and_Variable_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T * Variable ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" * Variable ***");
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceVariable_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" * Variable ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_Variable_withAsterisk_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("* Variable ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_Variable_withAsterisks_atTheBeginAndEnd_spaceLetterT() {
        String expectedToCut = " *** Variable ***";
        StringBuilder text = new StringBuilder(expectedToCut).append(" T");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(expectedToCut.length());
        assertThat(token.getText().toString()).isEqualTo(expectedToCut);
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceLetterT_and_Variable_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("T *** Variable ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(" *** Variable ***");
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_spaceVariable_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder(" *** Variable ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_check_Variable_withAsterisks_atTheBeginAndEnd() {
        StringBuilder text = new StringBuilder("*** Variable ***");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getType()).isEqualTo(rec.getProducedType());
    }


    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo(
                "[ ]?[*]+[\\s]*("
                        + ATokenRecognizer
                                .createUpperLowerCaseWord("Variables") + "|"
                        + ATokenRecognizer.createUpperLowerCaseWord("Variable")
                        + ")[\\s]*[*]*");
    }


    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(
                RobotTokenType.VARIABLES_TABLE_HEADER);
    }


    @Before
    public void setUp() {
        rec = new VariablesTableHeaderRecognizer();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
