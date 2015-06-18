package org.robotframework.ide.core.testHelpers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see CircullarArrayIterator
 */
public class CircullarArrayIteratorTest {

    @ForClean
    private CircullarArrayIterator<Integer> array;


    @Test
    public void test_forThreeElements_middleOne_isRemovedAfterTheFirstIteration() {
        // prepare
        int elementOne = 10;
        int elementTwo = 20;
        int elementThree = 30;
        Integer[] arrayInt = new Integer[] { elementOne, elementTwo,
                elementThree };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        assertThat(array.hasNext()).isTrue();
        assertThat(array.next()).isEqualTo(elementOne);
        assertThat(arrayInt).hasSize(3);
        assertThat(arrayInt).contains(elementOne, elementTwo, elementThree);

        assertThat(array.hasNext()).isTrue();
        assertThat(array.next()).isEqualTo(elementTwo);
        assertThat(arrayInt).hasSize(3);
        assertThat(arrayInt).contains(elementOne, elementTwo, elementThree);

        assertThat(array.hasNext()).isTrue();
        assertThat(array.next()).isEqualTo(elementThree);
        assertThat(arrayInt).hasSize(3);
        assertThat(arrayInt).contains(elementOne, elementTwo, elementThree);

        assertThat(array.hasNext()).isTrue();
        assertThat(array.next()).isEqualTo(elementOne);
        assertThat(arrayInt).hasSize(3);
        assertThat(arrayInt).contains(elementOne, elementTwo, elementThree);

        assertThat(array.hasNext()).isTrue();
        array.remove();

        assertThat(array.hasNext()).isTrue();
        assertThat(array.next()).isEqualTo(elementThree);
        assertThat(arrayInt).hasSize(3);
        assertThat(arrayInt).contains(elementOne, elementTwo, elementThree);

        assertThat(array.hasNext()).isTrue();
        assertThat(array.next()).isEqualTo(elementOne);
        assertThat(arrayInt).hasSize(3);
        assertThat(arrayInt).contains(elementOne, elementTwo, elementThree);
    }


    @Test
    public void test_remove_forOneElementArray_shouldNotThrownAnyException_next_shouldReturn_NULLandHasNextFALSE_executedTwice() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int execId = 0; execId < 2; execId++) {
            array.remove();
            assertThat(array.hasNext()).isFalse();
            assertThat(array.next()).isNull();
            assertThat(arrayInt).hasSize(1);
            assertThat(arrayInt).contains(elementOne);
        }
    }


    @Test
    public void test_remove_forOneElementArray_shouldNotThrownAnyException_next_shouldReturn_NULLandHasNextFALSE() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        array.remove();
        assertThat(array.hasNext()).isFalse();
        assertThat(array.next()).isNull();
        assertThat(arrayInt).hasSize(1);
        assertThat(arrayInt).contains(elementOne);
    }


    @Test
    public void test_next_forOneElementArray_shouldReturn_theFirstElement_executedThrid() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int execId = 0; execId < 3; execId++) {
            assertThat(array.next()).isEqualTo(elementOne);
            assertThat(arrayInt).hasSize(1);
            assertThat(arrayInt).contains(elementOne);
        }
    }


    @Test
    public void test_next_forOneElementArray_shouldReturn_theFirstElement_executedTwice() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int execId = 0; execId < 2; execId++) {
            assertThat(array.next()).isEqualTo(elementOne);
            assertThat(arrayInt).hasSize(1);
            assertThat(arrayInt).contains(elementOne);
        }
    }


    @Test
    public void test_next_forOneElementArray_shouldReturn_theFirstElement() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        assertThat(array.next()).isEqualTo(elementOne);
        assertThat(arrayInt).hasSize(1);
        assertThat(arrayInt).contains(elementOne);
    }


    @Test
    public void test_hasNext_forOneElementArray_shouldReturn_say_TRUE_executedThird() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int execId = 0; execId < 3; execId++) {
            assertThat(array.hasNext()).isTrue();
            assertThat(arrayInt).hasSize(1);
            assertThat(arrayInt).contains(elementOne);
        }
    }


    @Test
    public void test_hasNext_forOneElementArray_shouldReturn_say_TRUE_executedTwice() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int execId = 0; execId < 2; execId++) {
            assertThat(array.hasNext()).isTrue();
            assertThat(arrayInt).hasSize(1);
            assertThat(arrayInt).contains(elementOne);
        }
    }


    @Test
    public void test_hasNext_forOneElementArray_shouldReturn_say_TRUE() {
        // prepare
        int elementOne = 10;
        Integer[] arrayInt = new Integer[] { elementOne };
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify

        assertThat(array.hasNext()).isTrue();
        assertThat(arrayInt).hasSize(1);
        assertThat(arrayInt).contains(elementOne);
    }


    @Test
    public void test_hasNext_forEmptyArray_shouldReturn_FALSE() {
        // prepare
        Integer[] arrayInt = new Integer[0];
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int checkNr = 0; checkNr < 3; checkNr++) {
            assertThat(array.hasNext()).isFalse();
            assertThat(arrayInt).isEmpty();
        }
    }


    @Test
    public void test_next_forEmptyArray_shouldReturn_NULL() {
        // prepare
        Integer[] arrayInt = new Integer[0];
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int checkNr = 0; checkNr < 3; checkNr++) {
            assertThat(array.next()).isNull();
            assertThat(arrayInt).isEmpty();
        }
    }


    @Test
    public void test_remove_forEmptyArray_shouldNotThrownAnyException() {
        // prepare
        Integer[] arrayInt = new Integer[0];
        array = new CircullarArrayIterator<>(arrayInt);

        // execute & verify
        for (int checkNr = 0; checkNr < 3; checkNr++) {
            array.remove();
            assertThat(arrayInt).isEmpty();
        }
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
