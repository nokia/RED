package org.robotframework.ide.eclipse.main.plugin.propertytester;

import javax.inject.Named;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.IEditorPart;
import org.robotframework.ide.eclipse.main.plugin.propertytester.RobotEditorPropertyTester.E4RobotEditorPropertyTester;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DIPropertyTester;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.CasesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordCallsTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.KeywordDefinitionsTransfer;

public class RobotEditorPropertyTester extends DIPropertyTester<E4RobotEditorPropertyTester> {

    public RobotEditorPropertyTester() {
        super(E4RobotEditorPropertyTester.class);
    }

    public static class E4RobotEditorPropertyTester {

        @PropertyTest
        public Boolean testEditorProperties(@Named(DIPropertyTester.RECEIVER) final RobotFormEditor robotEditor,
                @Named(DIPropertyTester.PROPERTY) final String propertyName,
                @Named(DIPropertyTester.EXPECTED_VALUE) final Boolean expected) {

            if ("editorModelIsEditable".equals(propertyName)) {
                return robotEditor.provideSuiteModel().isEditable() == expected.booleanValue();

            } else if ("activeSectionEditorHasSection".equals(propertyName)) {
                final IEditorPart activeEditor = robotEditor.getActiveEditor();
                final ISectionEditorPart activePage = activeEditor instanceof ISectionEditorPart ? (ISectionEditorPart) activeEditor
                        : null;
                if (activePage != null) {
                    return activePage.provideSection(robotEditor.provideSuiteModel()).isPresent() == expected
                            .booleanValue();
                } else {
                    return !expected;
                }

            } else if ("thereAreKeywordDefinitionElementsInClipboard".equals(propertyName)) {
                final Clipboard clipboard = robotEditor.getClipboard();
                return KeywordDefinitionsTransfer.hasKeywordDefinitions(clipboard) == expected.booleanValue();

            } else if ("thereAreKeywordCallElementsInClipboard".equals(propertyName)) {
                final Clipboard clipboard = robotEditor.getClipboard();
                return KeywordCallsTransfer.hasKeywordCalls(clipboard) == expected.booleanValue();

            } else if ("thereAreCasesElementsInClipboard".equals(propertyName)) {
                final Clipboard clipboard = robotEditor.getClipboard();
                return CasesTransfer.hasCases(clipboard) == expected.booleanValue();

            } else if ("thereIsTextInClipboard".equals(propertyName)) {
                final Clipboard clipborad = robotEditor.getClipboard();
                return (clipborad.getContents(TextTransfer.getInstance()) != null) == expected.booleanValue();

            }
            return false;
        }
    }
}
