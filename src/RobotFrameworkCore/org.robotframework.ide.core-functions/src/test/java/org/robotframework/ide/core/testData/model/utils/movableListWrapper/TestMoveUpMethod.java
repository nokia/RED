package org.robotframework.ide.core.testData.model.utils.movableListWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.utils.MovableLinkedListWrapper;


/**
 * @author wypych
 * @see MovableLinkedListWrapper#moveUp(int)
 */
public class TestMoveUpMethod {

    private LinkedList<Integer> luckyNumbers;

    private MovableLinkedListWrapper<Integer> testObject;


    @Test
    public void test_move_element_1_to_0_inEmptyList_shouldReturn_False() {
        int index = 1;
        assertFalse("List is empty move element " + index + " to "
                + (index - 1) + " is not possible", testObject.moveUp(index));
    }


    @Test
    public void test_move_element_minus_1_inEmptyList_shouldReturn_False() {
        int index = -1;
        assertFalse("Move negative index " + index + " is not possible",
                testObject.moveUp(index));
    }


    @Test
    public void test_move_element_0_inOneElementList_shouldReturn_False() {
        int index = 0;
        luckyNumbers.add(0);
        assertFalse("Move element " + index + " upper is not possible",
                testObject.moveUp(index));
        assertThat(luckyNumbers).hasSize(1).contains(0, atIndex(0));
    }


    @Test
    public void test_move_element_minus_1_inOneElementList_shouldReturn_False() {
        int index = -1;
        luckyNumbers.add(0);
        assertFalse("Move negative index " + index + " is not possible",
                testObject.moveUp(index));
        assertThat(luckyNumbers).hasSize(1).contains(0, atIndex(0));
    }


    @Test
    public void test_move_nonExisting_element_2_inOneElementList_shouldReturn_False() {
        int index = 2;
        luckyNumbers.add(0);
        assertFalse(
                "Moving not existing element " + index + " is not possible",
                testObject.moveUp(index));
        assertThat(luckyNumbers).hasSize(1).contains(0, atIndex(0));
    }


    @Test
    public void test_move_element_0_inTwoElementList_shouldReturn_False() {
        int index = 0;
        luckyNumbers.add(0);
        luckyNumbers.add(1);
        assertFalse("Moving element " + index + " is not possible",
                testObject.moveUp(index));
        assertThat(luckyNumbers).hasSize(2).contains(0, atIndex(0))
                .contains(1, atIndex(1));
    }


    @Test
    public void test_move_element_1_inTwoElementList_shouldReturn_True() {
        int index = 1;
        luckyNumbers.add(0);
        luckyNumbers.add(1);
        assertTrue("Moving element " + index + " should be possible",
                testObject.moveUp(index));
        assertThat(luckyNumbers).hasSize(2).contains(1, atIndex(0))
                .contains(0, atIndex(1));
    }


    @Test
    public void test_move_element_2_inThreeElementList_shouldReturn_True() {
        int index = 2;
        luckyNumbers.add(0);
        luckyNumbers.add(1);
        luckyNumbers.add(2);
        assertTrue("Moving element " + index + " should be possible",
                testObject.moveUp(index));
        assertThat(luckyNumbers).hasSize(3).contains(0, atIndex(0))
                .contains(2, atIndex(1)).contains(1, atIndex(2));
    }


    @Test
    public void test_move_element_1_inThreeElementList_shouldReturn_True() {
        int index = 1;
        luckyNumbers.add(0);
        luckyNumbers.add(1);
        luckyNumbers.add(2);
        assertTrue("Moving element " + index + " should be possible",
                testObject.moveUp(index));
        assertThat(luckyNumbers).hasSize(3).contains(1, atIndex(0))
                .contains(0, atIndex(1)).contains(2, atIndex(2));
    }


    @Before
    public void setUp() {
        luckyNumbers = new LinkedList<Integer>();
        testObject = new MovableLinkedListWrapper<Integer>(luckyNumbers);
    }


    @After
    public void tearDown() {
        // to not overkill too much Garbage Collector
        luckyNumbers = null;
        testObject = null;
    }
}
