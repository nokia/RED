package org.robotframework.ide.core.testData.model.util.movableListWrapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.util.MovableLinkedListWrapper;


/**
 * 
 * @author wypych
 * @see MovableLinkedListWrapper#add(Object)
 * @see MovableLinkedListWrapper#delete(int)
 * @see MovableLinkedListWrapper#get(int)
 * @see MovableLinkedListWrapper#iterator()
 * @see MovableLinkedListWrapper#size()
 */
public class TestCRUDMethods {

    private MovableLinkedListWrapper<Integer> movable;


    @Test
    public void test_add_three_elements() {
        // verify
        assertThat(movable).hasSize(3).containsSequence(1, 2, 3);
    }


    @Test
    public void test_delete_one_element() {
        // execute
        movable.delete(0);

        // verify
        assertThat(movable).hasSize(2).containsSequence(2, 3);
    }


    @Test
    public void test_middle_element_in_three_element_list() {
        // verify & execute
        for (int i = 0; i < 3; i++) {
            assertThat(movable.get(i)).isEqualTo(i + 1);
        }

        assertThat(movable).hasSize(3).containsSequence(1, 2, 3);
    }


    @Test
    public void test_iterator() {
        // first check if is iterable
        int k = 0;
        for (Integer i : movable) {
            // first we increase value before we return it
            assertThat(i).isEqualTo(++k);
        }
    }


    @Test
    public void test_checkSize_forEmptyList() {
        // get clean list
        movable = new MovableLinkedListWrapper<Integer>();

        assertThat(movable).isEmpty();
        assertThat(movable.size()).isEqualTo(0);
    }


    @Test
    public void test_checkSize_forOneElementList() {
        // get clean list
        movable = new MovableLinkedListWrapper<Integer>();

        movable.add(0);
        assertThat(movable).hasSize(1).containsSequence(0);
        assertThat(movable.size()).isEqualTo(1);
    }


    @Before
    public void setUp() {
        movable = new MovableLinkedListWrapper<Integer>();

        movable.add(1);
        movable.add(2);
        movable.add(3);
    }


    @After
    public void tearDown() {
        movable = null;
    }
}
