package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;


public class DebuggerPreferencePage extends RedFieldEditorPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.launch.debugger";

    public DebuggerPreferencePage() {
        setDescription("Configure debugger options");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createLibKeywordOmitEditor(parent);
        createErroneousStateBehaviourEditor(parent);
    }

    private void createLibKeywordOmitEditor(final Composite parent) {
        final BooleanFieldEditor omitLibraryKeywordsEditor = new BooleanFieldEditor(
                RedPreferences.DEBUGGER_OMIT_LIB_KEYWORDS, "Omit library keywords when stepping into/return", parent);
        addField(omitLibraryKeywordsEditor);
        final Button button = (Button) omitLibraryKeywordsEditor.getDescriptionControl(parent);
        GridDataFactory.fillDefaults().indent(10, 10).applyTo(button);
    }

    private void createErroneousStateBehaviourEditor(final Composite parent) {
        final String[][] labelAndValues = new String[][] { new String[] { "Always", "always" },
                new String[] { "Never", "never" }, new String[] { "Prompt", "prompt" } };
        final RadioGroupFieldEditor suspendOnErrorEditor = new RadioGroupFieldEditor(
                RedPreferences.DEBUGGER_SUSPEND_ON_ERROR,
                "Suspend execution whenever debugger goes into erroneous state", 3, labelAndValues, parent, true);
        GridDataFactory.fillDefaults().indent(0, 5).grab(true, false).span(2, 1).applyTo(
                suspendOnErrorEditor.getRadioBoxControl(parent));
        addField(suspendOnErrorEditor);
    }
}
