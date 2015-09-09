/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.ContentAssistKeywordContext;

/**
 * @author mmarzec
 *
 */
public class TextEditorTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

    private Map<String, ContentAssistKeywordContext> keywordMap;

    private Map<String, Object> debugVariables;

    private TextEditorHoverManager hoverManager;

    public TextEditorTextHover(final Map<String, ContentAssistKeywordContext> keywordMap) {
        this.keywordMap = new HashMap<>();
        for (final String keywordName : keywordMap.keySet()) {
            this.keywordMap.put(keywordName.toLowerCase(), keywordMap.get(keywordName));
        }
        hoverManager = new TextEditorHoverManager();
    }
    
    @Override
    public IRegion getHoverRegion(final ITextViewer viewer, final int offset) {

        return hoverManager.findHoveredText(viewer, offset);
    }

    @Override
    public Object getHoverInfo2(final ITextViewer viewer, final IRegion hoverRegion) {

        try {
            final String hoveredText = viewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
            final ContentAssistKeywordContext keywordContext = keywordMap.get(hoveredText.toLowerCase());
            if (keywordContext != null) {
                return keywordContext.getDescription();
            }
            if (debugVariables != null) {
                return hoverManager.extractDebugVariableHoverInfo(debugVariables, hoveredText);
            }
        } catch (BadLocationException e) {
        }
        return null;
    }
    
    @Override
    public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
        return null;
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent);
            }
        };
    }

    public void setDebugVariables(final Map<String, Object> debugVariables) {
        this.debugVariables = debugVariables;
    }

}
