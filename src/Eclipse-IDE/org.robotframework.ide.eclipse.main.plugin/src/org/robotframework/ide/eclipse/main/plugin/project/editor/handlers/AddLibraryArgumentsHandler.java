/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static com.google.common.collect.Lists.newArrayList;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.AddLibraryArgumentsHandler.E4AddLibraryArgumentsHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary.RedXmlRemoteLib;
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

            Selections.getOptionalFirstElement(selection, RedXmlLibrary.class).ifPresent(lib -> {
                final boolean wasAdded = addArguments(shell, input.getProjectConfiguration(), lib);
                if (wasAdded) {
                    eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED,
                            new RedProjectConfigEventData<>(input.getFile(), newArrayList(lib)));
                }
            });
        }

        private boolean addArguments(final Shell shell, final RobotProjectConfig config, final RedXmlLibrary lib) {
            if (lib instanceof RedXmlRemoteLib) {
                return addRemoteLocation(shell, config);
            } else {
                return addArguments(shell, lib);
            }
        }

        private boolean addRemoteLocation(final Shell shell, final RobotProjectConfig config) {
            final RemoteLocationDialog dialog = new RemoteLocationDialog(shell);
            if (dialog.open() == Window.OK) {
                return config.addRemoteLocation(dialog.getCreatedElement());
            }
            return false;
        }

        private boolean addArguments(final Shell shell, final RedXmlLibrary lib) {
            boolean shouldChangeToDynamic = false;

            final ReferencedLibrary library = lib.getLibrary();
            if (!library.isDynamic() && !library.getArgumentsVariants().isEmpty()) {
                final MessageDialog dialog = new MessageDialog(shell, "Static library detected", null,
                        String.format("The library '%s' is static and has arguments already defined. Only dynamic "
                                + "libraries can have multiple arguments lists defined. "
                                + "Do you want to make this library dynamic?", library.getName()),
                        MessageDialog.QUESTION, new String[] { "Mark as dynamic", "Cancel" }, 0);

                final int returnCode = dialog.open();
                if (returnCode != 0) { // other than 'Mark as dynamic'
                    return false;
                }
                shouldChangeToDynamic = true;
            }
            final ReferencedLibraryArgumentsDialog dialog = new ReferencedLibraryArgumentsDialog(shell);
            if (dialog.open() == Window.OK) {
                if (shouldChangeToDynamic) {
                    library.setDynamic(true);
                }
                return library.addArgumentsVariant(dialog.getCreatedElement());
            }
            return false;
        }
    }
}
