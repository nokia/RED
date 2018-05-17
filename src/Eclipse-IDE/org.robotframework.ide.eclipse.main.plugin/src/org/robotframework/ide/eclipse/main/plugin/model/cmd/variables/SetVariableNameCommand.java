/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static com.google.common.collect.Iterables.filter;

import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Predicate;

public class SetVariableNameCommand extends EditorCommand {

    private final RobotVariable variable;
    private final String newName;
    private String previousName;

    public SetVariableNameCommand(final RobotVariable variable, final String newName) {
        this.variable = variable;
        this.newName = newName == null || newName.isEmpty() ? "${}" : newName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final Optional<RobotToken> modifiedToken = modifyToken();
        if (!modifiedToken.isPresent()) {
            return;
        }

        final boolean typeHasChanged = typeChangeIsRequired();
        if (typeHasChanged) {
            final RobotVariablesSection section = variable.getParent();
            final VariableTable table = section.getLinkedElement();

            final AVariable newHolder = createProjectedVariable(modifiedToken);

            final int index = table.getVariables().indexOf(variable.getLinkedElement());
            table.removeVariable(variable.getLinkedElement());
            table.addVariable(index, newHolder);

            variable.setLinkedElement(newHolder);
        }

        variable.getLinkedElement().getType(); // in order to fix scalar to scalar as list
        variable.getLinkedElement().setName(getNewHolderName());

        if (typeHasChanged) {
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE, variable);
        }
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE, variable);
    }

    private AVariable createProjectedVariable(final Optional<RobotToken> modifiedToken) {
        final List<RobotToken> elementTokens = variable.getLinkedElement().getElementTokens();
        final Iterable<RobotToken> valueTokens = filter(elementTokens,
                containingOneOf(RobotTokenType.VARIABLES_VARIABLE_VALUE));

        final AVariable newHolder;

        switch (getProjectedType()) {
        case SCALAR:
            newHolder = createScalarHolder(modifiedToken, valueTokens);
            break;
        case LIST:
            newHolder = createListHolder(modifiedToken, valueTokens);
            break;
        case DICTIONARY:
            newHolder = createDictHolder(modifiedToken, valueTokens);
            break;
        default:
            newHolder = createUnknownHolder(modifiedToken, valueTokens);
            break;
        }

        final Iterable<RobotToken> commentTokens = filter(elementTokens, containingOneOf(
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE));
        for (final RobotToken commentToken : commentTokens) {
            newHolder.addCommentPart(commentToken);
        }
        return newHolder;
    }

    private ScalarVariable createScalarHolder(final Optional<RobotToken> modifiedToken,
            final Iterable<RobotToken> valueTokens) {
        final ScalarVariable scalarHolder = new ScalarVariable(getNewHolderName(), modifiedToken.get(),
                variable.getLinkedElement().getScope());
        for (final RobotToken valueToken : valueTokens) {
            scalarHolder.addValue(valueToken);
        }
        return scalarHolder;
    }

    private ListVariable createListHolder(final Optional<RobotToken> modifiedToken,
            final Iterable<RobotToken> valueTokens) {
        final ListVariable listHolder = new ListVariable(getNewHolderName(), modifiedToken.get(),
                variable.getLinkedElement().getScope());
        for (final RobotToken valueToken : valueTokens) {
            listHolder.addItem(valueToken);
        }
        return listHolder;
    }

    private DictionaryVariable createDictHolder(final Optional<RobotToken> modifiedToken,
            final Iterable<RobotToken> valueTokens) {
        final DictionaryVariable dictHolder = new DictionaryVariable(getNewHolderName(), modifiedToken.get(),
                variable.getLinkedElement().getScope());
        for (final RobotToken valueToken : valueTokens) {
            final DictionaryKeyValuePair keyValuePair = DictionaryKeyValuePair.createFromRaw(valueToken.getText());
            dictHolder.put(keyValuePair.getRaw(), keyValuePair.getKey(), keyValuePair.getValue());
        }
        return dictHolder;
    }

    private UnknownVariable createUnknownHolder(final Optional<RobotToken> modifiedToken,
            final Iterable<RobotToken> valueTokens) {
        final UnknownVariable unknownHolder = new UnknownVariable(getNewHolderName(), modifiedToken.get(),
                variable.getLinkedElement().getScope());
        for (final RobotToken valueToken : valueTokens) {
            unknownHolder.addItem(valueToken);
        }
        return unknownHolder;
    }

    private static Predicate<RobotToken> containingOneOf(final RobotTokenType... types) {
        return new Predicate<RobotToken>() {
            @Override
            public boolean apply(final RobotToken token) {
                for (final RobotTokenType type : types) {
                    if (token.getTypes().contains(type)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private Optional<RobotToken> modifyToken() {
        final RobotToken declaringToken = variable.getLinkedElement().getDeclaration();
        previousName = declaringToken.getText();
        if (previousName.equals(newName)) {
            return Optional.empty();
        }
        declaringToken.setText(newName);
        declaringToken.setType(getDeclaringTokenType());

        return Optional.of(declaringToken);
    }

    private IRobotTokenType getDeclaringTokenType() {
        if (newName.startsWith(VariableType.SCALAR.getIdentificator() + "{") && newName.endsWith("}")) {
            return RobotTokenType.VARIABLES_SCALAR_DECLARATION;
        } else if (newName.startsWith(VariableType.LIST.getIdentificator() + "{") && newName.endsWith("}")) {
            return RobotTokenType.VARIABLES_LIST_DECLARATION;
        } else if (newName.startsWith(VariableType.DICTIONARY.getIdentificator() + "{") && newName.endsWith("}")) {
            return RobotTokenType.VARIABLES_DICTIONARY_DECLARATION;
        }
        return RobotTokenType.VARIABLES_UNKNOWN_DECLARATION;
    }

    private boolean typeChangeIsRequired() {
        final VariableType projectedType = getProjectedType();
        return variable.getType() != projectedType
                && !(variable.getType() == VariableType.SCALAR_AS_LIST && projectedType == VariableType.SCALAR);
    }

    private VariableType getProjectedType() {
        if (newName.isEmpty()) {
            return VariableType.INVALID;
        }
        final VariableType projectedType = VariableType.getTypeByChar(newName.charAt(0));
        return projectedType == null || !newName.endsWith("}") ? VariableType.INVALID : projectedType;
    }

    private String getNewHolderName() {
        return getProjectedType() == VariableType.INVALID ? newName : newName.substring(2, newName.length() - 1);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetVariableNameCommand(variable, previousName));
    }
}
