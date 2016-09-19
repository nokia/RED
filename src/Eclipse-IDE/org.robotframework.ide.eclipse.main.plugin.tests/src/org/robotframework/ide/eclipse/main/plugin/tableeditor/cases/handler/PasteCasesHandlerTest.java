/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.children;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelFunctions.toNames;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCaseConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCallConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.PasteCasesHandler.E4PasteCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

public class PasteCasesHandlerTest {

    private final E4PasteCasesHandler handler = new E4PasteCasesHandler();

    private final RedClipboard clipboard = new RedClipboardMock();

    private final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();

    @Before
    public void beforeTest() {
        clipboard.clear();
        commandsStack.clear();
    }

    @Test
    public void whenNoCasesSectionExist_itIsCreatedForPastedCases() {
        final RobotCase[] cases = createCasesToPaste();
        clipboard.insertContent((Object) cases);

        final RobotSuiteFile emptyModel = new RobotSuiteFileCreator().build();
        handler.pasteCases(emptyModel, selection(), clipboard, commandsStack);

        assertThat(emptyModel.getSections()).hasSize(1);
        final RobotCasesSection section = emptyModel.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    @Test
    public void whenSelectionIsEmpty_casesArePastedAtTheEndOfSection() {
        final RobotCase[] cases = createCasesToPaste();
        clipboard.insertContent((Object) cases);

        final RobotSuiteFile model = createTargetModel();
        handler.pasteCases(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3",
                "case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    @Test
    public void whenNonNestedAddingTokenIsSelected_casesArePastedAtTheEndOfSection() {
        final RobotCase[] cases = createCasesToPaste();
        clipboard.insertContent((Object) cases);

        final RobotSuiteFile model = createTargetModel();
        final IStructuredSelection selection = selection(new AddingToken(null, mock(TokenState.class)));
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3",
                "case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    @Test
    public void whenCaseIsSelected_casesArePastedBeforeIt() {
        final RobotCase[] cases = createCasesToPaste();
        clipboard.insertContent((Object) cases);

        final RobotSuiteFile model = createTargetModel();
        final RobotCase selectedCase = model.findSection(RobotCasesSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCase);
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing case 1", "case 1", "case 2",
                "case 3", "existing case 2", "existing case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    @Test
    public void whenKeywordCallIsSelected_casesArePastedBeforeItsParent() {
        final RobotCase[] cases = createCasesToPaste();
        clipboard.insertContent((Object) cases);

        final RobotSuiteFile model = createTargetModel();
        final RobotCase parentCase = model.findSection(RobotCasesSection.class).get().getChildren().get(1);
        final RobotKeywordCall selectedCall = parentCase.getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCall);
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "case 1", "case 2",
                "case 3", "existing case 2", "existing case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    @Test
    public void whenNestedAddingTokenIsSelected_casesArePastedBeforeItsParent() {
        final RobotCase[] cases = createCasesToPaste();
        clipboard.insertContent((Object) cases);

        final RobotSuiteFile model = createTargetModel();
        final RobotCase tokenParent = model.findSection(RobotCasesSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(new AddingToken(tokenParent, mock(TokenState.class)));
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "case 1", "case 2",
                "case 3", "existing case 2", "existing case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    @Test
    public void whenNoCasesSectionExist_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile emptyModel = new RobotSuiteFileCreator().build();

        handler.pasteCases(emptyModel, selection(), clipboard, commandsStack);

        assertThat(emptyModel.getSections()).isEmpty();
    }

    @Test
    public void whenSelectionIsEmpty_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();

        handler.pasteCases(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }
    
    @Test
    public void whenNonNestedAddingTokenIsSelected_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();

        final IStructuredSelection selection = selection(new AddingToken(null, mock(TokenState.class)));
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    @Test
    public void whenCaseIsSelected_callsArePastedAtTheEndOfIt() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotCase selectedCase = model.findSection(RobotCasesSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCase);
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3");
        
        final RobotCase fstCase = section.getChildren().get(0);
        assertThat(fstCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(fstCase.getChildren(), toNames())).containsExactly("a", "b");
        assertThat(fstCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
        
        final RobotCase sndCase = section.getChildren().get(1);
        assertThat(sndCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(sndCase.getChildren(), toNames())).containsExactly("c", "d", "call1", "call2");
        assertThat(sndCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotCase trdCase = section.getChildren().get(2);
        assertThat(trdCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(trdCase.getChildren(), toNames())).containsExactly("e", "f");
        assertThat(trdCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenKeywordCallIsSelected_callsArePastedBeforeIt() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotCase parentCase = model.findSection(RobotCasesSection.class).get().getChildren().get(1);
        final RobotKeywordCall selectedCall = parentCase.getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCall);
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3");
        
        final RobotCase fstCase = section.getChildren().get(0);
        assertThat(fstCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(fstCase.getChildren(), toNames())).containsExactly("a", "b");
        assertThat(fstCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
        
        final RobotCase sndCase = section.getChildren().get(1);
        assertThat(sndCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(sndCase.getChildren(), toNames())).containsExactly("c", "call1", "call2", "d");
        assertThat(sndCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotCase trdCase = section.getChildren().get(2);
        assertThat(trdCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(trdCase.getChildren(), toNames())).containsExactly("e", "f");
        assertThat(trdCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenNestedAddingTokenIsSelected_callsArePastedAtTheEndOfParentCase() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotCase parentCase = model.findSection(RobotCasesSection.class).get().getChildren().get(1);

        final IStructuredSelection selection = selection(new AddingToken(parentCase, mock(TokenState.class)));
        handler.pasteCases(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3");
        
        final RobotCase fstCase = section.getChildren().get(0);
        assertThat(fstCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(fstCase.getChildren(), toNames())).containsExactly("a", "b");
        assertThat(fstCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
        
        final RobotCase sndCase = section.getChildren().get(1);
        assertThat(sndCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(sndCase.getChildren(), toNames())).containsExactly("c", "d", "call1", "call2");
        assertThat(sndCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotCase trdCase = section.getChildren().get(2);
        assertThat(trdCase).has(RobotCaseConditions.properlySetParent());
        assertThat(transform(trdCase.getChildren(), toNames())).containsExactly("e", "f");
        assertThat(trdCase.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenThereAreTestCasesAndCallInClipboard_onlyCasesAreInserted() {
        final RobotCase[] cases = createCasesToPaste();
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent(cases, calls);
        
        final RobotSuiteFile model = createTargetModel();
        handler.pasteCases(model, selection(), clipboard, commandsStack);
        
        assertThat(model.getSections()).hasSize(1);
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly(
                "existing case 1", "existing case 2", "existing case 3",
                "case 1", "case 2", "case 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotCaseConditions.properlySetParent());
    }

    private static IStructuredSelection selection(final Object... selectedObjects) {
        return new StructuredSelection(selectedObjects);
    }

    private static RobotSuiteFile createTargetModel() {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("existing case 1")
                .appendLine("  a  1  2")
                .appendLine("  b  3  4")
                .appendLine("existing case 2")
                .appendLine("  c  5  6")
                .appendLine("  d  7  8")
                .appendLine("existing case 3")
                .appendLine("  e  9  10")
                .appendLine("  f  11  12")
                .build();
    }

    private static RobotCase[] createCasesToPaste() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  a  1  2")
                .appendLine("  b  3  4")
                .appendLine("case 2")
                .appendLine("  c  5  6")
                .appendLine("  d  7  8")
                .appendLine("case 3")
                .appendLine("  e  9  10")
                .appendLine("  f  11  12")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren().toArray(new RobotCase[0]);
    }

    private static RobotKeywordCall[] createCallsToPaste() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case x")
                .appendLine("  call1  1  2")
                .appendLine("case y")
                .appendLine("  call2  3  4")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        final List<RobotKeywordCall> calls = new ArrayList<>();
        calls.addAll(section.getChildren().get(0).getChildren());
        calls.addAll(section.getChildren().get(1).getChildren());
        return calls.toArray(new RobotKeywordCall[0]);
    }
}
