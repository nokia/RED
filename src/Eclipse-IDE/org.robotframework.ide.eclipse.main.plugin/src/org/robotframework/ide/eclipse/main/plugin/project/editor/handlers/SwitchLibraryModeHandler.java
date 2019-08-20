/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static java.util.stream.Collectors.joining;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.SwitchLibraryModeHandler.E4SwitchLibraryModeHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

/**
 * @author Michal Anglart
 */
public class SwitchLibraryModeHandler extends DIParameterizedHandler<E4SwitchLibraryModeHandler> {

    public SwitchLibraryModeHandler() {
        super(E4SwitchLibraryModeHandler.class);
    }

    public static class E4SwitchLibraryModeHandler {

        @Execute
        public void switchMode(final Shell shell, @Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {

            final List<RedXmlLibrary> libs = Selections.getElements(selection, RedXmlLibrary.class);
            final boolean targetMode = !libs.stream()
                    .findFirst()
                    .map(RedXmlLibrary::isDynamic)
                    .orElseThrow(IllegalStateException::new);

            if (!targetMode) {
                // when switching to static we do not want to have static libs with more than 1
                // arguments list
                final String libsWithTooMany = libs.stream()
                        .filter(RedXmlLibrary::isDynamic)
                        .filter(l -> l.getVariants().size() > 1)
                        .map(RedXmlLibrary::getLibrary)
                        .map(ReferencedLibrary::getName)
                        .collect(joining("\n  -"));
                if (!libsWithTooMany.isEmpty()) {
                    final String enumeratedLibs = "\n  -" + libsWithTooMany;
                    final MessageDialog dialog = new MessageDialog(shell, "Dynamic libraries detected", null,
                            String.format(
                                    "Following libraries:%s\n\nare currently dynamic and have more than one arguments "
                                            + "list defined. After switching them to static unnecessary arguments will "
                                            + "be removed. Do you want to proceed?",
                                    enumeratedLibs),
                            MessageDialog.QUESTION, new String[] { "Proceed", "Cancel" }, 0);

                    final int returnCode = dialog.open();
                    if (returnCode != 0) { // other than 'Proceed'
                        return;
                    }
                }
            }

            for (final RedXmlLibrary lib : libs) {
                lib.setDynamic(targetMode);
            }
            if (!libs.isEmpty()) {
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED,
                        new RedProjectConfigEventData<>(input.getFile(), libs));
            }
        }
    }
}
