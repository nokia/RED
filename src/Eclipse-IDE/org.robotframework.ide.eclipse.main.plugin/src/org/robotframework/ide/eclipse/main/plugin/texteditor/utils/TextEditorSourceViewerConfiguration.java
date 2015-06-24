package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class TextEditorSourceViewerConfiguration extends SourceViewerConfiguration {

    private TextEditorTextHover textEditorHover;

    public TextEditorSourceViewerConfiguration(TextEditorTextHover textEditorHover) {
        this.textEditorHover = textEditorHover;
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
    
}
