/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static com.google.common.collect.Lists.newArrayList;

import java.util.function.Predicate;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.AddLibraryArgumentsHandler.E4AddLibraryArgumentsHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesContentProvider.RemoteLibraryViewItem;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryArgumentsDialog;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RemoteLocationDialog;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


/**
 * @author Michal Anglart
 *
 */
public class AddLibraryArgumentsHandler extends DIParameterizedHandler<E4AddLibraryArgumentsHandler> {

    public AddLibraryArgumentsHandler() {
        super(E4AddLibraryArgumentsHandler.class);
    }

    public static class E4AddLibraryArgumentsHandler {

        @Execute
        public void addArguments(final Shell shell, @Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {

            final RobotProjectConfig config = input.getProjectConfiguration();

            final Predicate<Object> isLib = elem -> elem instanceof RemoteLibraryViewItem || elem instanceof ReferencedLibrary;
            Selections.getOptionalFirstElement(selection, isLib).ifPresent(lib -> {
                Object newElement;
                if (lib instanceof RemoteLibraryViewItem) {
                    newElement = addRemoteLocation(shell, config);
                } else {
                    newElement = addArguments(shell, (ReferencedLibrary) lib);
                }
                if (newElement != null) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_ADDED,
                            new RedProjectConfigEventData<>(input.getFile(), newArrayList(newElement)));
                }
            });
        }

        private RemoteLocation addRemoteLocation(final Shell shell, final RobotProjectConfig config) {
            final RemoteLocationDialog dialog = new RemoteLocationDialog(shell);
            if (dialog.open() == Window.OK) {
                final RemoteLocation newLocation = dialog.getCreatedElement();
                final boolean wasAdded = config.addRemoteLocation(newLocation);
                return wasAdded ? newLocation : null;
            }
            return null;
        }

        private ReferencedLibraryArgumentsVariant addArguments(final Shell shell, final ReferencedLibrary library) {
            boolean shouldChangeToDynamic = false;

            if (!library.isDynamic() && !library.getArgumentsVariants().isEmpty()) {
                final MessageDialog dialog = new MessageDialog(shell, "Static library detected", null,
                        String.format("The library '%s' is static and has arguments already defined. Only dynamic "
                                + "libraries can have multiple arguments lists defined. "
                                + "Do you want to make this library dynamic?", library.getName()),
                        MessageDialog.QUESTION, new String[] { "Mark as dynamic", "Cancel" }, 0);

                final int returnCode = dialog.open();
                if (returnCode != 0) { // other than 'Mark as dynamic'
                    return null;
                }
                shouldChangeToDynamic = true;
            }
            final ReferencedLibraryArgumentsDialog dialog = new ReferencedLibraryArgumentsDialog(shell);
            if (dialog.open() == Window.OK) {
                if (shouldChangeToDynamic) {
                    library.setDynamic(true);
                }
                final ReferencedLibraryArgumentsVariant newArgs = dialog.getCreatedElement();
                final boolean wasAdded = library.addArgumentsVariant(newArgs);
                return wasAdded ? newArgs : null;
            }
            return null;
        }
    }
}
