/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteReferencedLibraryHandler.E4DeleteReferencedLibraryHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesContentProvider.RemoteLibraryViewItem;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @author Michal Anglart
 *
 */
public class DeleteReferencedLibraryHandler extends DIParameterizedHandler<E4DeleteReferencedLibraryHandler> {

    public DeleteReferencedLibraryHandler() {
        super(E4DeleteReferencedLibraryHandler.class);
    }

    public static class E4DeleteReferencedLibraryHandler {

        @Execute
        public void deleteReferencedLibraries(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {

            final List<RemoteLocation> remotesFromLib = Selections
                    .getOptionalFirstElement(selection, RemoteLibraryViewItem.class)
                    .map(RemoteLibraryViewItem::getLocations)
                    .orElse(new ArrayList<RemoteLocation>());
            final List<ReferencedLibrary> libs = Selections.getElements(selection, ReferencedLibrary.class);
            final List<ReferencedLibraryArgumentsVariant> variants = Selections.getElements(selection,
                    ReferencedLibraryArgumentsVariant.class);
            final List<RemoteLocation> remotes = Selections.getElements(selection, RemoteLocation.class);

            if (!libs.isEmpty() || !remotesFromLib.isEmpty()) {
                final boolean removedRefLibs = input.getProjectConfiguration().removeReferencedLibraries(libs);
                final boolean removedRemotes = input.getProjectConfiguration().removeRemoteLocations(remotesFromLib);

                if (removedRefLibs) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED,
                            new RedProjectConfigEventData<>(input.getFile(), libs));
                    input.getRobotProject().unregisterWatchingOnReferencedLibraries(libs);
                }
                if (removedRemotes) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_REMOVED,
                            new RedProjectConfigEventData<>(input.getFile(), remotesFromLib));
                }

            } else if (!variants.isEmpty() || !remotes.isEmpty()) {
                final List<Object> removedElems = new ArrayList<>();
                removedElems.addAll(variants);
                removedElems.addAll(remotes);

                final boolean removedRemotes = input.getProjectConfiguration().removeRemoteLocations(remotes);

                final Multimap<ReferencedLibrary, ReferencedLibraryArgumentsVariant> groupedVariants = Multimaps
                        .index(variants, ReferencedLibraryArgumentsVariant::getParent);
                boolean removedVariants = false;
                for (final ReferencedLibrary refLib : groupedVariants.keySet()) {
                    removedVariants |= refLib.removeArgumentsVariants(groupedVariants.get(refLib));
                }

                if (removedRemotes || removedVariants) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_REMOVED,
                            new RedProjectConfigEventData<>(input.getFile(), removedElems));
                }
            }
        }
    }
}
