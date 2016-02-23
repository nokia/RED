/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.RenameInSourceHandler.E4RenameInSourceHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
public class RenameInSourceHandler extends DIParameterizedHandler<E4RenameInSourceHandler> {

    public RenameInSourceHandler() {
        super(E4RenameInSourceHandler.class);
    }

    public static class E4RenameInSourceHandler {

        @Execute
        public Object formatSource(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel) {
            final SourceViewer viewer = editor.getSourceEditor().getViewer();
            final int offset = viewer.getTextWidget().getCaretOffset();
            final IDocument document = editor.getSourceEditor().getDocument();

            try {
                final Optional<IRegion> cell = DocumentUtilities.findCellRegion(document, offset);
                if (cell.isPresent()) {
                    /* create groups - this step is independent of the linked mode */
                    final LinkedPositionGroup group1 = new LinkedPositionGroup();
                    group1.addPosition(new LinkedPosition(document, cell.get().getOffset(), cell.get().getLength()));

                    /* set up linked mode */
                    final LinkedModeModel model = new LinkedModeModel();
                    model.addGroup(group1);
                    model.forceInstall();

                    /* create UI */
                    final LinkedModeUI ui = new LinkedModeUI(model, new ITextViewer[] { viewer });
                    ui.enter();
                }
            } catch (final BadLocationException e) {
                // nothing to do
            }

            return null;
        }

//        private void foo(final RobotSuiteFile fileModel) {
//            new KeywordDefinitionLocator(fileModel).locateKeywordDefinition(new KeywordDetector() {
//
//                @Override
//                public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
//                        final KeywordSpecification kwSpec) {
//                    return ContinueDecision.CONTINUE;
//                }
//
//                @Override
//                public ContinueDecision keywordDetected(final RobotSuiteFile file,
//                        final RobotKeywordDefinition keyword) {
//                    if (file == fileModel) {
//
//                    }
//                    return null;
//                }
//            });
//        }
    }
}
