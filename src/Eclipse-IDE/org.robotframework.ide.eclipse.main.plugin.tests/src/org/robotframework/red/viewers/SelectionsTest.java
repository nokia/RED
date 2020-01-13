/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.jupiter.api.Test;

public class SelectionsTest {

    @Test
    public void emptyListIsReturned_whenGettingObjectsFromEmptySelection() {
        final IStructuredSelection selection = StructuredSelection.EMPTY;

        assertThat(Selections.getElements(selection, Object.class)).isEmpty();
    }

    @Test
    public void emptyListIsReturned_whenSelectionHasObjectsOfNonMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(new Object(), new String(""), new Object()));

        assertThat(Selections.getElements(selection, Integer.class)).isEmpty();
    }

    @Test
    public void onlyObjectsOfMatchingTypesAreReturned_whenSelectionContainsMatchingAndNonMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(new Object(), new String(""), Integer.valueOf(5)));

        assertThat(Selections.getElements(selection, Integer.class)).containsExactly(Integer.valueOf(5));
    }

    @Test
    public void allObjectsAreReturned_whenSelectionContainsOnlyMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(Short.valueOf((short) 1), Integer.valueOf(7), Long.valueOf(42)));

        assertThat(Selections.getElements(selection, Number.class)).containsExactly(Short.valueOf((short) 1),
                Integer.valueOf(7), Long.valueOf(42));
    }

    @Test
    public void exceptionIsThrown_whenTryingToGetSingleElementFromEmptySelection() {
        final IStructuredSelection selection = StructuredSelection.EMPTY;

        assertThatIllegalArgumentException().isThrownBy(() -> Selections.getSingleElement(selection, Object.class))
                .withMessage("Given selection should contain only one element of class " + Object.class.getName()
                        + ", but have 0 elements instead")
                .withNoCause();
    }

    @Test
    public void exceptionIsThrown_whenTryingToGetSingleElementFromMultiselection() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(Integer.valueOf(7), Long.valueOf(42)));

        assertThatIllegalArgumentException().isThrownBy(() -> Selections.getSingleElement(selection, Object.class))
                .withMessage("Given selection should contain only one element of class " + Object.class.getName()
                        + ", but have 2 elements instead")
                .withNoCause();
    }

    @Test
    public void selectedObjectIsReturned_whenTryingToGetSingleElementFromSingleSelection() {
        final IStructuredSelection selection = new StructuredSelection(Integer.valueOf(7));

        assertThat(Selections.getSingleElement(selection, Integer.class)).isEqualTo(Integer.valueOf(7));
    }

    @Test
    public void selectedObjectIsReturned_whenTryingToGetSingleElementFromMultiselectionButWithOnlyOneMatchingObject() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(Integer.valueOf(7), new String(""), new Object()));

        assertThat(Selections.getSingleElement(selection, Integer.class)).isEqualTo(Integer.valueOf(7));
    }

    @Test
    public void elementIsAbsent_whenTryingToGetOptionalFirstFromEmptySelection() {
        final IStructuredSelection selection = StructuredSelection.EMPTY;

        assertThat(Selections.getOptionalFirstElement(selection, Object.class)).isNotPresent();
    }

    @Test
    public void elementIsAbsent_whenTryingToGetOptionalFirstFromSelectionWithNonMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(Integer.valueOf(7), Long.valueOf(42)));

        assertThat(Selections.getOptionalFirstElement(selection, String.class)).isNotPresent();
    }

    @Test
    public void firstMatchingElementIsReturned_whenTryingToGetOptionalFirstFromSelectionWithMatchingTypes() {
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(Integer.valueOf(7), new String("1"), Long.valueOf(42), new String("2")));

        assertThat(Selections.getOptionalFirstElement(selection, String.class)).isPresent().hasValue("1");
    }
}
