package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.IEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.CasesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordCallsTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordDefinitionsTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.VariablesTransfer;

import com.google.common.base.Preconditions;

public class RobotEditorPropertyTester extends PropertyTester {

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof RobotFormEditor,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + RobotFormEditor.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((RobotFormEditor) receiver, property, ((Boolean) expectedValue).booleanValue());
        }
        return false;
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final boolean expected) {
        if ("editorModelIsEditable".equals(property)) {
            return editor.provideSuiteModel().isEditable() == expected;
        } else if ("activeSectionEditorHasSection".equals(property)) {
            final IEditorPart activeEditor = editor.getActiveEditor();
            final ISectionEditorPart activePage = activeEditor instanceof ISectionEditorPart ? (ISectionEditorPart) activeEditor
                    : null;
            if (activePage != null) {
                return activePage.provideSection(editor.provideSuiteModel()).isPresent() == expected;
            } else {
                return !expected;
            }
        } else if ("thereAreKeywordDefinitionElementsInClipboard".equals(property)) {
            final Clipboard clipboard = editor.getClipboard();
            return KeywordDefinitionsTransfer.hasKeywordDefinitions(clipboard) == expected;

        } else if ("thereAreKeywordCallElementsInClipboard".equals(property)) {
            final Clipboard clipboard = editor.getClipboard();
            return KeywordCallsTransfer.hasKeywordCalls(clipboard) == expected;

        } else if ("thereAreCasesElementsInClipboard".equals(property)) {
            final Clipboard clipboard = editor.getClipboard();
            return CasesTransfer.hasCases(clipboard) == expected;

        } else if ("thereAreVariablesInClipboard".equals(property)) {
            final Clipboard clipboard = editor.getClipboard();
            return VariablesTransfer.hasVariables(clipboard) == expected;

        } else if ("thereIsTextInClipboard".equals(property)) {
            final Clipboard clipborad = editor.getClipboard();
            return (clipborad.getContents(TextTransfer.getInstance()) != null) == expected;

        }
        return false;
    }
}
