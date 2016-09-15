/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;

import com.google.common.base.Optional;

public class SelectionsTest {

    @Test
    public void emptyListIsReturned_whenGettingObjectsFromEmptySelection() {
        final IStructuredSelection selection = StructuredSelection.EMPTY;
        final List<Object> elements = Selections.getElements(selection, Object.class);

        assertThat(elements).isEmpty();
    }

    @Test
    public void emptyListIsReturned_whenSelectionHasObjectsOfNonMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(new Object(), new String(""), new Object()));
        final List<Integer> elements = Selections.getElements(selection, Integer.class);

        assertThat(elements).isEmpty();
    }

    @Test
    public void onlyObjectsOfMatchingTypesAreReturned_whenSelectionContainsMatchingAndNonMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(new Object(), new String(""), new Integer(5)));
        final List<Integer> elements = Selections.getElements(selection, Integer.class);

        assertThat(elements).contains(Integer.valueOf(5));
    }

    @Test
    public void allObjectsAreReturned_whenSelectionContainsOnlyMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(new Short((short) 1), new Integer(7), new Long(42)));
        final List<Number> elements = Selections.getElements(selection, Number.class);

        assertThat(elements).contains(Short.valueOf((short) 1));
        assertThat(elements).contains(Integer.valueOf(7));
        assertThat(elements).contains(Long.valueOf(42));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenTryingToGetSingleElementFromEmptySelection() {
        final IStructuredSelection selection = StructuredSelection.EMPTY;

        Selections.getSingleElement(selection, Object.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenTryingToGetSingleElementFromMultiselection() {
        final IStructuredSelection selection = new StructuredSelection(Arrays.asList(new Integer(7), new Long(42)));

        Selections.getSingleElement(selection, Object.class);
    }

    @Test
    public void selectedObjectIsReturned_whenTryingToGetSingleElementFromSingleSelection() {
        final IStructuredSelection selection = new StructuredSelection(new Integer(7));

        final Integer element = Selections.getSingleElement(selection, Integer.class);
        assertThat(element).isEqualTo(Integer.valueOf(7));
    }

    @Test
    public void selectedObjectIsReturned_whenTryingToGetSingleElementFromMultiselectioButWithOnlyOneMatchingObject() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(new Integer(7), new String(""), new Object()));

        final Integer element = Selections.getSingleElement(selection, Integer.class);
        assertThat(element).isEqualTo(Integer.valueOf(7));
    }

    @Test
    public void elementIsAbsent_whenTryingToGetOptionalFirstFromEmptySelection() {
        final IStructuredSelection selection = StructuredSelection.EMPTY;

        final Optional<Object> optionalFirst = Selections.getOptionalFirstElement(selection, Object.class);
        assertThat(optionalFirst.isPresent()).isFalse();
    }

    @Test
    public void elementIsAbsent_whenTryingToGetOptionalFirstFromSelectionWithNonMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(Arrays.asList(new Integer(7), new Long(42)));
        
        final Optional<String> optionalFirst = Selections.getOptionalFirstElement(selection, String.class);
        assertThat(optionalFirst.isPresent()).isFalse();
    }

    @Test
    public void firstMatchingElementIsReturned_whenTryingToGetOptionalFirstFromSelectionWithMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(new Integer(7), new String("1"), new Long(42), new String("2")));

        final Optional<String> optionalFirst = Selections.getOptionalFirstElement(selection, String.class);
        assertThat(optionalFirst.isPresent()).isTrue();
        assertThat(optionalFirst.get()).isEqualTo("1");
    }
}
