package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.FindElementUsagesInTextHandler.E4FindUsagesInTextHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class FindElementUsagesInTextHandler extends DIParameterizedHandler<E4FindUsagesInTextHandler> {

    public FindElementUsagesInTextHandler() {
        super(E4FindUsagesInTextHandler.class);
    }

    public static class E4FindUsagesInTextHandler {

        @Execute
        public void findUsages(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named("org.robotframework.ide.eclipse.findElementUsages.place") final String place) {

            final SourceViewer viewer = editor.getSourceEditor().getViewer();

            final ITextSelection selection = (ITextSelection) viewer.getSelection();

            final String selectedText = selection.getText();

            if (selectedText.isEmpty()) {
                // nothing is selected so we won't search
                return;
            }

            FindUsagesHandler.findElements(place, editor, selectedText);

        }

    }
}
