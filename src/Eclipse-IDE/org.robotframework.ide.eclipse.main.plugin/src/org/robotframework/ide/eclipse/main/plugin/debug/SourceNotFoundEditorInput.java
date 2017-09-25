/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorControls;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorInput;

class SourceNotFoundEditorInput implements RedDebuggerAssistantEditorInput {

    private final SourceOfStackFrameNotFound element;

    SourceNotFoundEditorInput(final SourceOfStackFrameNotFound element) {
        this.element = element;
    }

    public SourceOfStackFrameNotFound getElement() {
        return element;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public RedDebuggerAssistantEditorControls getControlsCreator(final IWorkbenchPage page) {
        return new SourceNotFoundEditorControls();
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return RedImages.getStackFrameImage();
    }

    @Override
    public ImageDescriptor getTitleImageDescriptor() {
        return RedImages.getBigErrorImage();
    }

    @Override
    public String getId() {
        return "assistant.editor.source.notFound";
    }

    @Override
    public String getTitle() {
        final String msg = element.instructionPointerText;
        return msg.contains("\n") ? msg.substring(0, msg.indexOf('\n')) : msg;
    }

    @Override
    public String getDetailedInformation() {
        final String msg = element.instructionPointerText;
        return msg.contains("\n") ? msg.substring(msg.indexOf('\n') + 1, msg.length()) : "";
    }

    @Override
    public String getName() {
        return element.frameName;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return element.frameName;
    }

    @Override
    public <T> T getAdapter(final Class<T> adapter) {
        return null;
    }

    static class SourceOfStackFrameNotFound {

        private final String instructionPointerText;

        private final String frameName;

        SourceOfStackFrameNotFound(final String frameName, final String instructionPointerText) {
            this.frameName = frameName;
            this.instructionPointerText = instructionPointerText;
        }
    }
}
