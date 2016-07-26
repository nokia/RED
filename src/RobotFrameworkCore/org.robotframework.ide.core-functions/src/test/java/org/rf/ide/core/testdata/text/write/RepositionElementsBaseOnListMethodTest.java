/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.separators.Separator;

/**
 * @author wypych
 */
public class RepositionElementsBaseOnListMethodTest {

    private RobotFileDumperInheritance testable;

    @Before
    public void setUp() {
        this.testable = new RobotFileDumperInheritance();
    }

    @SuppressWarnings("unchecked")
    @Test(timeout = 10000)
    public void twoCorrectors_inWrongPositionShouldPutInCorrectOrder_theSecondIsNew() {
        // prepare
        final List<AModelElement<SettingTable>> correctorsList = new ArrayList<AModelElement<SettingTable>>();
        final AModelElement<SettingTable> correctorOne = mock(AModelElement.class);
        when(correctorOne.getBeginPosition()).thenReturn(new FilePosition(1, 1, 1));
        final AModelElement<SettingTable> correctorTwo = mock(AModelElement.class);
        when(correctorTwo.getBeginPosition()).thenReturn(FilePosition.createNotSet());
        correctorsList.add(correctorOne);
        correctorsList.add(correctorTwo);
        final List<? extends AModelElement<SettingTable>> correctors = spy(correctorsList);

        final LimitedSizeList<AModelElement<SettingTable>> srcList = new LimitedSizeList<AModelElement<SettingTable>>();
        srcList.add(correctorTwo);
        srcList.add(correctorOne);
        srcList.setMaxSize(srcList.size() + 1);
        final List<AModelElement<SettingTable>> src = spy(srcList);

        // execute
        testable.repositionElementsBaseOnList(src, correctors);

        // verify
        assertThat(srcList).containsExactly(correctorOne, correctorTwo);
        assertThat(correctorsList).containsExactly(correctorOne, correctorTwo);
    }

    @SuppressWarnings("unchecked")
    @Test(timeout = 10000)
    public void twoCorrectors_inWrongPositionShouldPutInCorrectOrder_theFirstIsNew() {
        // prepare
        final List<AModelElement<SettingTable>> correctorsList = new ArrayList<AModelElement<SettingTable>>();
        final AModelElement<SettingTable> correctorOne = mock(AModelElement.class);
        when(correctorOne.getBeginPosition()).thenReturn(FilePosition.createNotSet());
        final AModelElement<SettingTable> correctorTwo = mock(AModelElement.class);
        when(correctorTwo.getBeginPosition()).thenReturn(new FilePosition(1, 1, 1));
        correctorsList.add(correctorOne);
        correctorsList.add(correctorTwo);
        final List<? extends AModelElement<SettingTable>> correctors = spy(correctorsList);

        final LimitedSizeList<AModelElement<SettingTable>> srcList = new LimitedSizeList<AModelElement<SettingTable>>();
        srcList.add(correctorTwo);
        srcList.add(correctorOne);
        srcList.setMaxSize(srcList.size() + 1);
        final List<AModelElement<SettingTable>> src = spy(srcList);

        // execute
        testable.repositionElementsBaseOnList(src, correctors);

        // verify
        assertThat(srcList).containsExactly(correctorOne, correctorTwo);
        assertThat(correctorsList).containsExactly(correctorOne, correctorTwo);
    }

    @SuppressWarnings("unchecked")
    @Test(timeout = 10000)
    public void twoCorrectors_inWrongPositionShouldPutInCorrectOrder_bothAreNew() {
        // prepare
        final List<AModelElement<SettingTable>> correctorsList = new ArrayList<AModelElement<SettingTable>>();
        final AModelElement<SettingTable> correctorOne = mock(AModelElement.class);
        when(correctorOne.getBeginPosition()).thenReturn(FilePosition.createNotSet());
        final AModelElement<SettingTable> correctorTwo = mock(AModelElement.class);
        when(correctorTwo.getBeginPosition()).thenReturn(FilePosition.createNotSet());
        correctorsList.add(correctorOne);
        correctorsList.add(correctorTwo);
        final List<? extends AModelElement<SettingTable>> correctors = spy(correctorsList);

        final LimitedSizeList<AModelElement<SettingTable>> srcList = new LimitedSizeList<AModelElement<SettingTable>>();
        srcList.add(correctorTwo);
        srcList.add(correctorOne);
        srcList.setMaxSize(srcList.size() + 1);
        final List<AModelElement<SettingTable>> src = spy(srcList);

        // execute
        testable.repositionElementsBaseOnList(src, correctors);

        // verify
        assertThat(srcList).containsExactly(correctorOne, correctorTwo);
        assertThat(correctorsList).containsExactly(correctorOne, correctorTwo);
    }

    @SuppressWarnings("unchecked")
    @Test(timeout = 10000)
    public void oneCorrector_shouldDoNothing() {
        // prepare
        final LimitedSizeList<AModelElement<SettingTable>> srcList = new LimitedSizeList<AModelElement<SettingTable>>();
        srcList.setMaxSize(1);
        final List<AModelElement<SettingTable>> src = spy(srcList);
        final List<AModelElement<SettingTable>> correctorsList = new ArrayList<AModelElement<SettingTable>>();
        final AModelElement<SettingTable> correctorOne = mock(AModelElement.class);
        correctorsList.add(correctorOne);
        final List<? extends AModelElement<SettingTable>> correctors = spy(correctorsList);

        // execute
        testable.repositionElementsBaseOnList(src, correctors);

        // verify
        InOrder order = inOrder(src, correctors);
        order.verify(correctors, times(1)).size();
        order.verifyNoMoreInteractions();

        assertThat(srcList).isEmpty();
        assertThat(correctorsList).containsOnly(correctorOne);
    }

    @Test(timeout = 10000)
    public void empty_correctors_shouldDoNothing() {
        // prepare
        final List<AModelElement<SettingTable>> srcList = new LimitedSizeList<AModelElement<SettingTable>>();
        final List<AModelElement<SettingTable>> src = spy(srcList);
        final List<AModelElement<SettingTable>> correctorsList = new ArrayList<AModelElement<SettingTable>>();
        final List<? extends AModelElement<SettingTable>> correctors = spy(correctorsList);

        // execute
        testable.repositionElementsBaseOnList(src, correctors);

        // verify
        InOrder order = inOrder(src, correctors);
        order.verify(correctors, times(1)).size();
        order.verifyNoMoreInteractions();

        assertThat(srcList).isEmpty();
        assertThat(correctorsList).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_isNextTheSameAsCurrent_enoughElementsAndTheSame_shouldReturn_TRUE() {
        // prepare
        final List<AModelElement<SettingTable>> srcList = new ArrayList<AModelElement<SettingTable>>();
        srcList.add(mock(AModelElement.class));
        final AModelElement<SettingTable> m = mock(AModelElement.class);
        srcList.add(m);
        final List<AModelElement<SettingTable>> src = spy(srcList);

        // execute
        boolean result = testable.isNextTheSameAsCurrent(src, m, 0);

        // verify
        assertThat(result).isTrue();

        InOrder order = inOrder(src);
        order.verify(src, times(1)).size();
        order.verify(src, times(1)).get(1);
        order.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_isNextTheSameAsCurrent_enoughElementsButNotTheSame_shouldReturn_FALSE() {
        // prepare
        final List<AModelElement<SettingTable>> srcList = new ArrayList<AModelElement<SettingTable>>();
        srcList.add(mock(AModelElement.class));
        srcList.add(mock(AModelElement.class));
        final List<AModelElement<SettingTable>> src = spy(srcList);
        final AModelElement<SettingTable> m = mock(AModelElement.class);

        // execute
        boolean result = testable.isNextTheSameAsCurrent(src, m, 0);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(src);
        order.verify(src, times(1)).size();
        order.verify(src, times(1)).get(1);
        order.verifyNoMoreInteractions();
    }

    @Test
    public void test_isNextTheSameAsCurrent_notEnoughElements_shouldReturn_FALSE() {
        // prepare
        final List<AModelElement<SettingTable>> srcList = new ArrayList<AModelElement<SettingTable>>();
        final List<AModelElement<SettingTable>> src = spy(srcList);
        @SuppressWarnings("unchecked")
        final AModelElement<SettingTable> m = mock(AModelElement.class);

        // execute
        boolean result = testable.isNextTheSameAsCurrent(src, m, 0);

        // verify
        assertThat(result).isFalse();

        InOrder order = inOrder(src);
        order.verify(src, times(1)).size();
        order.verifyNoMoreInteractions();
    }

    private class LimitedSizeList<T> extends ArrayList<T> {

        private static final long serialVersionUID = 8445111812492585164L;

        private int maxSize = -1;

        public void setMaxSize(final int maxSize) {
            this.maxSize = maxSize;
        }

        public int getMaxSize() {
            return this.maxSize;
        }

        @Override
        public void add(final int index, final T elem) {
            boolean toAdd = false;
            if (getMaxSize() >= 0 && super.size() + 1 <= getMaxSize()) {
                toAdd = true;
            } else if (getMaxSize() < 0) {
                toAdd = true;
            }

            if (toAdd) {
                super.add(index, elem);
            } else {
                throw new IndexOutOfBoundsException("Elements size exceed maximum number of elements.");
            }
        }

        @Override
        public boolean add(final T elem) {
            boolean toAdd = false;
            if (getMaxSize() >= 0 && super.size() + 1 <= getMaxSize()) {
                toAdd = true;
            } else if (getMaxSize() < 0) {
                toAdd = true;
            }

            if (toAdd) {
                return super.add(elem);
            } else {
                throw new IndexOutOfBoundsException("Elements size exceed maximum number of elements.");
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_limitedSizeList_addPos_notEnoughSpace() {
        // prepare
        LimitedSizeList<String> p = new LimitedSizeList<>();
        p.setMaxSize(2);

        // execute
        p.add("c");
        p.add("d");
        p.add(1, "r");

        // verify
        assertThat(p).containsExactly("c", "d");
    }

    @Test
    public void test_limitedSizeList_addPos_enoughSpace() {
        // prepare
        LimitedSizeList<String> p = new LimitedSizeList<>();
        p.setMaxSize(3);

        // execute
        p.add("c");
        p.add("d");
        p.add(1, "r");

        // verify
        assertThat(p).containsExactly("c", "r", "d");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_limitedSizeList_add_notEnoughSpace() {
        // prepare
        LimitedSizeList<String> p = new LimitedSizeList<>();
        p.setMaxSize(1);

        // execute
        p.add("c");
        p.add("d");

        // verify
        assertThat(p).containsExactly("c");
    }

    @Test
    public void test_limitedSizeList_add_enoughSpace() {
        // prepare
        LimitedSizeList<String> p = new LimitedSizeList<>();
        p.setMaxSize(2);

        // execute
        p.add("c");
        p.add("d");

        // verify
        assertThat(p).containsExactly("c", "d");
    }

    private class RobotFileDumperInheritance extends ARobotFileDumper {

        @Override
        public boolean canDumpFile(final File file) {
            // Irrelevant for this test
            return false;
        }

        @Override
        protected Separator getSeparatorDefault() {
            // Irrelevant for this test
            return null;
        }

        @Override
        public void repositionElementsBaseOnList(final List<AModelElement<SettingTable>> src,
                final List<? extends AModelElement<SettingTable>> correctors) {
            super.repositionElementsBaseOnList(src, correctors);
        }

        @Override
        public boolean isNextTheSameAsCurrent(final List<AModelElement<SettingTable>> src,
                final AModelElement<SettingTable> m, int currentIndex) {
            return super.isNextTheSameAsCurrent(src, m, currentIndex);
        }
    }
}
