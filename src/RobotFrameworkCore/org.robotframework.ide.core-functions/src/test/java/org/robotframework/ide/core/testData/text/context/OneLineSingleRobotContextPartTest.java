package org.robotframework.ide.core.testData.text.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
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
