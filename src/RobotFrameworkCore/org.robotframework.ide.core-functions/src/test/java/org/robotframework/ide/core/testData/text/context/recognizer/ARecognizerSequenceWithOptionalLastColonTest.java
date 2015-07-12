package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;


/**
 * Add functionality of testing sequence of words with optional colon at the
 * end. Its mainly for Settings table propose.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * 
 */
public abstract class ARecognizerSequenceWithOptionalLastColonTest extends
        ARecognizerTest {

    protected ARecognizerSequenceWithOptionalLastColonTest(
            Class<? extends ATableElementRecognizer> recognizerClass) {
        super(recognizerClass);
    }


    protected void assertExpectedSequenceAllMandatory(IRobotTokenType... types) {
        List<ExpectedSequenceElement> sequence = new LinkedList<>();
        for (IRobotTokenType t : types) {
            sequence.add(ExpectedSequenceElement.buildMandatory(t));
        }
        sequence.add(ExpectedSequenceElement
                .buildOptional(RobotSingleCharTokenType.SINGLE_COLON));

        assertThat(((ATableElementRecognizer) context).getExpectedElements())
                .containsExactlyElementsOf(sequence);
    }
}
