package org.robotframework.ide.core.testData.text.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AggregatedOneLineRobotContexts
 */
public class AggregatedOneLineRobotContextTest {

    @ForClean
    private AggregatedOneLineRobotContexts context;


    @Test
    public void test_addNewLineContext() {
        // prepare
        IContextElement elementOne = mock(IContextElement.class);
        when(elementOne.getType()).thenReturn(
                SimpleRobotContextType.QUOTES_SENTENCE);

        // execute
        context.addNextLineContext(elementOne);

        // verify
        assertThat(context.getChildContexts()).containsSequence(elementOne);
        assertThat(context.getChildContextTypes().asMap()).hasSize(1);
        List<IContextElement> list = context.getChildContextTypes().get(
                SimpleRobotContextType.QUOTES_SENTENCE);
        assertThat(list).isNotNull();
        assertThat(list).isNotEmpty();
        assertThat(list).containsSequence(elementOne);
    }


    @Test
    public void test_setAndGet_type() {
        // prepare
        ComplexRobotContextType type = ComplexRobotContextType.SEPARATORS;

        // execute & verify
        assertThat(context.getType()).isEqualTo(
                ComplexRobotContextType.UNDECLARED_COMMENT);
        context.setType(type);
        assertThat(context.getType()).isEqualTo(type);
    }


    @Test
    public void test_setAndGet_parentContext() {
        // prepare
        IContextElement parent = mock(IContextElement.class);

        // execute & verify
        assertThat(context.getParent()).isNull();
        context.setParent(parent);
        assertThat(context.getParent()).isEqualTo(parent);
    }


    @Test
    public void test_setAndGet_separators() {
        // prepare
        RobotLineSeparatorsContexts seps = mock(RobotLineSeparatorsContexts.class);

        // execute & verify
        assertThat(context.getSeparators()).isNotNull();
        context.setSeparators(seps);
        assertThat(context.getSeparators()).isEqualTo(seps);
    }


    @Before
    public void setUp() {
        context = new AggregatedOneLineRobotContexts();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
