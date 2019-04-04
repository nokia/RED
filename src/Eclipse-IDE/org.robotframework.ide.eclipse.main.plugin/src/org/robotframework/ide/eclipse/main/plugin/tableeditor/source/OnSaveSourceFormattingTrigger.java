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
import org.eclipse.jface.text.Region;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorDifferenceFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorFormatter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceEditorSelectionFixer;

import com.google.common.annotations.VisibleForTesting;

class OnSaveSourceFormattingTrigger {

    private final SuiteSourceEditorFormatter formatter;

    OnSaveSourceFormattingTrigger() {
        this(new SuiteSourceEditorFormatter());
    }

    @VisibleForTesting
    OnSaveSourceFormattingTrigger(final SuiteSourceEditorFormatter formatter) {
        this.formatter = formatter;
    }

    void formatSourceIfRequired(final Supplier<IDocument> documentSupplier,
            final SuiteSourceEditorSelectionFixer selectionFixer, final RobotSuiteFile fileModel,
            final IProgressMonitor progressMonitor) {
        if (shouldFormat(fileModel)) {
            final IDocument document = documentSupplier.get();
            try {
                selectionFixer.saveSelection(document);

                if (RedPlugin.getDefault().getPreferences().isSaveActionsChangedLinesOnlyEnabled()) {
                    final List<Integer> linesToFormat = SuiteSourceEditorDifferenceFinder
                            .calculateChangedLines(fileModel.getFile(), document, progressMonitor);
                    formatter.format(document, linesToFormat);
                } else {
                    formatter.format(document, new Region(0, document.getLength()));
                }

                selectionFixer.fixSelection(document);
            } catch (final BadLocationException e) {
                // some regions where not formatted
            }
        }
    }

    private boolean shouldFormat(final RobotSuiteFile fileModel) {
        return !fileModel.isTsvFile() && RedPlugin.getDefault().getPreferences().isSaveActionsCodeFormattingEnabled();
    }
}
