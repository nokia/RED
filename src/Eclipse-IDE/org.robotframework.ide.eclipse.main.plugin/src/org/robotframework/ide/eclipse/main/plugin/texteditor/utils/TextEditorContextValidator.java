package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * @author mmarzec
 *
 */
public class TextEditorContextValidator implements IContextInformationValidator{
	
	
	private IContentAssistProcessor processor;
	private IContextInformation currentContextInformation;
	private ITextViewer viewer;
	
	private int currentLine;
	private int currentOffset;
	
	public TextEditorContextValidator(IContentAssistProcessor processor) {
		this.processor= processor;
	}

	@Override
	public void install(IContextInformation contextInformation, ITextViewer viewer, int offset) {
		
		this.currentContextInformation = contextInformation;
		this.viewer = viewer;
		
		currentLine = viewer.getTextWidget().getLineAtOffset(offset);
		currentOffset = offset;
	}

	@Override
	public boolean isContextInformationValid(int offset) {
		
        int line = viewer.getTextWidget().getLineAtOffset(offset);
        if (line != currentLine || offset < currentOffset) {
            return false;
        }

//        IContextInformation[] contextInfos = processor.computeContextInformation(viewer, offset);
//        if (contextInfos != null && contextInfos.length > 0) {
//            for (int i = 0; i < contextInfos.length; i++)
//                if (currentContextInformation.equals(contextInfos[i]))
//                    return true;
//        }
//        return false;
        return true;
	}
	
}
