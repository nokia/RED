/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.PasteCodeHoldersCellsCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

class PasteTasksCellsCommandsCollector extends PasteCodeHoldersCellsCommandsCollector {

    @Override
    protected boolean hasCodeHolders(final RedClipboard clipboard) {
        return clipboard.hasTasks();
    }

    @Override
    protected RobotCodeHoldingElement<?>[] getCodeHolders(final RedClipboard clipboard) {
        return clipboard.getTasks();
    }

    @Override
    protected List<String> getValuesFromCodeHolder(final RobotCodeHoldingElement<?> codeHolder,
            final int clipboardElementColumnIndex) {

        if (clipboardElementColumnIndex == 0) {
            return newArrayList(codeHolder.getName());
        }
        return new ArrayList<>();
    }
}
