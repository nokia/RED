/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SourceDocumentFormatter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorDifferenceFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorSelectionFixer;

import com.google.common.annotations.VisibleForTesting;

public class OnSaveSourceFormattingTrigger {

    private final RedPreferences preferences = RedPlugin.getDefault().getPreferences();


    void formatSourceIfRequired(final Supplier<IDocument> documentSupplier,
            final SuiteSourceEditorSelectionFixer selectionFixer, final RobotSuiteFile fileModel,
            final IProgressMonitor progressMonitor) {

        final SourceDocumentFormatter formatter = getFormatter(fileModel);

        if (shouldFormat(fileModel)) {
            final IDocument document = documentSupplier.get();
            try {
                selectionFixer.saveSelection(document);

                if (shouldFormatChangedLinesOnly()) {
                    final List<Integer> linesToFormat = SuiteSourceEditorDifferenceFinder
                            .calculateChangedLines(fileModel.getFile(), document, progressMonitor);
                    formatter.format(document, linesToFormat);
                } else {
                    formatter.format(document);
                }
                selectionFixer.fixSelection(document);

            } catch (final BadLocationException e) {
                // some regions where not formatted
            }
        }
    }

    @VisibleForTesting
    public SourceDocumentFormatter getFormatter(final RobotSuiteFile fileModel) {
        return SourceDocumentFormatter.create(preferences, fileModel.getRuntimeEnvironment());
    }

    private boolean shouldFormat(final RobotSuiteFile fileModel) {
        return !fileModel.isTsvFile() && preferences.isSaveActionsCodeFormattingEnabled();
    }

    private boolean shouldFormatChangedLinesOnly() {
        return preferences.isSaveActionsChangedLinesOnlyEnabled();
    }
}
