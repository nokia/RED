package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * 
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see NumberType
 */
public class NumberTypeTest {

    @Test
    public void test_getTokenType_forNumberWithSign() {
        // prepare
        NumberType expectedType = NumberType.NUMBER_WITH_SIGN;
        String text = "-2";

        assertReturnType(expectedType, text);
    }


    @Test
    public void test_getTokenType_forNumberWithoutSign() {
        // prepare
        NumberType expectedType = NumberType.NUMBER_WITHOUT_SIGN;
        String text = "2";

        assertReturnType(expectedType, text);
    }


    @Test
    public void test_getTokenType_forIncorrectValueWithFloatingPoint() {
        // prepare
        NumberType expectedType = NumberType.UNKNOWN;
        String text = "-2.0";

        assertReturnType(expectedType, text);
    }


    @Test
    public void test_getTokenType_forIncorrectValue() {
        // prepare
        NumberType expectedType = NumberType.UNKNOWN;
        String text = "-abc";

        assertReturnType(expectedType, text);
    }


    private void assertReturnType(NumberType expectedType, String text) {
        NumberType type = NumberType.UNKNOWN;
        StringBuilder textBuilder = new StringBuilder(text);

        assertThat(type.getTokenType(text)).isEqualTo(expectedType);
        assertThat(type.getTokenType(textBuilder)).isEqualTo(expectedType);
    }


    @Test
    public void test_typeNUMBER_WITHOUT_SIGN_isIncorrect() {
        NumberType numberWithoutSign = NumberType.NUMBER_WITHOUT_SIGN;

        assertThat(numberWithoutSign.isMine("-2")).isFalse();
        assertThat(numberWithoutSign.isWriteable()).isFalse();
        try {
            numberWithoutSign.toWrite();
            fail("exception expected");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + numberWithoutSign);
        }
    }


    @Test
    public void test_typeNUMBER_WITHOUT_SIGN_isCorrect() {
        NumberType numberWithoutSign = NumberType.NUMBER_WITHOUT_SIGN;

        assertThat(numberWithoutSign.isMine("2")).isTrue();
        assertThat(numberWithoutSign.isWriteable()).isFalse();
        try {
            numberWithoutSign.toWrite();
            fail("exception expected");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + numberWithoutSign);
        }
    }


    @Test
    public void test_typeNUMBER_WITH_SIGN_isIncorrect() {
        NumberType numberWithSign = NumberType.NUMBER_WITH_SIGN;

        assertThat(numberWithSign.isMine("2")).isFalse();
        assertThat(numberWithSign.isWriteable()).isFalse();
        try {
            numberWithSign.toWrite();
            fail("exception expected");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + numberWithSign);
        }
    }


    @Test
    public void test_typeNUMBER_WITH_SIGN_isCorrect() {
        NumberType numberWithSign = NumberType.NUMBER_WITH_SIGN;

        assertThat(numberWithSign.isMine("-2")).isTrue();
        assertThat(numberWithSign.isWriteable()).isFalse();
        try {
            numberWithSign.toWrite();
            fail("exception expected");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + numberWithSign);
        }
    }


    @Test
    public void test_typeUNKNOWN_shouldAlwaysReturn_FALSE() {
        NumberType unknown = NumberType.UNKNOWN;

        assertThat(unknown.isMine("2.00")).isFalse();
        assertThat(unknown.isMine("-2")).isFalse();
        assertThat(unknown.isMine("2")).isFalse();

        assertThat(unknown.isWriteable()).isFalse();
        try {
            unknown.toWrite();
            fail("exception expected");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + unknown);
        }
    }
}
