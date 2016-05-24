/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class MoveElementHelperTest {

    @Test
    public void zeroElements_tryToMoveUp_shouldReturn_False() {
        assertThat(MoveElementHelper.moveUp(Arrays.asList(), "A")).isFalse();
    }

    @Test
    public void zeroElements_tryToMoveDown_shouldReturn_False() {
        assertThat(MoveElementHelper.moveDown(Arrays.asList(), "A")).isFalse();
    }

    @Test
    public void oneElement_tryToMoveUp_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        final List<String> list = newArrayList(elemOne);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveUp(list, elemOne);

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne);
    }

    @Test
    public void oneElement_tryToMoveDown_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        final List<String> list = newArrayList(elemOne);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveDown(list, elemOne);

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne);
    }

    @Test
    public void twoElements_tryToMoveUp_notExistElement_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        final List<String> list = newArrayList(elemOne, elemTwo);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveUp(list, "C");

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne, elemTwo);
    }

    @Test
    public void twoElements_tryToMoveDown_notExistElement_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        final List<String> list = newArrayList(elemOne, elemTwo);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveDown(list, "C");

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne, elemTwo);
    }

    @Test
    public void twoElements_tryToMoveUp_theFirstOne_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        final List<String> list = newArrayList(elemOne, elemTwo);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveUp(list, elemOne);

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne, elemTwo);
    }

    @Test
    public void twoElements_tryToMoveDown_theFirstOne_shouldReturn_True() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        final List<String> list = newArrayList(elemOne, elemTwo);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveDown(list, elemOne);

        // verify
        assertThat(result).isTrue();
        assertThat(list).containsExactly(elemTwo, elemOne);
    }

    @Test
    public void twoElements_tryToMoveUp_theSecondOne_shouldReturn_True() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        final List<String> list = newArrayList(elemOne, elemTwo);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveUp(list, elemTwo);

        // verify
        assertThat(result).isTrue();
        assertThat(list).containsExactly(elemTwo, elemOne);
    }

    @Test
    public void twoElements_tryToMoveDown_theSecondOne_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        final List<String> list = newArrayList(elemOne, elemTwo);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveDown(list, elemTwo);

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne, elemTwo);
    }

    @Test
    public void threeElements_tryToMoveUp_theFirstOne_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        String elemThree = "C";
        final List<String> list = newArrayList(elemOne, elemTwo, elemThree);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveUp(list, elemOne);

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne, elemTwo, elemThree);
    }

    @Test
    public void threeElements_tryToDownUp_theFirstOne_shouldReturn_True() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        String elemThree = "C";
        final List<String> list = newArrayList(elemOne, elemTwo, elemThree);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveDown(list, elemOne);

        // verify
        assertThat(result).isTrue();
        assertThat(list).containsExactly(elemTwo, elemOne, elemThree);
    }

    @Test
    public void threeElements_tryToMoveUp_theSecondOne_shouldReturn_True() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        String elemThree = "C";
        final List<String> list = newArrayList(elemOne, elemTwo, elemThree);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveUp(list, elemTwo);

        // verify
        assertThat(result).isTrue();
        assertThat(list).containsExactly(elemTwo, elemOne, elemThree);
    }

    @Test
    public void threeElements_tryToDownUp_theSecondOne_shouldReturn_True() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        String elemThree = "C";
        final List<String> list = newArrayList(elemOne, elemTwo, elemThree);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveDown(list, elemTwo);

        // verify
        assertThat(result).isTrue();
        assertThat(list).containsExactly(elemOne, elemThree, elemTwo);
    }

    @Test
    public void threeElements_tryToMoveUp_theThirdOne_shouldReturn_True() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        String elemThree = "C";
        final List<String> list = newArrayList(elemOne, elemTwo, elemThree);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveUp(list, elemThree);

        // verify
        assertThat(result).isTrue();
        assertThat(list).containsExactly(elemOne, elemThree, elemTwo);
    }

    @Test
    public void threeElements_tryToDownUp_theThirdOne_shouldReturn_False() {
        // prepare
        String elemOne = "A";
        String elemTwo = "B";
        String elemThree = "C";
        final List<String> list = newArrayList(elemOne, elemTwo, elemThree);
        boolean result = false;

        // execute
        result = MoveElementHelper.moveDown(list, elemThree);

        // verify
        assertThat(result).isFalse();
        assertThat(list).containsExactly(elemOne, elemTwo, elemThree);
    }
}
