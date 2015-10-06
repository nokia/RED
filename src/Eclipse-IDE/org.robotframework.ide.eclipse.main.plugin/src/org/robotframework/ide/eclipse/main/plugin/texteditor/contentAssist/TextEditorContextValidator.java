/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * @author mmarzec
 *
 */
public class TextEditorContextValidator implements IContextInformationValidator{
	
	private ITextViewer viewer;
	
	private int currentLine;
	private int currentOffset;
	
	@Override
	public void install(final IContextInformation contextInformation, final ITextViewer viewer, final int offset) {
		this.viewer = viewer;
		
		currentLine = viewer.getTextWidget().getLineAtOffset(offset);
		currentOffset = offset;
	}

	@Override
	public boolean isContextInformationValid(final int offset) {
        final int line = viewer.getTextWidget().getLineAtOffset(offset);
        if (line != currentLine || offset < currentOffset) {
            return false;
        }
        return true;
	}
	
}
