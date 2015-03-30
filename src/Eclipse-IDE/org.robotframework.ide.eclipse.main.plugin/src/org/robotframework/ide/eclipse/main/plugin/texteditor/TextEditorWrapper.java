package org.robotframework.ide.eclipse.main.plugin.texteditor;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;

/**
 * @author mmarzec
 *
 */
@SuppressWarnings("restriction")
public class TextEditorWrapper extends DIEditorPart<TextEditor>{

	public TextEditorWrapper() {
		super(TextEditor.class);
	}

	public void setPartName(String name) {
	    super.setPartName(name);
	}
}
