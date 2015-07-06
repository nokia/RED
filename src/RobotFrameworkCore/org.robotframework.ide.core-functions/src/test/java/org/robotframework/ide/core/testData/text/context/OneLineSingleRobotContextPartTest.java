package org.robotframework.ide.core.testData.text.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see OneLineSingleRobotContextPart
 */
public class OneLineSingleRobotContextPartTest {

    @ForClean
    private OneLineSingleRobotContextPart context;


    @Test
    public void test_addingParentWhichIsNot_AggregatedOneLineContextType_shouldThrown_anException() {
        // prepare
        IContextElement notCorrectClass = mock(IContextElement.class);

        // execute
        try {
            context.setParent(notCorrectClass);
            fail("Should thrown IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(
                    "Context should be instance of "
                            + AggregatedOneLineRobotContexts.class
                            + ", but was " + notCorrectClass.getClass());
        }
    }


    @Test
    public void test_removeMethod_shouldContains_emptyList() {
        // prepare
        RobotToken tokenOne = mock(RobotToken.class);
        RobotToken tokenTwo = mock(RobotToken.class);
        RobotToken tokenThree = mock(RobotToken.class);
        context.addNextToken(tokenOne);
        context.addNextToken(tokenTwo);
        context.addNextToken(tokenThree);

        // execute
        context.removeAllContextTokens();

        // verify
        assertThat(context.getContextTokens()).isEmpty();
    }


    @Test
    public void test_addNextTokenForContextExecutedThirdTime() {
        // prepare
        RobotToken tokenOne = mock(RobotToken.class);
        RobotToken tokenTwo = mock(RobotToken.class);
        RobotToken tokenThree = mock(RobotToken.class);

        // execute & verify
        context.addNextToken(tokenOne);
        List<RobotToken> contextTokens = context.getContextTokens();
        assertThat(contextTokens).containsSequence(tokenOne);

        context.addNextToken(tokenTwo);
        assertThat(contextTokens).containsSequence(tokenOne, tokenTwo);

        context.addNextToken(tokenThree);
        assertThat(contextTokens).containsSequence(tokenOne, tokenTwo,
                tokenThree);
    }


    @Test
    public void test_getAndSetParentContext() {
        assertThat(context.getParent()).isNull();

        AggregatedOneLineRobotContexts parent = mock(AggregatedOneLineRobotContexts.class);
        context.setParent(parent);
        assertThat(context.getParent()).isEqualTo(parent);
    }


    @Test
    public void test_getAndSetType() {
        assertThat(context.getType()).isEqualTo(
                SimpleRobotContextType.UNDECLARED_COMMENT);

        // set-get test
        context.setType(SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertThat(context.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
    }


    @Test
    public void test_getLineNumber_shouldReturn_THE_FIRST_LINE() {
        assertThat(context.getLineNumber()).isEqualTo(
                LinearPositionMarker.THE_FIRST_LINE);
    }


    @Before
    public void setUp() {
        context = new OneLineSingleRobotContextPart(
                LinearPositionMarker.THE_FIRST_LINE);
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
