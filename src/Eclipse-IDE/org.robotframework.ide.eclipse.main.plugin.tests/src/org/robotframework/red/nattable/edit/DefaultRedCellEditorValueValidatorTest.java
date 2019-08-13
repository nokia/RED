/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.Test;
import org.robotframework.red.nattable.edit.CellEditorValueValidator.CellEditorValueValidationException;

public class DefaultRedCellEditorValueValidatorTest {

    @Test
    public void doNothing_whenValueIsNull() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate(null, 0);

        verifyZeroInteractions(dataProvider);
    }

    @Test
    public void noErrorIsThrown_forCorrectValueInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("correct value", 0);
    }

    @Test
    public void noErrorIsThrown_forValueWithPipeSeparatorInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("value | separated", 0);
    }

    @Test(expected = CellEditorValueValidationException.class)
    public void errorIsThrown_forValueWithSeparatorInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("value  separated", 0);
    }

    @Test(expected = CellEditorValueValidationException.class)
    public void errorIsThrown_forValueStartingWithSpaceInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate(" value", 0);
    }

    @Test(expected = CellEditorValueValidationException.class)
    public void errorIsThrown_forValueEndingWithSpaceInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("value ", 0);
    }

    @Test
    public void noErrorIsThrown_forValueEndingWithEscapedSpaceInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("value\\ ", 0);
    }
}
