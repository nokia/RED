/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ShowInTableHandler.E4ShowInTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

public class ShowInTableHandler extends DIParameterizedHandler<E4ShowInTableHandler> {

    public ShowInTableHandler() {
        super(E4ShowInTableHandler.class);
    }

    public static class E4ShowInTableHandler {

        @Execute
        public Object openDeclaration(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile suiteModel) {

            final SuiteSourceEditor sourceEditor = editor.getSourceEditor();
            final int offset = sourceEditor.getViewer().getTextWidget().getCaretOffset();

            final Optional<? extends RobotElement> element = suiteModel.findElement(offset);
            
            if (element.isPresent()) {
                final RobotFileInternalElement e = (RobotFileInternalElement) element.get();
                final ISectionEditorPart activatedPage = editor.activatePage(getSection(e));
                if (activatedPage != null) {
                    activatedPage.setFocus();
                    activatedPage.revealElement(e);
                }
            }
            
            return null;
        }

        private RobotSuiteFileSection getSection(final RobotFileInternalElement element) {
            RobotElement current = element;
            while (current != null && !(current instanceof RobotSuiteFileSection)) {
                current = current.getParent();
            }
            return (RobotSuiteFileSection) current;
        }
    }
}
