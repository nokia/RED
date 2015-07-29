package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;
import org.robotframework.ide.core.testHelpers.CombinationGenerator;


public class SettingDocumentationRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;


    @Test
    public void test_documentationColonWord_allCombinations() {
        List<String> combinations = new CombinationGenerator()
                .combinations("Documentation:");

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(
                    textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }


    @Test
    public void test_twoSpacesAndDocumentationColonThanWord() {
        StringBuilder text = new StringBuilder(" Documentation:");
        StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleSpaceAndDocumentationColonThanWord() {
        StringBuilder text = new StringBuilder(" Documentation:");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleDocumentationColonThanLetterCWord() {
        StringBuilder text = new StringBuilder("Documentation:");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleDocumentationColonWord() {
        StringBuilder text = new StringBuilder("Documentation:");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_documentationWord_allCombinations() {
        List<String> combinations = new CombinationGenerator()
                .combinations("Documentation");

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(
                    textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }


    @Test
    public void test_twoSpacesAndDocumentationThanWord() {
        StringBuilder text = new StringBuilder(" Documentation");
        StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleSpaceAndDocumentationThanWord() {
        StringBuilder text = new StringBuilder(" Documentation");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleDocumentationThanLetterCWord() {
        StringBuilder text = new StringBuilder("Documentation");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleDocumentationWord() {
        StringBuilder text = new StringBuilder("Documentation");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo(
                "[ ]?("
                        + ATokenRecognizer
                                .createUpperLowerCaseWord("Documentation:")
                        + "|"
                        + ATokenRecognizer
                                .createUpperLowerCaseWord("Documentation")
                        + ")");

    }


    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(
                RobotTokenType.SETTING_DOCUMENTATION_DECLARATION);
    }


    @Before
    public void setUp() {
        rec = new SettingDocumentationRecognizer();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
