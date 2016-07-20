/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordCallCommand extends EditorCommand {

    private final RobotCodeHoldingElement parent;

    private final int modelTableIndex;

    private final int codeHoldingElementIndex;

    private final String keywordCallName;

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent) {
        this(parent, "", -1, -1);
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement parent, final String keywordCallName,
            final int modelTableIndex, final int codeHoldingElementIndex) {
        this.parent = parent;
        this.keywordCallName = keywordCallName;
        this.modelTableIndex = modelTableIndex;
        this.codeHoldingElementIndex = codeHoldingElementIndex;
    }

    @Override
    public void execute() throws CommandExecutionException {

        parent.createKeywordCall(keywordCallName, modelTableIndex, codeHoldingElementIndex);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }
}
