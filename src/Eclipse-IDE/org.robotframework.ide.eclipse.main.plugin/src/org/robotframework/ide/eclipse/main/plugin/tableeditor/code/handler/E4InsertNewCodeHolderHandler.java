/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import java.util.Optional;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshHolderCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.viewers.Selections;

public class E4InsertNewCodeHolderHandler {

    protected final void insertNewHolder(final RobotSuiteFile fileModel, final IStructuredSelection selection,
            final RobotEditorCommandsStack stack, final Class<? extends RobotSuiteFileSection> sectionClass) {

        if (selection.size() > 1) {
            throw new IllegalArgumentException(
                    "Given selection should contain at most one element, but has " + selection.size() + " instead");
        }

        final RobotCodeHoldingElement<?> holder = findHolder(selection);
        if (holder == null) {
            final RobotSuiteFileSection section = fileModel.findSection(sectionClass).get();
            stack.execute(new CreateFreshHolderCommand(section));

        } else {
            final RobotSuiteFileSection section = holder.getParent();
            final int index = section.getChildren().indexOf(holder);
            stack.execute(new CreateFreshHolderCommand(section, index));
        }
    }

    private RobotCodeHoldingElement<?> findHolder(final IStructuredSelection selection) {
        final Optional<RobotElement> selectedElem = Selections.getOptionalFirstElement(selection, RobotElement.class);

        if (selectedElem.isPresent() && selectedElem.get() instanceof RobotKeywordCall) {
            return (RobotCodeHoldingElement<?>) selectedElem.get().getParent();

        } else if (selectedElem.isPresent() && selectedElem.get() instanceof RobotCodeHoldingElement<?>) {
            return (RobotCodeHoldingElement<?>) selectedElem.get();

        } else {
            return Selections.getOptionalFirstElement(selection, AddingToken.class)
                    .map(AddingToken::getParent)
                    .map(parent -> (RobotCodeHoldingElement<?>) parent)
                    .orElse(null);
        }
    }
}
