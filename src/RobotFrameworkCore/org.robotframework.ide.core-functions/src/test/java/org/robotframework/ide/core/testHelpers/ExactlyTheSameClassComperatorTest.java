package org.robotframework.ide.core.testHelpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ExactlyTheSameClassComperator
 */
public class ExactlyTheSameClassComperatorTest {

    @ForClean
    private ExactlyTheSameClassComperator compare;


    @Test
    public void test_compare_for_emptyLinkedListAndArrayList_shouldReturn_minus1() {
        assertThat(compare.compare(new LinkedList<>(), new ArrayList<>(0)))
                .isEqualTo(-1);
    }


    @Test
    public void test_compare_for_bothObjectsAreFromString_shouldReturn_0() {
        assertThat(compare.compare(new String("foo"), new String("bar")))
                .isEqualTo(0);
    }


    @Test
    public void test_compare_for_firstObjectIsFromObject_theSecondFromString_shouldReturn_minus1() {
        assertThat(compare.compare(new Object(), new String())).isEqualTo(-1);
    }


    @Test
    public void test_compare_for_theSecondObjectNull_shouldReturn_minus1() {
        assertThat(compare.compare(new Object(), null)).isEqualTo(-1);
    }


    @Test
    public void test_compare_for_theFirstObjectNull_shouldReturn_minus1() {
        assertThat(compare.compare(null, new Object())).isEqualTo(-1);
    }


    @Test
    public void test_compare_for_bothObjectNulls_shouldReturn_minus1() {
        assertThat(compare.compare(null, null)).isEqualTo(-1);
    }


    @Before
    public void setUp() {
        compare = new ExactlyTheSameClassComperator();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
