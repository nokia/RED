/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;

class SuiteSourceEditorConfiguration extends SourceViewerConfiguration {
    
    private final SuiteSourceEditor editor;

    public SuiteSourceEditorConfiguration(final SuiteSourceEditor editor) {
        this.editor = editor;
    }

    @Override
    public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
        final List<String> legal = newArrayList(SuiteSourcePartitionScanner.LEGAL_CONTENT_TYPES);
        legal.add(0, IDocument.DEFAULT_CONTENT_TYPE);
        return legal.toArray(new String[0]);
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
        final PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(
                new SingleTokenScanner(new TextAttribute(RedTheme.getSectionHeaderColor())));
        reconciler.setDamager(damagerRepairer, SuiteSourcePartitionScanner.SECTION_HEADER);
        reconciler.setRepairer(damagerRepairer, SuiteSourcePartitionScanner.SECTION_HEADER);

        damagerRepairer = new DefaultDamagerRepairer(
                new SingleTokenScanner(new TextAttribute(RedTheme.getCommentsColor())));
        reconciler.setDamager(damagerRepairer, SuiteSourcePartitionScanner.COMMENT);
        reconciler.setRepairer(damagerRepairer, SuiteSourcePartitionScanner.COMMENT);

        damagerRepairer = new DefaultDamagerRepairer(
                new SingleTokenScanner(new TextAttribute(RedTheme.getVariableColor())));
        reconciler.setDamager(damagerRepairer, SuiteSourcePartitionScanner.SCALAR_VARIABLE);
        reconciler.setRepairer(damagerRepairer, SuiteSourcePartitionScanner.SCALAR_VARIABLE);

        damagerRepairer = new DefaultDamagerRepairer(
                new SingleTokenScanner(new TextAttribute(RedTheme.getVariableColor())));
        reconciler.setDamager(damagerRepairer, SuiteSourcePartitionScanner.LIST_VARIABLE);
        reconciler.setRepairer(damagerRepairer, SuiteSourcePartitionScanner.LIST_VARIABLE);

        damagerRepairer = new DefaultDamagerRepairer(
                new SingleTokenScanner(new TextAttribute(RedTheme.getVariableColor())));
        reconciler.setDamager(damagerRepairer, SuiteSourcePartitionScanner.DICT_VARIABLE);
        reconciler.setRepairer(damagerRepairer, SuiteSourcePartitionScanner.DICT_VARIABLE);

        return reconciler;
    }

    @Override
    public IReconciler getReconciler(final ISourceViewer sourceViewer) {
        return new MonoReconciler(getReconcilingStrategy(), true);
    }

    private IReconcilingStrategy getReconcilingStrategy() {
        return new SuiteSourceReconcilingStrategy(editor);
    }

    private static class SingleTokenScanner extends BufferedRuleBasedScanner {
        public SingleTokenScanner(final TextAttribute attribute) {
            setDefaultReturnToken(new Token(attribute));
        }
    };
}
