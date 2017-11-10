/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class SetVariableNameCommandTest {

    @Test
    public void nameIsProperlyChangedFromScalarToScalar() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(0);
        
        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "${other_scalar}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(variable.getName()).isEqualTo("other_scalar");
        assertThat(variable.getValue()).isEqualTo("0");
        assertThat(variable.getComment()).isEqualTo("# comment 1");
    }

    @Test
    public void nameIsProperlyChangedFromScalarToList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(0);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "@{other_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.LIST);
        assertThat(variable.getName()).isEqualTo("other_list");
        assertThat(variable.getValue()).isEqualTo("[0]");
        assertThat(variable.getComment()).isEqualTo("# comment 1");
    }

    @Test
    public void nameIsProperlyChangedFromScalarToDictionary() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(0);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "&{other_dict}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.DICTIONARY);
        assertThat(variable.getName()).isEqualTo("other_dict");
        assertThat(variable.getValue()).isEqualTo("{0 = }");
        assertThat(variable.getComment()).isEqualTo("# comment 1");
    }

    @Test
    public void nameIsProperlyChangedFromScalarToInvalid() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(0);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "{other_invalid}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.INVALID);
        assertThat(variable.getName()).isEqualTo("{other_invalid}");
        assertThat(variable.getValue()).isEqualTo("[0]");
        assertThat(variable.getComment()).isEqualTo("# comment 1");
    }

    @Test
    public void nameIsProperlyChangedFromScalarAsListToScalarAsList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(1);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "${other_scalar_as_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.SCALAR_AS_LIST);
        assertThat(variable.getName()).isEqualTo("other_scalar_as_list");
        assertThat(variable.getValue()).isEqualTo("[0, 1, 2]");
        assertThat(variable.getComment()).isEqualTo("# comment 2");
    }

    @Test
    public void nameIsProperlyChangedFromScalarAsListToList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(1);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "@{other_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.LIST);
        assertThat(variable.getName()).isEqualTo("other_list");
        assertThat(variable.getValue()).isEqualTo("[0, 1, 2]");
        assertThat(variable.getComment()).isEqualTo("# comment 2");
    }

    @Test
    public void nameIsProperlyChangedFromScalarAsListToDictionary() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(1);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "&{other_dict}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.DICTIONARY);
        assertThat(variable.getName()).isEqualTo("other_dict");
        assertThat(variable.getValue()).isEqualTo("{0 = , 1 = , 2 = }");
        assertThat(variable.getComment()).isEqualTo("# comment 2");
    }

    @Test
    public void nameIsProperlyChangedFromScalarAsListToInvalid() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(1);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "{other_invalid}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.INVALID);
        assertThat(variable.getName()).isEqualTo("{other_invalid}");
        assertThat(variable.getValue()).isEqualTo("[0, 1, 2]");
        assertThat(variable.getComment()).isEqualTo("# comment 2");
    }

    @Test
    public void nameIsProperlyChangedFromListToScalarAsList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(2);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "${other_scalar_as_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.SCALAR_AS_LIST);
        assertThat(variable.getName()).isEqualTo("other_scalar_as_list");
        assertThat(variable.getValue()).isEqualTo("[1, 2, 3]");
        assertThat(variable.getComment()).isEqualTo("# comment 3");
    }

    @Test
    public void nameIsProperlyChangedFromListToList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(2);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "@{other_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.LIST);
        assertThat(variable.getName()).isEqualTo("other_list");
        assertThat(variable.getValue()).isEqualTo("[1, 2, 3]");
        assertThat(variable.getComment()).isEqualTo("# comment 3");
    }

    @Test
    public void nameIsProperlyChangedFromListToDictionary() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(2);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "&{other_dict}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.DICTIONARY);
        assertThat(variable.getName()).isEqualTo("other_dict");
        assertThat(variable.getValue()).isEqualTo("{1 = , 2 = , 3 = }");
        assertThat(variable.getComment()).isEqualTo("# comment 3");
    }

    @Test
    public void nameIsProperlyChangedFromListToInvalid() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(2);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "{other_invalid}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.INVALID);
        assertThat(variable.getName()).isEqualTo("{other_invalid}");
        assertThat(variable.getValue()).isEqualTo("[1, 2, 3]");
        assertThat(variable.getComment()).isEqualTo("# comment 3");
    }

    @Test
    public void nameIsProperlyChangedFromDictionaryToScalarAsList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(3);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "${other_scalar_as_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.SCALAR_AS_LIST);
        assertThat(variable.getName()).isEqualTo("other_scalar_as_list");
        assertThat(variable.getValue()).isEqualTo("[a=1, b=2, c=3]");
        assertThat(variable.getComment()).isEqualTo("# comment 4");
    }

    @Test
    public void nameIsProperlyChangedFromDictionaryToList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(3);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "@{other_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.LIST);
        assertThat(variable.getName()).isEqualTo("other_list");
        assertThat(variable.getValue()).isEqualTo("[a=1, b=2, c=3]");
        assertThat(variable.getComment()).isEqualTo("# comment 4");
    }

    @Test
    public void nameIsProperlyChangedFromDictionaryToDictionary() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(3);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "&{other_dict}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.DICTIONARY);
        assertThat(variable.getName()).isEqualTo("other_dict");
        assertThat(variable.getValue()).isEqualTo("{a = 1, b = 2, c = 3}");
        assertThat(variable.getComment()).isEqualTo("# comment 4");
    }

    @Test
    public void nameIsProperlyChangedFromDictionaryToInvalid() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(3);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "{other_invalid}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.INVALID);
        assertThat(variable.getName()).isEqualTo("{other_invalid}");
        assertThat(variable.getValue()).isEqualTo("[a=1, b=2, c=3]");
        assertThat(variable.getComment()).isEqualTo("# comment 4");
    }

    @Test
    public void nameIsProperlyChangedFromInvalidToScalar() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(4);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "${other_scalar_as_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.SCALAR);
        assertThat(variable.getName()).isEqualTo("other_scalar_as_list");
        assertThat(variable.getValue()).isEqualTo("0");
        assertThat(variable.getComment()).isEqualTo("# comment 5");
    }

    @Test
    public void nameIsProperlyChangedFromInvalidToList() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(4);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "@{other_list}"));
        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.LIST);
        assertThat(variable.getName()).isEqualTo("other_list");
        assertThat(variable.getValue()).isEqualTo("[0]");
        assertThat(variable.getComment()).isEqualTo("# comment 5");
    }

    @Test
    public void nameIsProperlyChangedFromInvalidToDictionary() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(4);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "&{other_dict}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.DICTIONARY);
        assertThat(variable.getName()).isEqualTo("other_dict");
        assertThat(variable.getValue()).isEqualTo("{0 = }");
        assertThat(variable.getComment()).isEqualTo("# comment 5");
    }

    @Test
    public void nameIsProperlyChangedFromInvalidToInvalid() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariable variable = createVariables().get(4);

        final SetVariableNameCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetVariableNameCommand(variable, "{other_invalid}"));

        command.execute();

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
        verifyNoMoreInteractions(eventBroker);

        assertThat(variable.getType()).isEqualTo(VariableType.INVALID);
        assertThat(variable.getName()).isEqualTo("{other_invalid}");
        assertThat(variable.getValue()).isEqualTo("[0]");
        assertThat(variable.getComment()).isEqualTo("# comment 5");
    }

    @Test
    public void nothingHappensWhenTheNameIsTheSameAsAlreadyDefined() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        for (final RobotVariable variable : createVariables()) {
            // the command changes name to already defined one
            final SetVariableNameCommand command = ContextInjector.prepareContext()
                    .inWhich(eventBroker)
                    .isInjectedInto(new SetVariableNameCommand(variable, getActualName(variable)));
            command.execute();
        }
        verifyZeroInteractions(eventBroker);
    }

    private String getActualName(final RobotVariable variable) {
        return variable.getType() == VariableType.INVALID ? variable.getName()
                : variable.getPrefix() + variable.getName() + variable.getSuffix();
    }

    private static List<RobotVariable> createVariables() {
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
