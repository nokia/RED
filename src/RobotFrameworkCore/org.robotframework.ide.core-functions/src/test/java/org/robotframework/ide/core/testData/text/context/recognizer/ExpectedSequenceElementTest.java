package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.core.testData.text.context.recognizer.ExpectedSequenceElement.PriorityType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ExpectedSequenceElement
 */
public class ExpectedSequenceElementTest {

    @Test
    public void test_buildMandatory_element() {
        ExpectedSequenceElement elem = ExpectedSequenceElement
                .buildMandatory(RobotSingleCharTokenType.END_OF_LINE);

        assertThat(elem).isNotNull();
        assertThat(elem.getType()).isEqualTo(
                RobotSingleCharTokenType.END_OF_LINE);
        assertThat(elem.getPriority()).isEqualTo(PriorityType.MANDATORY);
    }


    @Test
    public void test_buildOptional_element() {
        ExpectedSequenceElement elem = ExpectedSequenceElement
                .buildOptional(RobotSingleCharTokenType.END_OF_LINE);

        assertThat(elem).isNotNull();
        assertThat(elem.getType()).isEqualTo(
                RobotSingleCharTokenType.END_OF_LINE);
        assertThat(elem.getPriority()).isEqualTo(PriorityType.OPTIONAL);
    }
}
