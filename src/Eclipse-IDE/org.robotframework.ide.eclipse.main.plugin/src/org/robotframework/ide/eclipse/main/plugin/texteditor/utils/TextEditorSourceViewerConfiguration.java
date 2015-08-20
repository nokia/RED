package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class TextEditorSourceViewerConfiguration extends TextSourceViewerConfiguration {

    private TextEditorTextHover textEditorHover;
    
    private TextEditorIndentLineAutoEditStrategy indentLineAutoEditStrategy;

    public TextEditorSourceViewerConfiguration(TextEditorTextHover textEditorHover, TextEditorIndentLineAutoEditStrategy indentLineAutoEditStrategy) {
        this.textEditorHover = textEditorHover;
        this.indentLineAutoEditStrategy = indentLineAutoEditStrategy;
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new DefaultAnnotationHover();
    }

    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return textEditorHover;
    }

    @Override
    public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
        return super.getHyperlinkPresenter(sourceViewer);
    }

    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        return new IHyperlinkDetector[] { new KeywordHyperlinkDetector() };
    }
    
    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE, TextEditorPartitionScanner.TEST_CASES_SECTION,
                TextEditorPartitionScanner.KEYWORDS_SECTION, TextEditorPartitionScanner.SETTINGS_SECTION,
                TextEditorPartitionScanner.VARIABLES_SECTION };
    }
    
    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        return new IAutoEditStrategy[] { indentLineAutoEditStrategy };
    }
    
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        return null; //must be null to avoid conflicts with reconciler defined directly in TextEditor class
    }
    
    @Override
    public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
        return null;
    }
    
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        return null;
    }
    
    @Override
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
        return super.getIndentPrefixes(sourceViewer, contentType);
    }
    
}
