/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.PasteCodeHoldersCellsCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

/**
 * @author mmarzec
 */
public class PasteKeywordsCellsCommandsCollector extends PasteCodeHoldersCellsCommandsCollector {

    @Override
    protected boolean hasCodeHolders(final RedClipboard clipboard) {
        return clipboard.hasKeywordDefinitions();
    }

    @Override
    protected RobotCodeHoldingElement<?>[] getCodeHolders(final RedClipboard clipboard) {
        return clipboard.getKeywordDefinitions();
    }

    @Override
    protected List<String> getValuesFromCodeHolder(final RobotCodeHoldingElement<?> codeHolder,
            final int clipboardElementColumnIndex, final int tableColumnsCount) {

        if (clipboardElementColumnIndex == 0) {
            return newArrayList(codeHolder.getName());

        } else if (clipboardElementColumnIndex > 0 && clipboardElementColumnIndex < tableColumnsCount - 1) {
            final RobotDefinitionSetting argumentsSetting = ((RobotKeywordDefinition) codeHolder).getArgumentsSetting();
            final int argIndex = clipboardElementColumnIndex - 1;
            if (argumentsSetting != null && argIndex < argumentsSetting.getArguments().size()) {
                return newArrayList(argumentsSetting.getArguments().get(argIndex));
            }
        }
        return new ArrayList<>();
    }
}
