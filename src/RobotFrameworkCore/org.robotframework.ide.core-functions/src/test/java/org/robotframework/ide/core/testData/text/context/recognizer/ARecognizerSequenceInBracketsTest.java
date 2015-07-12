package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Add functionality of testing sequence of words wrapped in square brackets.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * 
 */
public abstract class ARecognizerSequenceInBracketsTest extends ARecognizerTest {

    protected ARecognizerSequenceInBracketsTest(
            Class<? extends ATableElementRecognizer> recognizerClass) {
        super(recognizerClass);
    }


    protected void assertExpectedSequenceAllMandatory(IRobotTokenType... types) {
        List<ExpectedSequenceElement> sequence = new LinkedList<>();
        sequence.add(ExpectedSequenceElement
                .buildMandatory(RobotSingleCharTokenType.SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET));
        for (IRobotTokenType t : types) {
            sequence.add(ExpectedSequenceElement.buildMandatory(t));
        }
        sequence.add(ExpectedSequenceElement
                .buildMandatory(RobotSingleCharTokenType.SINGLE_POSITION_INDEX_END_SQUARE_BRACKET));

        assertThat(((ATableElementRecognizer) context).getExpectedElements())
                .containsExactlyElementsOf(sequence);
    }
}
