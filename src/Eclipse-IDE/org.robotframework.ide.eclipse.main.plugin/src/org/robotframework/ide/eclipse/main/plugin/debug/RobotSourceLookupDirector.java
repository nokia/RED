/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceInLibraryEditorInput.SourceOfStackFrameInLibrary;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceNotFoundEditorInput.SourceOfStackFrameNotFound;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;


public class RobotSourceLookupDirector extends AbstractSourceLookupDirector {

    private final RedWorkspace workspace;

    public RobotSourceLookupDirector() {
        this.workspace = new RedWorkspace(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    public void initializeParticipants() {
        // no participants to add we'll do lookup without them
    }

    @Override
    protected List<Object> doSourceLookup(final Object element) {
        if (element instanceof RobotStackFrame) {
            final RobotStackFrame frame = (RobotStackFrame) element;
            final Optional<IResource> resource = frame.getPath()
                    .map(uri -> workspace.forUri(uri));

            if (resource.isPresent()) {
                final List<Object> sources = newArrayList(resource.get());
                cacheResolvedElement(sources, element);
                return sources;

            } else if (frame.isErroneous()) {
                final List<Object> sources = newArrayList(
                        new SourceOfStackFrameNotFound(frame.getLabel(), frame.getInstructionPointerText()));
                cacheResolvedElement(sources, element);
                return sources;

            } else if (frame.isLibraryKeywordFrame()) {
                final List<Object> sources = newArrayList(
                        new SourceOfStackFrameInLibrary(frame.getName(), frame.getContextPath().orElse(null)));
                cacheResolvedElement(sources, element);
                return sources;

            } else {
                final List<Object> sources = newArrayList("<avoiding null source>");
                cacheResolvedElement(sources, element);
                return sources;
            }
        }
        return new ArrayList<>();
    }
}
