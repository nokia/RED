/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MatchesGroupingElementTest {

    @Test
    public void hashcodeAndEqualsTests() {
        final Object elem1 = new Object();
        final Object elem2 = "value";
        final Object elem3 = Integer.valueOf(42);

        final MatchesGroupingElement m1 = new MatchesGroupingElement(elem1, elem2, elem3);
        final MatchesGroupingElement m2 = new MatchesGroupingElement(elem1, elem2, elem3);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m1.equals(m2)).isTrue();

        final MatchesGroupingElement stableElement = new MatchesGroupingElement(elem1, elem2, elem3);
        assertThat(stableElement.hashCode()).isEqualTo(stableElement.hashCode());
        assertThat(stableElement.equals(stableElement)).isTrue();
    }

    @Test
    public void getGroupingElementTests() {
        final Object elem1 = new Object();
        final Object elem2 = "value";
        final Object elem3 = Integer.valueOf(42);

        final MatchesGroupingElement groupingElem = new MatchesGroupingElement(elem1, elem2, elem3);

        assertThat(groupingElem.getGroupingObjectOf(Object.class).get()).isSameAs(elem1);
        assertThat(groupingElem.getGroupingObjectOf(String.class).get()).isSameAs(elem2);
        assertThat(groupingElem.getGroupingObjectOf(Integer.class).get()).isSameAs(elem3);
        assertThat(groupingElem.getGroupingObjectOf(Number.class).get()).isSameAs(elem3);
        assertThat(groupingElem.getGroupingObjectOf(Long.class).isPresent()).isFalse();
    }

}
