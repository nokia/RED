/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper;

/**
 * @author mmarzec
 */
public class RobotPartListener implements IPartListener {

    private RobotEventBroker robotEventBroker;
    
    private KeywordContext keywordContext;

    public RobotPartListener(RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
        if (part != null) {
            TextEditorWrapper texteditor = part.getAdapter(org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper.class);
            if (texteditor != null && keywordContext != null) {
                String editorInputName = texteditor.getEditorInput().getName();

                String fileName = keywordContext.getFileName();
                int line = keywordContext.getLineNumber();
                if (editorInputName.equals(fileName)) {
                    robotEventBroker.sendHighlightLineEventToTextEditor(fileName, line, keywordContext.getVariables());
                }
            }
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
    }

    public void setKeywordContext(KeywordContext keywordContext) {
        this.keywordContext = keywordContext;
    }

}
