/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetScalarValueCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class VariableColumnsPropertyAccessorTest {

    @Test
    public void thereAre3ColumnsSupported() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));

        assertThat(accessor.getColumnCount()).isEqualTo(3);
    }

    @Test
    public void nameIsProperlyReturnedForDifferentVariables_whenGivenColumnIsTheFirstOne() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));
        
        final List<RobotVariable> variables = createVariablesForTest();
        
        assertThat(accessor.getDataValue(variables.get(0), 0)).isEqualTo("${scalar}");
        assertThat(accessor.getDataValue(variables.get(1), 0)).isEqualTo("${scalar_as_list}");
        assertThat(accessor.getDataValue(variables.get(2), 0)).isEqualTo("@{list}");
        assertThat(accessor.getDataValue(variables.get(3), 0)).isEqualTo("&{dictionary}");
        assertThat(accessor.getDataValue(variables.get(4), 0)).isEqualTo("invalid}");
    }

    @Test
    public void valueIsProperlyReturnedForDifferentVariables_whenGivenColumnIsSecondOne() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));

        final List<RobotVariable> variables = createVariablesForTest();

        assertThat(accessor.getDataValue(variables.get(0), 1)).isEqualTo("0");
        assertThat(accessor.getDataValue(variables.get(1), 1)).isEqualTo("[0, 1, 2]");
        assertThat(accessor.getDataValue(variables.get(2), 1)).isEqualTo("[1, 2, 3]");
        assertThat(accessor.getDataValue(variables.get(3), 1)).isEqualTo("{a -> 1, b -> 2, c -> 3}");
        assertThat(accessor.getDataValue(variables.get(4), 1)).isEqualTo("[0]");
    }

    @Test
    public void commentIsProperlyReturnedForDifferentVariables_whenGivenColumnIsThirdOne() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));

        final List<RobotVariable> variables = createVariablesForTest();

        assertThat(accessor.getDataValue(variables.get(0), 2)).isEqualTo("# comment 1");
        assertThat(accessor.getDataValue(variables.get(1), 2)).isEqualTo("# comment 2");
        assertThat(accessor.getDataValue(variables.get(2), 2)).isEqualTo("# comment 3");
        assertThat(accessor.getDataValue(variables.get(3), 2)).isEqualTo("# comment 4");
        assertThat(accessor.getDataValue(variables.get(4), 2)).isEqualTo("# comment 5");
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToGetValueForNonExisitingColumn_1() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));
        accessor.getDataValue(mock(RobotVariable.class), 3);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToGetValueForNonExisitingColumn_2() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));
        accessor.getDataValue(mock(RobotVariable.class), -1);
    }

    @Test
    public void validPropertiesAreReturnedForDifferentColumns() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));

        assertThat(accessor.getColumnProperty(-1)).isNull();
        assertThat(accessor.getColumnProperty(0)).isEqualTo("name");
        assertThat(accessor.getColumnProperty(1)).isEqualTo("value");
        assertThat(accessor.getColumnProperty(2)).isEqualTo("comment");
        assertThat(accessor.getColumnProperty(3)).isNull();
        assertThat(accessor.getColumnProperty(10)).isNull();
    }

    @Test
    public void validIndexesAreReturnedForDifferentColumnsProperties() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));

        assertThat(accessor.getColumnIndex("name")).isEqualTo(0);
        assertThat(accessor.getColumnIndex("value")).isEqualTo(1);
        assertThat(accessor.getColumnIndex("comment")).isEqualTo(2);
    }

    @Test
    public void valueChangeOfScalarIsRequestedProperly() {
        final RobotEditorCommandsStack stack = mock(RobotEditorCommandsStack.class);
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(stack);

        final List<RobotVariable> variables = createVariablesForTest();

        accessor.setDataValue(variables.get(0), 1, "1729");

        verify(stack).execute(isA(SetScalarValueCommand.class));
    }

    @Test
    public void valueChangeOfNonScalarVariableIsNotRequested() {
        // this is done separately by details cell editor
        final RobotEditorCommandsStack stack = mock(RobotEditorCommandsStack.class);
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(stack);

        final List<RobotVariable> variables = createVariablesForTest();

        accessor.setDataValue(variables.get(1), 1, "1729");
        accessor.setDataValue(variables.get(2), 1, "1729");
        accessor.setDataValue(variables.get(3), 1, "1729");
        accessor.setDataValue(variables.get(4), 1, "1729");

        verify(stack, never()).execute(Matchers.<EditorCommand> any());
    }

    @Test
    public void nameChangeOfVariablesIsRequestedProperly() {
        final RobotEditorCommandsStack stack = mock(RobotEditorCommandsStack.class);
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(stack);

        final List<RobotVariable> variables = createVariablesForTest();

        for (final RobotVariable variable : variables) {
            accessor.setDataValue(variable, 0, "new_name");
        }

        verify(stack, times(5)).execute(isA(SetVariableNameCommand.class));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToSetValueInNonExisitingColumn_1() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));
        accessor.setDataValue(mock(RobotVariable.class), 3, "value");
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenTryingToSetValueInNonExisitingColumn_2() {
        final VariableColumnsPropertyAccessor accessor = new VariableColumnsPropertyAccessor(
                mock(RobotEditorCommandsStack.class));
        accessor.setDataValue(mock(RobotVariable.class), -1, "value");
    }

    private static List<RobotVariable> createVariablesForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0  # comment 1")
                .appendLine("${scalar_as_list}  0  1  2  # comment 2")
                .appendLine("@{list}  1  2  3  # comment 3")
                .appendLine("&{dictionary}  a=1  b=2  c=3  # comment 4")
                .appendLine("invalid}  0  # comment 5")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren();
    }
}
