/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowView;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ToggleCommentInTableHandler.E4ToggleCommentInTableHandler;

public class ToggleCommentInTableHandlerTest {

    E4ToggleCommentInTableHandler handler = new E4ToggleCommentInTableHandler();

    @Test
    public void commentMarkIsAdded_whenThereIsUncommentedKeywordCallSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Log  t  # comment")
                .build();
        final RobotElement selected = getCall(model, 0);

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection(selected));

        assertThat(ExecutablesRowView.rowData(getCall(model, 0))).containsExactly("# Log", "t", "# comment");
    }

    @Test
    public void commentMarkIsRemoved_whenThereIsCommentedKeywordCallSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  # Log  t  # comment")
                .build();
        final RobotElement selected = getCall(model, 0);

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection(selected));

        assertThat(ExecutablesRowView.rowData(getCall(model, 0))).containsExactly("Log", "t", "# comment");
    }

    @Test
    public void commentMarkIsRemoved_whenThereIsCommentedKeywordCallWithoutSpaceSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  #Log  t  # comment")
                .build();
        final RobotElement selected = getCall(model, 0);

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection(selected));

        assertThat(ExecutablesRowView.rowData(getCall(model, 0))).containsExactly("Log", "t", "# comment");
    }

    @Test
    public void commentMarksAdded_whenThereAreUncommentedKeywordCallsSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  Kw1  t  # comment")
                .appendLine("  Kw2  t  # comment")
                .build();
        final RobotElement selected1 = getCall(model, 0);
        final RobotElement selected2 = getCall(model, 1);

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection(selected1, selected2));

        assertThat(ExecutablesRowView.rowData(getCall(model, 0))).containsExactly("# Kw1", "t", "# comment");
        assertThat(ExecutablesRowView.rowData(getCall(model, 1))).containsExactly("# Kw2", "t", "# comment");
    }

    @Test
    public void commentMarksRemoved_whenThereAreCommentedKeywordCallsSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  # Kw1  t  # comment")
                .appendLine("  #Kw2  t  # comment")
                .build();
        final RobotElement selected1 = getCall(model, 0);
        final RobotElement selected2 = getCall(model, 1);

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection(selected1, selected2));

        assertThat(ExecutablesRowView.rowData(getCall(model, 0))).containsExactly("Kw1", "t", "# comment");
        assertThat(ExecutablesRowView.rowData(getCall(model, 1))).containsExactly("Kw2", "t", "# comment");
    }

    @Test
    public void commentMarksAdded_whenThereAreMixedKeywordCallsSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  # Kw1  t  # comment")
                .appendLine("  Kw2  t  # comment")
                .build();
        final RobotElement selected1 = getCall(model, 0);
        final RobotElement selected2 = getCall(model, 1);

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection(selected1, selected2));

        assertThat(ExecutablesRowView.rowData(getCall(model, 0))).containsExactly("# # Kw1", "t", "# comment");
        assertThat(ExecutablesRowView.rowData(getCall(model, 1))).containsExactly("# Kw2", "t", "# comment");
    }

    @Test
    public void commentMarksAdded_whenThereIsRobotDefinitionSettingSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Documentation]  doc  # comment")
                .build();
        final RobotElement selected = getCall(model, 0);

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection(selected));

        assertThat(ExecutablesRowView.rowData(getCall(model, 0))).containsExactly("# [Documentation]", "doc",
                "# comment");
    }

    @Test
    public void nothingHappened_whenThereAreNoKeywordCallsSelected() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .build();

        handler.toggleCommentInTable(new RobotEditorCommandsStack(), selection());

        final List<RobotCase> tests = model.findSection(RobotCasesSection.class).get().getChildren();

        assertThat(tests).hasSize(1);
        assertThat(tests.get(0).getName()).isEqualTo("t1");
        assertThat(tests.get(0).getChildren()).isEmpty();
    }

    private static RobotKeywordCall getCall(final RobotSuiteFile model, final int index) {
        return model.findSection(RobotCasesSection.class).get().getChildren().get(0).getChildren().get(index);
    }

    private static IStructuredSelection selection(final RobotElement... selected) {
        return new StructuredSelection(selected);
    }
}
