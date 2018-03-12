/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

public class RobotEditorPropertyTesterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RobotEditorPropertyTester tester = new RobotEditorPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotRobotEditor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + RobotFormEditor.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final boolean testResult = tester.test(mock(RobotFormEditor.class),
                RobotEditorPropertyTester.EDITOR_MODEL_IS_EDITABLE, null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        assertThat(tester.test(mock(RobotFormEditor.class), "unknown_property", null, true)).isFalse();
        assertThat(tester.test(mock(RobotFormEditor.class), "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testEditorModelEditableProperty() {
        final RobotSuiteFile editableModel = new RobotSuiteFileCreator().build();
        final RobotSuiteFile nonEditableModel = new RobotSuiteFileCreator().buildReadOnly();

        final RobotFormEditor editableEditor = mock(RobotFormEditor.class);
        final RobotFormEditor nonEditableEditor = mock(RobotFormEditor.class);

        when(editableEditor.provideSuiteModel()).thenReturn(editableModel);
        when(nonEditableEditor.provideSuiteModel()).thenReturn(nonEditableModel);

        assertThat(editorModelIsEditable(editableEditor, true)).isTrue();
        assertThat(editorModelIsEditable(editableEditor, false)).isFalse();
        assertThat(editorModelIsEditable(nonEditableEditor, true)).isFalse();
        assertThat(editorModelIsEditable(nonEditableEditor, false)).isTrue();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testActiveSectionEditorHasSectionProperty() {
        final RobotFormEditor editorOnSourcePage = mock(RobotFormEditor.class);
        final RobotFormEditor editorOnSectionPageWithSection = mock(RobotFormEditor.class);
        final RobotFormEditor editorOnSectionPageWithoutSection = mock(RobotFormEditor.class);

        final IEditorPart sourceEditorPart = mock(TextEditor.class);
        final DISectionEditorPart sectionEditorPartWithSection = mock(DISectionEditorPart.class);
        final DISectionEditorPart sectionEditorPartWithoutSection = mock(DISectionEditorPart.class);

        when(editorOnSourcePage.getActiveEditor()).thenReturn(sourceEditorPart);
        when(editorOnSourcePage.provideSuiteModel()).thenReturn(new RobotSuiteFileCreator().build());
        when(editorOnSectionPageWithSection.getActiveEditor()).thenReturn(sectionEditorPartWithSection);
        when(editorOnSectionPageWithSection.provideSuiteModel()).thenReturn(new RobotSuiteFileCreator().build());
        when(editorOnSectionPageWithoutSection.getActiveEditor()).thenReturn(sectionEditorPartWithoutSection);
        when(editorOnSectionPageWithoutSection.provideSuiteModel()).thenReturn(new RobotSuiteFileCreator().build());
        
        when(sectionEditorPartWithSection.provideSection(any(RobotSuiteFile.class)))
                .thenReturn(Optional.of(mock(RobotSuiteFileSection.class)));
        when(sectionEditorPartWithoutSection.provideSection(any(RobotSuiteFile.class))).thenReturn(Optional.empty());

        assertThat(activeSectionEditorHasSection(editorOnSourcePage, true)).isFalse();
        assertThat(activeSectionEditorHasSection(editorOnSourcePage, false)).isTrue();
        assertThat(activeSectionEditorHasSection(editorOnSectionPageWithSection, true)).isTrue();
        assertThat(activeSectionEditorHasSection(editorOnSectionPageWithSection, false)).isFalse();
        assertThat(activeSectionEditorHasSection(editorOnSectionPageWithoutSection, true)).isFalse();
        assertThat(activeSectionEditorHasSection(editorOnSectionPageWithoutSection, false)).isTrue();
    }

    @Test
    public void testThereAreKeywordDefinitionsInClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_ARE_KEYWORD_DEFINITION_ELEMENTS_IN_CLIPBOARD, createKeywords());
    }

    @Test
    public void testThereAreCasesClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_ARE_CASES_ELEMENTS_IN_CLIPBOARD, createCases());
    }

    @Test
    public void testThereAreCallsClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_ARE_KEYWORD_CALL_ELEMENTS_IN_CLIPBOARD, createCalls());
    }

    @Test
    public void testThereAreGeneralSettingsClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_ARE_GENERAL_SETTINGS_IN_CLIPBOARD, createGeneralSettings());
    }

    @Test
    public void testThereAreMetadataSettingsClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_ARE_METADATA_SETTINGS_IN_CLIPBOARD, createMetadataSettings());
    }

    @Test
    public void testThereAreImportSettingsClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_ARE_IMPORT_SETTINGS_IN_CLIPBOARD, createImportSettings());
    }

    @Test
    public void testThereAreVariablesClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_ARE_VARIABLES_IN_CLIPBOARD, createVariables());
    }

    @Test
    public void testThereIsTextClipboardProperty() {
        testProperty(RobotEditorPropertyTester.THERE_IS_TEXT_IN_CLIPBOARD, "some text");
    }

    public void testProperty(final String property, final Object expectedClipboardContent) {
        final RobotFormEditor editorWithExpectedContentInClipboard = mock(RobotFormEditor.class);
        final RobotFormEditor editorWithoutExpectedContentInClipboard = mock(RobotFormEditor.class);

        final RedClipboard clipboardWithExpectedContent = new RedClipboardMock()
                .insertContent(expectedClipboardContent);
        final RedClipboard clipboardWithoutExpectedContent = expectedClipboardContent instanceof String
                ? new RedClipboardMock().insertContent(createCases()) : new RedClipboardMock().insertContent("text");

        when(editorWithExpectedContentInClipboard.getClipboard()).thenReturn(clipboardWithExpectedContent);
        when(editorWithoutExpectedContentInClipboard.getClipboard()).thenReturn(clipboardWithoutExpectedContent);

        assertThat(tester.test(editorWithExpectedContentInClipboard, property, null, true)).isTrue();
        assertThat(tester.test(editorWithExpectedContentInClipboard, property, null, false)).isFalse();
        assertThat(tester.test(editorWithoutExpectedContentInClipboard, property, null, true)).isFalse();
        assertThat(tester.test(editorWithoutExpectedContentInClipboard, property, null, false)).isTrue();
    }

    private boolean editorModelIsEditable(final RobotFormEditor editor, final boolean expected) {
        return tester.test(editor, RobotEditorPropertyTester.EDITOR_MODEL_IS_EDITABLE, null, expected);
    }

    private boolean activeSectionEditorHasSection(final RobotFormEditor editor, final boolean expected) {
        return tester.test(editor, RobotEditorPropertyTester.ACTIVE_SECTION_EDITOR_HAS_SECTION, null, expected);
    }

    private static Object createKeywords() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().toArray(new RobotKeywordDefinition[0]);
    }

    private static Object createCases() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotCasesSection.class).get().getChildren().toArray(new RobotCase[0]);
    }

    private static Object createCalls() {
        final List<RobotKeywordCall> calls = new ArrayList<>();
        final RobotCase[] cases = (RobotCase[]) createCases();

        for (final RobotCase robotCase : cases) {
            calls.addAll(robotCase.getChildren());
        }
        return calls.toArray(new RobotKeywordCall[0]);
    }

    private static Object createVariables() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotVariablesSection.class).get().getChildren().toArray(new RobotVariable[0]);
    }

    private static Object createImportSettings() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotSettingsSection.class).get().getImportSettings().toArray(new RobotSetting[0]);
    }

    private static Object createMetadataSettings() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotSettingsSection.class).get().getMetadataSettings().toArray(new RobotSetting[0]);
    }

    private static Object createGeneralSettings() {
        final RobotSuiteFile model = createModel();
        return model.findSection(RobotSettingsSection.class).get().getGeneralSettings().toArray(new RobotSetting[0]);
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  Log  1")
                .appendLine("kw 2")
                .appendLine("  Log  2")
                .appendLine("kw 3")
                .appendLine("  Log  3")
                .appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log 10")
                .appendLine("case 2")
                .appendLine("  Log 20")
                .appendLine("*** Variables ***")
                .appendLine("${var1}  1")
                .appendLine("@{var2}  1  2  3")
                .appendLine("&{var3}  a=1  b=2  c=3")
                .appendLine("*** Settings ***")
                .appendLine("Library  Collections")
                .appendLine("Resource  res1.robot")
                .build();
    }
}
