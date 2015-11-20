/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.OpenDeclarationHandler.E4OpenDeclarationHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks.HyperlinkToFilesDetector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks.HyperlinkToKeywordsDetector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks.HyperlinkToVariablesDetector;

public class OpenDeclarationHandler extends DIHandler<E4OpenDeclarationHandler> {

    public OpenDeclarationHandler() {
        super(E4OpenDeclarationHandler.class);
    }

    public static class E4OpenDeclarationHandler {

        @Execute
        public Object openDeclaration(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel) {
            final SourceViewer viewer = editor.getSourceEditor().getViewer();
            final int offset = viewer.getTextWidget().getCaretOffset();
            final Region hyperlinkRegion = new Region(offset, 0);

            IHyperlinkDetector detector = new HyperlinkToVariablesDetector(fileModel);
            IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, hyperlinkRegion, false);
            if (hyperlinks != null && hyperlinks.length > 0) {
                hyperlinks[0].open();
                return null;
            }

            detector = new HyperlinkToKeywordsDetector(fileModel);
            hyperlinks = detector.detectHyperlinks(viewer, hyperlinkRegion, false);
            if (hyperlinks != null && hyperlinks.length > 0) {
                hyperlinks[0].open();
                return null;
            }

            detector = new HyperlinkToFilesDetector(fileModel);
            hyperlinks = detector.detectHyperlinks(viewer, hyperlinkRegion, false);
            if (hyperlinks != null && hyperlinks.length > 0) {
                hyperlinks[0].open();
                return null;
            }

            return null;
        }
    }

}
