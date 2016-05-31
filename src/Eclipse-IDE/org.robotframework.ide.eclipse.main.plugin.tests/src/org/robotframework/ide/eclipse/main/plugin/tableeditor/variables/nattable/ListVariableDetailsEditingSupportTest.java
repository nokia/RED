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
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateCompoundVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveListVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveListVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetListVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

public class ListVariableDetailsEditingSupportTest {

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrownWhenTryingToGetInputOfScalar() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getInput(0, 0);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrownWhenTryingToGetInputOfDictionary() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getInput(0, 3);
    }

    @Test
    public void theInputIsListOfTokens_whenScalarAsListIsGiven() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        final List<RobotToken> input = support.getInput(0, 1);
        assertThat(input).hasSize(3);
        assertThat(input.get(0).getText()).isEqualTo("0");
        assertThat(input.get(1).getText()).isEqualTo("1");
        assertThat(input.get(2).getText()).isEqualTo("2");
    }

    @Test
    public void theInputIsListOfTokens_whenListIsGiven() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        final List<RobotToken> input = support.getInput(0, 2);
        assertThat(input).hasSize(3);
        assertThat(input.get(0).getText()).isEqualTo("1");
        assertThat(input.get(1).getText()).isEqualTo("2");
        assertThat(input.get(2).getText()).isEqualTo("3");
    }

    @Test
    public void theInputIsListOfTokens_whenInvalidIsGiven() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        final List<RobotToken> input = support.getInput(0, 4);
        assertThat(input).hasSize(1);
        assertThat(input.get(0).getText()).isEqualTo("0");
    }

    @Test(expected = IllegalStateException.class)
    public void itIsIllegalToGetDetailElementsWhenInputWasNotTakenFirst() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);

        support.getDetailElements();
    }

    @Test
    public void detailElementsAreReturnedProperly_whenInputWasTakenFirst() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        support.getInput(0, 2);

        final List<RobotToken> input = support.getDetailElements();
        assertThat(input).hasSize(3);
        assertThat(input.get(0).getText()).isEqualTo("1");
        assertThat(input.get(1).getText()).isEqualTo("2");
        assertThat(input.get(2).getText()).isEqualTo("3");
    }

    @Test
    public void newDetailElementAdditionIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        support.getInput(0, 1);

        support.addNewDetailElement("42");

        verify(commandsStack).execute(isA(CreateCompoundVariableValueElementCommand.class));
        assertThat(section.getChildren().get(1).getValue()).isEqualTo("[0, 1, 2, 42]");
    }

    @Test
    public void detailRemovalIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<RobotToken> input = support.getInput(0, 2);

        support.removeDetailElements(newArrayList(input.get(1), input.get(2)));

        verify(commandsStack).execute(isA(RemoveListVariableValueElementsCommand.class));
        assertThat(section.getChildren().get(2).getValue()).isEqualTo("[1]");
    }

    @Test
    public void detailLeftMoveIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<RobotToken> input = support.getInput(0, 2);

        support.moveLeft(newArrayList(input.get(1), input.get(2)));

        verify(commandsStack).execute(isA(MoveListVariableValueElementsCommand.class));
        assertThat(section.getChildren().get(2).getValue()).isEqualTo("[2, 3, 1]");
    }

    @Test
    public void detailRightMoveIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<RobotToken> input = support.getInput(0, 2);

        support.moveRight(newArrayList(input.get(0), input.get(1)));

        verify(commandsStack).execute(isA(MoveListVariableValueElementsCommand.class));
        assertThat(section.getChildren().get(2).getValue()).isEqualTo("[3, 1, 2]");
    }

    @Test
    public void detailValueChangeIsRequestedUsingCommandsStack() {
        final RobotVariablesSection section = createVariables();

        final RobotEditorCommandsStack commandsStack = spy(new RobotEditorCommandsStack());
        final VariablesDataProvider dataProvider = new VariablesDataProvider(commandsStack, section);
        final ListVariableDetailsEditingSupport support = new ListVariableDetailsEditingSupport(mock(TableTheme.class),
                dataProvider, commandsStack);
        final List<RobotToken> input = support.getInput(0, 4);

        support.setNewValue(input.get(0), "42");

        verify(commandsStack).execute(isA(SetListVariableValueElementCommand.class));
        assertThat(section.getChildren().get(4).getValue()).isEqualTo("[42]");
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
