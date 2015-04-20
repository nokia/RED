package org.robotframework.ide.eclipse.main.plugin.texteditor;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;

/**
 * @author mmarzec
 *
 */
public class TextEditorWrapper extends DIEditorPart<TextEditor>{

	public TextEditorWrapper() {
		super(TextEditor.class);
	}

	@Override
    public void setPartName(final String name) {
	    super.setPartName(name);
	}
}
