/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;


public class RobotFormEditorActionBarContributor extends MultiPageEditorActionBarContributor {

    public static final String DELIMITERS_INFO_ID = "ChangeDelimiters";
    private final Map<StatusFieldDef, StatusLineContributionItem> statusFields = new LinkedHashMap<>();

    public RobotFormEditorActionBarContributor() {
        final StatusFieldDef linesDef = new StatusFieldDef(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION,
                ITextEditorActionConstants.GOTO_LINE);
        final StatusLineContributionItem positionItem = new StatusLineContributionItem(linesDef.category);
        positionItem.setText("1:1");
        statusFields.put(linesDef, positionItem);

        final StatusFieldDef delimiterDef = new StatusFieldDef(DELIMITERS_INFO_ID, "ConvertDelimiters");
        final StatusLineContributionItem delimiterItem = new StatusLineContributionItem(delimiterDef.category);
        delimiterItem.setText("LF");
        statusFields.put(delimiterDef, delimiterItem);
    }

    @Override
    public void init(final IActionBars bars) {
        super.init(bars);

    }

    @Override
    public void setActivePage(final IEditorPart activeEditor) {
        updateStatusLineContributions(activeEditor);

        if (activeEditor instanceof ITextEditor) {
            final ITextEditor editor = (ITextEditor) activeEditor;
            addTextActionIfNeeded(editor, ITextEditorActionConstants.BLOCK_SELECTION_MODE,
                    RedImages.getBlockSelectionModeImage());
            addTextActionIfNeeded(editor, ITextEditorActionConstants.SHOW_WHITESPACE_CHARACTERS,
                    RedImages.getShowWhitespaceCharImage());
        } else {
            getActionBars().getToolBarManager().remove(ITextEditorActionConstants.BLOCK_SELECTION_MODE);
            getActionBars().getToolBarManager().remove(ITextEditorActionConstants.SHOW_WHITESPACE_CHARACTERS);
        }
        getActionBars().getToolBarManager().update(true);
        getActionBars().updateActionBars();
    }

    private void addTextActionIfNeeded(final ITextEditor editor, final String id, final ImageDescriptor image) {
        final IContributionItem item = getActionBars().getToolBarManager().find(id);
        if (item == null) {
            final IAction action = getAction(editor, id);
            action.setImageDescriptor(image);
            getActionBars().getToolBarManager().add(action);
        }
    }

    private void updateStatusLineContributions(final IEditorPart activeEditor) {
        final boolean isSourceEditor = activeEditor instanceof SuiteSourceEditor;

        if (isSourceEditor) {
            for (final Entry<StatusFieldDef, StatusLineContributionItem> contribution : statusFields.entrySet()) {
                contribution.getValue().setVisible(true);
                contribution.getValue()
                        .setActionHandler(getAction((ITextEditor) activeEditor, contribution.getKey().actionId));
            }
        } else {
            for (final Entry<StatusFieldDef, StatusLineContributionItem> contribution : statusFields.entrySet()) {
                contribution.getValue().setVisible(false);
                contribution.getValue().setActionHandler(null);
            }
        }
        getActionBars().getStatusLineManager().update(true);
    }

    private final IAction getAction(final ITextEditor editor, final String actionId) {
        return (editor == null || actionId == null ? null : editor.getAction(actionId));
    }

    @Override
    public void contributeToStatusLine(final IStatusLineManager statusLineManager) {
        for (final StatusLineContributionItem item : statusFields.values()) {
            statusLineManager.add(item);
        }
        statusLineManager.update(true);
    }

    private static class StatusFieldDef {

        private final String category;

        private final String actionId;

        private StatusFieldDef(final String category, final String actionId) {
            this.category = category;
            this.actionId = actionId;
        }
    }
}
