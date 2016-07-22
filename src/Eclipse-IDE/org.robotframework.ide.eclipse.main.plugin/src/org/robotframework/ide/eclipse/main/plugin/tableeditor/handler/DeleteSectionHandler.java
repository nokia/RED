/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSectionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.DeleteSectionHandler.E4DeleteSection;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

public class DeleteSectionHandler extends DIParameterizedHandler<E4DeleteSection> {

    public DeleteSectionHandler() {
        super(E4DeleteSection.class);
    }

    public static class E4DeleteSection {

        @Execute
        public void deleteSectionFromActivePage(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack stack) {
            final IEditorPart activeEditor = editor.getActiveEditor();

            if (activeEditor instanceof ISectionEditorPart) {
                final ISectionEditorPart part = (ISectionEditorPart) activeEditor;
                final Optional<? extends RobotSuiteFileSection> section = part
                        .provideSection(editor.provideSuiteModel());
                if (section.isPresent()) {
                    final List<RobotSuiteFileSection> sectionsToRemove = Arrays
                            .asList((RobotSuiteFileSection) section.get());
                    stack.execute(new DeleteSectionCommand(sectionsToRemove));
                }
            }
        }
    }
}
