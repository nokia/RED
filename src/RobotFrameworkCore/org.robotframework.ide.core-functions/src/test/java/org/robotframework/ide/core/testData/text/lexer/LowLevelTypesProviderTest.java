package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see LowLevelTypesProvider
 */
public class LowLevelTypesProviderTest {

    @Test
    public void test_getTokenType_of_type_asterisks_shouldReturn_UNKNOWN_type() {
        String text = "******************";
        RobotType expectedType = RobotWordType.UNKNOWN_WORD;

        assertTokenTypeReturn(text, expectedType);
    }


    @Test
    public void test_getTokenType_of_type_time_MILLISECOND() {
        String text = "ms";
        RobotType expectedType = RobotTimeType.MILLISECOND;

        assertTokenTypeReturn(text, expectedType);
    }


    @Test
    public void test_getTokenType_of_type_time_SECOND() {
        String text = "s";
        RobotType expectedType = RobotTimeType.SECOND;

        assertTokenTypeReturn(text, expectedType);
    }


    @Test
    public void test_getTokenType_of_type_time_MINUTE() {
        String text = "m";
        RobotType expectedType = RobotTimeType.MINUTE;

        assertTokenTypeReturn(text, expectedType);
    }


    @Test
    public void test_getTokenType_of_type_time_HOUR() {
        String text = "h";
        RobotType expectedType = RobotTimeType.HOUR;

        assertTokenTypeReturn(text, expectedType);
    }


    @Test
    public void test_getTokenType_of_type_time_DAY() {
        String text = "d";
        RobotType expectedType = RobotTimeType.DAY;

        assertTokenTypeReturn(text, expectedType);
    }


    @Test
    public void test_getTokenType_of_type_numberWithoutSign() {
        String text = "2";
        RobotType expectedType = NumberType.NUMBER_WITHOUT_SIGN;

        assertTokenTypeReturn(text, expectedType);
    }


    @Test
    public void test_getTokenType_of_type_numberWithSign() {
        String text = "-2";
        RobotType expectedType = NumberType.NUMBER_WITH_SIGN;

        assertTokenTypeReturn(text, expectedType);
    }


    private void assertTokenTypeReturn(String text, RobotType expectedType) {
        assertThat(LowLevelTypesProvider.getTokenType(text)).isEqualTo(
                expectedType);
        StringBuilder str = new StringBuilder(text);
        assertThat(LowLevelTypesProvider.getTokenType(str)).isEqualTo(
                expectedType);
    }


    @Test
    public void test_ifLowLevelTypesProvider_contains_onlyExpectedTypes() {
        assertThat(LowLevelTypesProvider.getDeclaredRobotTypes())
                .containsExactlyElementsOf(
                        Arrays.asList(RobotTimeType.UNKNOWN, NumberType.UNKNOWN));
    }


    @Test
    public void test_getTokenType_StringBuilder_nullValue_shouldReturn_UNKNOWN_WORD() {
        StringBuilder text = null;
        assertThat(LowLevelTypesProvider.getTokenType(text)).isEqualTo(
                RobotWordType.UNKNOWN_WORD);
    }


    @Test
    public void test_getTokenType_String_nullValue_shouldReturn_UNKNOWN_WORD() {
        String text = null;
        assertThat(LowLevelTypesProvider.getTokenType(text)).isEqualTo(
                RobotWordType.UNKNOWN_WORD);
    }
}
