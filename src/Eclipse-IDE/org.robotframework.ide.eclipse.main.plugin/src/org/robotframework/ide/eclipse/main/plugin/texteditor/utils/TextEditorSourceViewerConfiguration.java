package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class TextEditorSourceViewerConfiguration extends SourceViewerConfiguration{

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
	    return new DefaultAnnotationHover();
	}
	
	
}
