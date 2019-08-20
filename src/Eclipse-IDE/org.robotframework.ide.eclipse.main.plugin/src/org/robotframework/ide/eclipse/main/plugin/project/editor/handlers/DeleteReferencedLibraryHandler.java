/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static java.util.stream.Collectors.toList;

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
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlArgumentsVariant.RedXmlRemoteArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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

            final List<RedXmlLibrary> libs = Selections.getElements(selection, RedXmlLibrary.class);
            final List<RedXmlArgumentsVariant> variants = Selections.getElements(selection,
                    RedXmlArgumentsVariant.class);

            if (!libs.isEmpty()) {
                final List<ReferencedLibrary> refLibs = libs.stream()
                        .map(RedXmlLibrary::getLibrary)
                        .filter(l -> l != null)
                        .collect(toList());
                final boolean removedRefLibs = input.getProjectConfiguration().removeReferencedLibraries(refLibs);
                if (removedRefLibs) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED,
                            new RedProjectConfigEventData<>(input.getFile(), libs));
                    input.getRobotProject().unregisterWatchingOnReferencedLibraries(refLibs);
                }

            } else if (!variants.isEmpty()) {
                final List<RedXmlLibrary> libraries = new ArrayList<>();
                final List<RemoteLocation> locations = new ArrayList<>();
                final Multimap<ReferencedLibrary, ReferencedLibraryArgumentsVariant> groupedVariants = ArrayListMultimap
                        .create();

                for (final RedXmlArgumentsVariant variant : variants) {
                    final RedXmlLibrary lib = variant.getParent();
                    libraries.add(lib);

                    if (variant instanceof RedXmlRemoteArgumentsVariant) {
                        locations.add(((RedXmlRemoteArgumentsVariant) variant).getRemoteLocation());
                    } else {
                        groupedVariants.put(lib.getLibrary(), variant.getVariant());
                    }
                }

                boolean removedVariants = false;
                for (final ReferencedLibrary refLib : groupedVariants.keySet()) {
                    removedVariants |= refLib.removeArgumentsVariants(groupedVariants.get(refLib));
                }
                final boolean removedRemotes = input.getProjectConfiguration().removeRemoteLocations(locations);

                if (removedRemotes || removedVariants) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED,
                            new RedProjectConfigEventData<>(input.getFile(), libraries));
                }
            }
        }
    }
}
