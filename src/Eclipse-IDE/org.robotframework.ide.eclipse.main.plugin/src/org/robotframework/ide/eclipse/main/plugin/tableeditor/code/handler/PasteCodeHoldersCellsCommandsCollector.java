/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;

public abstract class PasteCodeHoldersCellsCommandsCollector extends PasteRobotElementCellsCommandsCollector {

    @Override
    protected final boolean hasRobotElementsInClipboard(final RedClipboard clipboard) {
        return clipboard.hasKeywordCalls() || hasCodeHolders(clipboard);
    }

    protected abstract boolean hasCodeHolders(final RedClipboard clipboard);

    @Override
    protected final RobotElement[] getRobotElementsFromClipboard(final RedClipboard clipboard) {
        final RobotKeywordCall[] keywordCalls = clipboard.getKeywordCalls();
        final RobotCodeHoldingElement<?>[] cases = getCodeHolders(clipboard);
        final List<RobotElement> elements = new ArrayList<>();
        if (keywordCalls != null) {
            elements.addAll(Arrays.asList(keywordCalls));
        }
        if (cases != null) {
            elements.addAll(Arrays.asList(cases));
        }
        return elements.toArray(new RobotElement[0]);
    }

    protected abstract RobotCodeHoldingElement<?>[] getCodeHolders(RedClipboard clipboard);

    @Override
    protected final List<String> findValuesToPaste(final RobotElement elementFromClipboard,
            final int clipboardElementColumnIndex, final int tableColumnsCount) {
        if (elementFromClipboard instanceof RobotKeywordCall) {
            return getValuesFromKeywordCall((RobotKeywordCall) elementFromClipboard, clipboardElementColumnIndex);

        } else if (elementFromClipboard instanceof RobotCodeHoldingElement<?>) {
            return getValuesFromCodeHolder((RobotCodeHoldingElement<?>) elementFromClipboard,
                    clipboardElementColumnIndex);
        }
        return new ArrayList<>();
    }

    @Override
    protected final List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();

        final String valueToPaste = valuesToPaste.isEmpty() ? "" : valuesToPaste.get(0);
        new CodeTableValuesChangingCommandsCollector()
                .collectForChange(selectedElement, valueToPaste, selectedElementColumnIndex, tableColumnsCount)
                .ifPresent(pasteCommands::add);

        return pasteCommands;
    }

    private List<String> getValuesFromKeywordCall(final RobotKeywordCall keywordCall,
            final int clipboardElementColumnIndex) {

        if (clipboardElementColumnIndex > 0 && keywordCall instanceof RobotDefinitionSetting
                && ((RobotDefinitionSetting) keywordCall).isDocumentation()) {
            if (clipboardElementColumnIndex == 1) {
                return newArrayList(getDocumentationText(keywordCall));
            } else {
                return new ArrayList<>();
            }
        }

        final List<RobotToken> execRowView = ExecutablesRowHolderCommentService.execRowView(keywordCall);
        if (clipboardElementColumnIndex < execRowView.size()) {
            return newArrayList(execRowView.get(clipboardElementColumnIndex).getText());
        }
        return new ArrayList<>();
    }

    private String getDocumentationText(final RobotKeywordCall keywordCall) {
        return DocumentationServiceHandler.toEditConsolidated(getHolder(keywordCall.getLinkedElement()));
    }

    private IDocumentationHolder getHolder(final AModelElement<?> element) {
        if (element instanceof IDocumentationHolder) {
            return (IDocumentationHolder) element;
        } else {
            final LocalSetting<?> setting = (LocalSetting<?>) element;
            return setting.adaptTo(IDocumentationHolder.class);
        }
    }

    protected abstract List<String> getValuesFromCodeHolder(final RobotCodeHoldingElement<?> codeHolder,
            final int tableColumnsCount);
}
