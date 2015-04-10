package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public class CasesEditorPage extends FormPage {

    private static final String ID = "org.robotframework.ide.eclipse.editor.mainPage";
    private static final String TITLE = "Edit Cases";

    public CasesEditorPage(final FormEditor editor) {
        super(editor, ID, TITLE);
    }

}
