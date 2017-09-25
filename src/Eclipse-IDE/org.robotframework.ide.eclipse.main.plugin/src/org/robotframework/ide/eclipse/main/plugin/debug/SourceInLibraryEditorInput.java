/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorControls;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorInput;

class SourceInLibraryEditorInput implements RedDebuggerAssistantEditorInput {

    private final SourceOfStackFrameInLibrary element;

    SourceInLibraryEditorInput(final SourceOfStackFrameInLibrary element) {
        this.element = element;
    }

    SourceOfStackFrameInLibrary getElement() {
        return element;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public RedDebuggerAssistantEditorControls getControlsCreator(final IWorkbenchPage page) {
        return new SourceInLibraryEditorControls(page);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return RedImages.getStackFrameImage();
    }

    @Override
    public ImageDescriptor getTitleImageDescriptor() {
        return RedImages.getBigKeywordImage();
    }

    @Override
    public String getId() {
        return "assistant.editor.source.lib";
    }

    @Override
    public String getTitle() {
        return "Keyword " + element.frameName + " contained in library";
    }

    @Override
    public String getDetailedInformation() {
        final String p1 = "The keyword <b>" + element.frameName + "</b> is not a User Keyword but is written in "
                + "external library. RED debugger alone is not able to debug both Robot and python code "
                + "however it is possible to setup cooperation with other debugger (PyDev for example).";
        final String p2 = "For more information on setting up Robot and Python debugging sessions please refer to "
                + "RED user guide in help system.";
        final String p3 = "";
        final String p4 = "          <img href=\"source\"/><a href=\"source\">Click here</a> to try opening "
                + "source of this keyword.";
        final String p5 = "";
        final String p6 = "<b>Note</b>: if you don't want to see this page you can configure debugger in "
                + "<a href=\"preferences\">Preferences</a> so that it will never suspend inside library "
                + "keywords when stepping.";
        
        return "<form>" + Stream.of(p1, p2, p3, p4, p5, p6).map(p -> "<p>" + p + "</p>").collect(joining(""))
                + "</form>";
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

    Optional<URI> getFileUri() {
        return Optional.ofNullable(element.contextUri);
    }

    public String getKeywordName() {
        return element.frameName;
    }

    static class SourceOfStackFrameInLibrary {

        private final String frameName;

        private final URI contextUri;

        SourceOfStackFrameInLibrary(final String frameName, final URI contextUri) {
            this.frameName = frameName;
            this.contextUri = contextUri;
        }
    }
}
