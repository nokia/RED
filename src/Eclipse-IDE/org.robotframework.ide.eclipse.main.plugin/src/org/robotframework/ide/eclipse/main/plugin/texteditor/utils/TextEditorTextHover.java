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
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Shell;

public class TextEditorTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

    private Map<String, TextEditorContentAssistKeywordContext> keywordMap;

    private Map<String, Object> debugVariables;

    public TextEditorTextHover(Map<String, TextEditorContentAssistKeywordContext> keywordMap) {
        this.keywordMap = keywordMap;
    }

    @Override
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        return null;
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
                return extractDebugVariableHoverInfo(hoveredText);
            }
        } catch (BadLocationException e) {
        }
        return null;
    }

    @Override
    public IRegion getHoverRegion(ITextViewer viewer, int offset) {

        int length = 0;
        int beginOffset = offset;
        String firstCharacter = getTextRange(viewer, offset, 1);
        if (!Character.isWhitespace(firstCharacter.charAt(0))) {
            length = 1;
            while (!isSeparator(getTextRange(viewer, offset + length, 2))) {
                length++;
            }
            int j = offset - 1;
            while (!isSeparatorInReverseSearching(getTextRangeInReverseSearching(viewer, j - 1, 2))) {
                beginOffset--;
                j--;
                length++;
            }
        }

        return new Region(beginOffset, length);
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

    private String extractDebugVariableHoverInfo(String hoveredText) {
        if (hoveredText.indexOf("}=") >= 0 || hoveredText.indexOf("} =") >= 0) {
            hoveredText = hoveredText.substring(0, hoveredText.lastIndexOf("}") + 1);
        }
        Object value = debugVariables.get(hoveredText);
        return value != null ? value.toString() : null;
    }

    private boolean isSeparator(String s) {
        if (s.substring(0, 1).equals(" ") && !Character.isWhitespace(s.charAt(1))) {
            return false;
        } else if (Character.isWhitespace(s.charAt(0))) {
            return true;
        }
        return false;
    }

    private boolean isSeparatorInReverseSearching(String s) {
        if (s.substring(1, 2).equals(" ") && !Character.isWhitespace(s.charAt(0))) {
            return false;
        } else if (Character.isWhitespace(s.charAt(1))) {
            return true;
        }
        return false;
    }

    private String getTextRange(ITextViewer viewer, int offset, int length) {

        String result = "  ";
        try {
            return viewer.getDocument().get(offset, length);
        } catch (BadLocationException e) {
            result = getTextRange(viewer, offset, length - 1) + " ";
        }

        return result;
    }

    private String getTextRangeInReverseSearching(ITextViewer viewer, int offset, int length) {

        String result = "  ";
        try {
            return viewer.getDocument().get(offset, length);
        } catch (BadLocationException e) {
            result = " " + getTextRangeInReverseSearching(viewer, offset + 1, length - 1);
        }

        return result;
    }

    public void setDebugVariables(Map<String, Object> debugVariables) {
        this.debugVariables = debugVariables;
    }

}
