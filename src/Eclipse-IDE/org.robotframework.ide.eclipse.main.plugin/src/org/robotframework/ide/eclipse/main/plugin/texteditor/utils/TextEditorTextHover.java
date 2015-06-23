package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

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

/**
 * @author mmarzec
 *
 */
public class TextEditorTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

    private Map<String, TextEditorContentAssistKeywordContext> keywordMap;

    private Map<String, Object> debugVariables;

    private TextEditorHoverManager hoverManager;

    public TextEditorTextHover(Map<String, TextEditorContentAssistKeywordContext> keywordMap) {
        this.keywordMap = keywordMap;
        hoverManager = new TextEditorHoverManager();
    }
    
    @Override
    public IRegion getHoverRegion(ITextViewer viewer, int offset) {

        return hoverManager.findHoveredText(viewer, offset);
    }

    @Override
    public Object getHoverInfo2(ITextViewer viewer, IRegion hoverRegion) {

        try {
            String hoveredText = viewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
            TextEditorContentAssistKeywordContext keywordContext = keywordMap.get(hoveredText);
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
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        return null;
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent);
            }
        };
    }

    public void setDebugVariables(Map<String, Object> debugVariables) {
        this.debugVariables = debugVariables;
    }

}
