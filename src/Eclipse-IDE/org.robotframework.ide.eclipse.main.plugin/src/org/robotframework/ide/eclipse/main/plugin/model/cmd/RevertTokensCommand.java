/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.rf.ide.core.testdata.model.table.CommonStep;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

class RevertTokensCommand extends EditorCommand {

    @SuppressWarnings("unchecked")
    static <T> T clone(final T element) {
        return (T) deserialize(serialize(element));
    }

    private static <T> byte[] serialize(final T element) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (final ObjectOutputStream objOutput = new ObjectOutputStream(output)) {
            objOutput.writeObject(element);
            return output.toByteArray();
        } catch (final IOException e) {
            return null;
        }
    }

    private static Object deserialize(final byte[] bytes) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try (final ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            return null;
        }
    }

    private final RobotKeywordCall call;

    private final CommonStep<?> sourceElement;

    private CommonStep<?> oldClone;

    RevertTokensCommand(final RobotKeywordCall call, final CommonStep<?> sourceElement) {
        this.call = call;
        this.sourceElement = sourceElement;
    }

    @Override
    public void execute() {
        final CommonStep<?> linkedElement = (CommonStep<?>) call.getLinkedElement();
        oldClone = clone(linkedElement);

        linkedElement.rewriteFrom(sourceElement);
        call.resetStored();

        RedEventBroker.using(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new RevertTokensCommand(call, oldClone));
    }
}