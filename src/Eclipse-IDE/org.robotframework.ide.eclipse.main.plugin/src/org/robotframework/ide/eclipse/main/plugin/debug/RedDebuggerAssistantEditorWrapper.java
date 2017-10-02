/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import javax.annotation.PostConstruct;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditor;
import org.robotframework.red.graphics.ImagesManager;

@SuppressWarnings("restriction")
public class RedDebuggerAssistantEditorWrapper extends DIEditorPart<RedDebuggerAssistantEditor>
        implements IReusableEditor {

    public static final String ID = "org.robotframework.red.debug.assistance.editor";

    public RedDebuggerAssistantEditorWrapper() {
        super(RedDebuggerAssistantEditor.class);
    }

    @Override
    public void setInput(final IEditorInput input) {
        setPartName(input.getName());
        setTitleImage(ImagesManager.getImage(input.getImageDescriptor()));

        if (input instanceof RedDebuggerAssistantEditorInput) {
            super.setInput(input);
            if (getComponent() != null) {
                getComponent().replaceInput((RedDebuggerAssistantEditorInput) input, getEditorSite().getPage());
            }
        } else {
            throw new IllegalStateException("Source Not Found Editor can only have "
                    + RedDebuggerAssistantEditorInput.class.getSimpleName() + " input");
        }
    }

    public static class RedDebuggerAssistantEditor {

        private RedDebuggerAssistantEditorControls controlsCreator;

        private String id;

        @PostConstruct
        public void construct(final IWorkbenchPage page, final Composite parent, final IEditorInput initialInput) {
            final RedDebuggerAssistantEditorInput input = (RedDebuggerAssistantEditorInput) initialInput;

            id = input.getId();
            controlsCreator = input.getControlsCreator(page);
            controlsCreator.construct(parent);

            controlsCreator.setInput(input);
        }

        @Focus
        public void focus() {
            controlsCreator.setFocus();
        }

        private void replaceInput(final RedDebuggerAssistantEditorInput input, final IWorkbenchPage page) {
            if (!id.equals(input.getId())) {
                final Composite parent = controlsCreator.getParent();
                try {
                    parent.setRedraw(false);

                    controlsCreator.dispose();

                    id = input.getId();
                    controlsCreator = input.getControlsCreator(page);
                    controlsCreator.construct(parent);
                } finally {
                    controlsCreator.setInput(input);
                    parent.setRedraw(true);
                    parent.redraw();
                    parent.layout();
                }
            } else {
                controlsCreator.setInput(input);
            }
        }

        @PersistState
        public void persistEditorState() {
            // nothing to persist
        }
    }

    public static interface RedDebuggerAssistantEditorInput extends IEditorInput {

        String getId();

        RedDebuggerAssistantEditorControls getControlsCreator(IWorkbenchPage page);

        ImageDescriptor getTitleImageDescriptor();

        String getTitle();

        String getDetailedInformation();
    }

    public static interface RedDebuggerAssistantEditorControls {

        Composite getParent();

        void construct(Composite parent);

        void dispose();

        void setFocus();

        void setInput(RedDebuggerAssistantEditorInput editorInput);

    }
}
