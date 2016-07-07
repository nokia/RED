/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class RobotEditorPropertyTester extends PropertyTester {

    public static final String NAMESPACE = "org.robotframework";

    @VisibleForTesting static final String THERE_IS_TEXT_IN_CLIPBOARD = "thereIsTextInClipboard";
    @VisibleForTesting static final String THERE_ARE_VARIABLES_IN_CLIPBOARD = "thereAreVariablesInClipboard";
    @VisibleForTesting static final String THERE_ARE_IMPORT_SETTINGS_IN_CLIPBOARD = "thereAreImportSettingsInClipboard";
    @VisibleForTesting static final String THERE_ARE_METADATA_SETTINGS_IN_CLIPBOARD = "thereAreMetadataSettingsInClipboard";
    @VisibleForTesting static final String THERE_ARE_GENERAL_SETTINGS_IN_CLIPBOARD = "thereAreGeneralSettingsInClipboard";
    @VisibleForTesting static final String THERE_ARE_CASES_ELEMENTS_IN_CLIPBOARD = "thereAreCasesElementsInClipboard";
    @VisibleForTesting static final String THERE_ARE_KEYWORD_CALL_ELEMENTS_IN_CLIPBOARD = "thereAreKeywordCallElementsInClipboard";
    @VisibleForTesting static final String THERE_ARE_KEYWORD_DEFINITION_ELEMENTS_IN_CLIPBOARD = "thereAreKeywordDefinitionElementsInClipboard";
    @VisibleForTesting static final String ACTIVE_SECTION_EDITOR_HAS_SECTION = "activeSectionEditorHasSection";
    @VisibleForTesting static final String EDITOR_MODEL_IS_EDITABLE = "editorModelIsEditable";

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
        if (EDITOR_MODEL_IS_EDITABLE.equals(property)) {
            return editor.provideSuiteModel().isEditable() == expected;
        } else if (ACTIVE_SECTION_EDITOR_HAS_SECTION.equals(property)) {
            final IEditorPart activeEditor = editor.getActiveEditor();
            final ISectionEditorPart activePage = activeEditor instanceof ISectionEditorPart ? (ISectionEditorPart) activeEditor
                    : null;
            if (activePage != null) {
                return activePage.provideSection(editor.provideSuiteModel()).isPresent() == expected;
            } else {
                return !expected;
            }
        } else if (THERE_ARE_KEYWORD_DEFINITION_ELEMENTS_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasKeywordDefinitions() == expected;

        } else if (THERE_ARE_CASES_ELEMENTS_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasCases() == expected;

        } else if (THERE_ARE_KEYWORD_CALL_ELEMENTS_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasKeywordCalls() == expected;

        } else if (THERE_ARE_GENERAL_SETTINGS_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasGeneralSettings() == expected;

        } else if (THERE_ARE_METADATA_SETTINGS_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasMetadataSettings() == expected;

        } else if (THERE_ARE_IMPORT_SETTINGS_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasImportSettings() == expected;

        } else if (THERE_ARE_VARIABLES_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasVariables() == expected;

        } else if (THERE_IS_TEXT_IN_CLIPBOARD.equals(property)) {
            return editor.getClipboard().hasText() == expected;
        }
        return false;
    }
}
