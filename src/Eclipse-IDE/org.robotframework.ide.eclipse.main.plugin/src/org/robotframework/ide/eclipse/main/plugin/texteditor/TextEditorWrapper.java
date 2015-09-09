/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
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
}
