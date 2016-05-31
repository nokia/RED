/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateCompoundVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveDictVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveDictVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetDictVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

public class DictVariableDetailsEditingSupportTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrownWhenTryingToGetInputOfScalar() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getInput(0, 0);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrownWhenTryingToGetInputOfScalarAsList() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getInput(0, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrownWhenTryingToGetInputOfList() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getInput(0, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrownWhenTryingToGetInputOfInvalid() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getInput(0, 4);
    }

    @Test
    public void theInputIsListKeyValuePairs_whenDictionaryIsGiven() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        
        final List<DictionaryKeyValuePair> input = support.getInput(0, 3);
        assertThat(input).hasSize(3);
        assertThat(input.get(0).getRaw().getText()).isEqualTo("a=1");
        assertThat(input.get(1).getRaw().getText()).isEqualTo("b=2");
        assertThat(input.get(2).getRaw().getText()).isEqualTo("c=3");
    }

    @Test(expected = IllegalStateException.class)
    public void itIsIllegalToGetDetailElementsWhenInputWasNotTakenFirst() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getDetailElements();
    }

    @Test
    public void detailElementsAreReturnedProperly_whenInputWasTakenFirst() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        support.getInput(0, 3);
        
        final List<DictionaryKeyValuePair> input = support.getDetailElements();
        assertThat(input).hasSize(3);
        assertThat(input.get(0).getRaw().getText()).isEqualTo("a=1");
        assertThat(input.get(1).getRaw().getText()).isEqualTo("b=2");
        assertThat(input.get(2).getRaw().getText()).isEqualTo("c=3");
    }

    @Test
    public void newDetailElementAdditionIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        support.getInput(0, 3);

        support.addNewDetailElement("key=value");

        verify(commandsStack).execute(isA(CreateCompoundVariableValueElementCommand.class));
        assertThat(section.getChildren().get(3).getValue()).isEqualTo("{a -> 1, b -> 2, c -> 3, key -> value}");
    }

    @Test
    public void detailRemovalIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<DictionaryKeyValuePair> input = support.getInput(0, 3);

        support.removeDetailElements(newArrayList(input.get(0), input.get(2)));

        verify(commandsStack).execute(isA(RemoveDictVariableValueElementsCommand.class));
        assertThat(section.getChildren().get(3).getValue()).isEqualTo("{b -> 2}");
    }

    @Test
    public void detailLeftMoveIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<DictionaryKeyValuePair> input = support.getInput(0, 3);

        support.moveLeft(newArrayList(input.get(0), input.get(2)));

        verify(commandsStack).execute(isA(MoveDictVariableValueElementsCommand.class));
        assertThat(section.getChildren().get(3).getValue()).isEqualTo("{a -> 1, c -> 3, b -> 2}");
    }

    @Test
    public void detailRightMoveIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<DictionaryKeyValuePair> input = support.getInput(0, 3);

        support.moveRight(newArrayList(input.get(0), input.get(2)));

        verify(commandsStack).execute(isA(MoveDictVariableValueElementsCommand.class));
        assertThat(section.getChildren().get(3).getValue()).isEqualTo("{b -> 2, a -> 1, c -> 3}");
    }

    @Test
    public void detailValueChangeIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final DictVariableDetailsEditingSupport support = new DictVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<DictionaryKeyValuePair> input = support.getInput(0, 3);

        support.setNewValue(input.get(1), "x=42");

        verify(commandsStack).execute(isA(SetDictVariableValueElementCommand.class));
        assertThat(section.getChildren().get(3).getValue()).isEqualTo("{a -> 1, x -> 42, c -> 3}");
    }

    private static RobotVariablesSection createVariables() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0  # comment 1")
                .appendLine("${scalar_as_list}  0  1  2  # comment 2")
                .appendLine("@{list}  1  2  3  # comment 3")
                .appendLine("&{dictionary}  a=1  b=2  c=3  # comment 4")
                .appendLine("invalid}  0  # comment 5")
                .build();
        return model.findSection(RobotVariablesSection.class).get();
    }
}
