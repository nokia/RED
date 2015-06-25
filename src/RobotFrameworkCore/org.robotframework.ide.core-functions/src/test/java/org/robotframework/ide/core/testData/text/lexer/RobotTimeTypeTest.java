package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.robotframework.ide.core.testHelpers.CombinationGenerator;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTimeType
 */
public class RobotTimeTypeTest {

    @Test
    public void test_typeMILLLISECOND() {
        RobotTimeType millisecond = RobotTimeType.MILLISECOND;
        assertThatIsMineFor(millisecond,
                Arrays.asList("ms", "millis", "millisecond", "milliseconds"));
        assertThat(millisecond.toWrite()).isEqualTo("millisecond");
    }


    @Test
    public void test_typeSECOND() {
        RobotTimeType second = RobotTimeType.SECOND;
        assertThatIsMineFor(second,
                Arrays.asList("s", "sec", "secs", "second", "seconds"));
        assertThat(second.toWrite()).isEqualTo("second");
    }


    @Test
    public void test_typeMINUTE() {
        RobotTimeType minute = RobotTimeType.MINUTE;

        assertThatIsMineFor(minute, Arrays.asList("m", "minute", "minutes"));
        assertThat(minute.toWrite()).isEqualTo("minute");
    }


    @Test
    public void test_typeHOUR() {
        RobotTimeType hour = RobotTimeType.HOUR;

        assertThatIsMineFor(hour, Arrays.asList("h", "hour", "hours"));
        assertThat(hour.toWrite()).isEqualTo("hour");
    }


    @Test
    public void test_typeDAY() {
        RobotTimeType day = RobotTimeType.DAY;

        assertThatIsMineFor(day, Arrays.asList("d", "day", "days"));
        assertThat(day.toWrite()).isEqualTo("day");
    }


    private void assertThatIsMineFor(RobotTimeType type,
            List<String> possibilities) {
        CombinationGenerator gen = new CombinationGenerator();
        for (String pos : possibilities) {
            List<String> combinations = gen.combinations(pos);
            for (String combination : combinations) {
                assertThat(type.isMine(combination)).isTrue();
                assertThat(RobotTimeType.UNKNOWN.getTokenType(combination))
                        .isEqualTo(type);
                assertThat(
                        RobotTimeType.UNKNOWN.getTokenType(new StringBuilder(
                                combination))).isEqualTo(type);
            }
        }

        assertThat(type.getPossibleRepresentations())
                .containsExactlyElementsOf(possibilities);
    }


    @Test
    public void test_typeUNKNOWN_shouldAlwaysReturn_FALSE() {
        RobotTimeType unknown = RobotTimeType.UNKNOWN;

        assertThat(unknown.isMine("day")).isFalse();
        assertThat(unknown.isMine("foobar")).isFalse();
        assertThat(unknown.getPossibleRepresentations()).isEmpty();
        assertThat(unknown.isWriteable()).isFalse();
        assertThat(unknown.toWrite()).isNull();
    }
}
