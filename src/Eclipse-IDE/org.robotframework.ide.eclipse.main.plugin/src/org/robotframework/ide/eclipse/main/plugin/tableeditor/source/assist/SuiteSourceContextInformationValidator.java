/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * @author mmarzec
 *
 */
public class SuiteSourceContextInformationValidator
        implements IContextInformationValidator, IContextInformationPresenter {
	
	private ITextViewer viewer;
	
	private int currentLine;
	private int currentOffset;

	@Override
	public void install(final IContextInformation contextInformation, final ITextViewer viewer, final int offset) {
		this.viewer = viewer;
		
		this.currentLine = viewer.getTextWidget().getLineAtOffset(offset);
        this.currentOffset = offset;
	}

	@Override
	public boolean isContextInformationValid(final int offset) {
        final int line = viewer.getTextWidget().getLineAtOffset(offset);
        return line == currentLine && offset >= currentOffset;
	}

    @Override
    public boolean updatePresentation(final int offset, final TextPresentation presentation) {
        // TODO : possibly update context informations
        return false;
    }
}
