package org.robotframework.ide.core.testData.text.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotLineSeparatorsContexts
 */
public class RobotLineSeparatorsContextsTest {

    @ForClean
    private RobotLineSeparatorsContexts context;


    @Test
    public void test_addingEmptyList_toExistingOne() {
        // prepare
        List<IContextElement> seps = buildListOfSeparators(
                SimpleRobotContextType.PIPE_SEPARATED, 1);
        context.addNextSeparators(seps);

        // execute
        context.addNextSeparators(new LinkedList<IContextElement>());

        // verify
        assertThat(context.getFoundSeperatorsExcludeType().asMap()).hasSize(1);
        assertThat(context.getPipeSeparators()).hasSize(1);
        assertThat(context.getPipeSeparators().get(0)).isEqualTo(seps.get(0));
        assertThat(context.getWhitespaceSeparators()).isEmpty();
    }


    @Test
    public void test_addingNull_toExistingOne() {
        // prepare
        List<IContextElement> seps = buildListOfSeparators(
                SimpleRobotContextType.PIPE_SEPARATED, 1);
        context.addNextSeparators(seps);

        // execute
        context.addNextSeparators(null);

        // verify
        assertThat(context.getFoundSeperatorsExcludeType().asMap()).hasSize(1);
        assertThat(context.getPipeSeparators()).hasSize(1);
        assertThat(context.getPipeSeparators().get(0)).isEqualTo(seps.get(0));
        assertThat(context.getWhitespaceSeparators()).isEmpty();
    }


    @Test
    public void test_addingListWith_twoWhitespaceSeparators() {
        // prepare
        List<IContextElement> seps = buildListOfSeparators(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED, 2);

        // execute
        context.addNextSeparators(seps);

        // verify
        assertThat(context.getFoundSeperatorsExcludeType().asMap()).hasSize(1);
        assertThat(context.getWhitespaceSeparators()).hasSize(2);
        IContextElement theFirstSeparatorInLine = seps.get(0);
        assertThat(context.getWhitespaceSeparators().get(0)).isEqualTo(
                theFirstSeparatorInLine);
        assertThat(context.getWhitespaceSeparators().get(1)).isEqualTo(
                seps.get(1));
        assertThat(context.getPipeSeparators()).isEmpty();

        InOrder order = inOrder(theFirstSeparatorInLine);
        order.verify(theFirstSeparatorInLine, times(1)).getType();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_addingListWith_twoPipeSeparators() {
        // prepare
        List<IContextElement> seps = buildListOfSeparators(
                SimpleRobotContextType.PIPE_SEPARATED, 2);

        // execute
        context.addNextSeparators(seps);

        // verify
        assertThat(context.getFoundSeperatorsExcludeType().asMap()).hasSize(1);
        assertThat(context.getPipeSeparators()).hasSize(2);
        IContextElement theFirstSeparatorInLine = seps.get(0);
        assertThat(context.getPipeSeparators().get(0)).isEqualTo(
                theFirstSeparatorInLine);
        assertThat(context.getPipeSeparators().get(1)).isEqualTo(seps.get(1));
        assertThat(context.getWhitespaceSeparators()).isEmpty();

        InOrder order = inOrder(theFirstSeparatorInLine);
        order.verify(theFirstSeparatorInLine, times(1)).getType();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_addingTheFirstPipeSeparated_andThen_WhiteSpace() {
        // prepare
        List<IContextElement> sepsPipe = buildListOfSeparators(
                SimpleRobotContextType.PIPE_SEPARATED, 2);
        List<IContextElement> sepsWS = buildListOfSeparators(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED, 2);

        // execute
        context.addNextSeparators(sepsPipe);
        context.addNextSeparators(sepsWS);

        // verify
        assertThat(context.getFoundSeperatorsExcludeType().asMap()).hasSize(2);
        assertThat(context.getPipeSeparators()).hasSize(2);
        assertThat(context.getWhitespaceSeparators()).hasSize(2);

        IContextElement theFirstPipe = sepsPipe.get(0);
        IContextElement theFirstWS = sepsWS.get(0);

        assertThat(context.getPipeSeparators()).containsExactlyElementsOf(
                sepsPipe);
        assertThat(context.getWhitespaceSeparators())
                .containsExactlyElementsOf(sepsWS);

        InOrder order = inOrder(theFirstPipe, theFirstWS);
        order.verify(theFirstPipe, times(1)).getType();
        order.verify(theFirstWS, times(1)).getType();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_addingEmptyListOfSeparators() {
        // prepare
        List<IContextElement> seps = buildListOfSeparators(
                SimpleRobotContextType.PIPE_SEPARATED, 0);

        // execute
        context.addNextSeparators(seps);

        // verify
        assertThat(context.getFoundSeperatorsExcludeType().asMap()).isEmpty();
        assertThat(context.getPipeSeparators()).isEmpty();
        assertThat(context.getWhitespaceSeparators()).isEmpty();
    }


    private List<IContextElement> buildListOfSeparators(
            final IContextElementType type, int numberOfElements) {
        List<IContextElement> elems = new LinkedList<>();
        for (int i = 0; i < numberOfElements; i++) {
            elems.add(buildMockContextWith(type));
        }

        return elems;
    }


    private IContextElement buildMockContextWith(final IContextElementType type) {
        IContextElement element = mock(IContextElement.class);
        when(element.getType()).thenReturn(type);

        return element;
    }


    @Test
    public void test_setAndGetParent() {
        assertThat(context.getParent()).isNull();
        IContextElement parent = mock(IContextElement.class);
        context.setParent(parent);
        assertThat(context.getParent()).isEqualTo(parent);
    }


    @Test
    public void test_getType_shouldReturn_ComplexType_SEPARATORS() {
        assertThat(context.getType()).isEqualTo(
                ComplexRobotContextType.SEPARATORS);
    }


    @Before
    public void setUp() {
        context = new RobotLineSeparatorsContexts();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
