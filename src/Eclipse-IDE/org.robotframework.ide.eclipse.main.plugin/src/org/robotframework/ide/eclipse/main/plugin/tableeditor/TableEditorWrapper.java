package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;

public class TableEditorWrapper extends DIEditorPart<TableEditor> {

    public TableEditorWrapper() {
        super(TableEditor.class);
    }

    @Override
	public void setPartName(final String name) {
		super.setPartName(name);
	}
}
