/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCallConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinitionConditions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.PasteKeywordsHandler.E4PasteKeywordsHandler;

public class PasteKeywordsHandlerTest {

    private final E4PasteKeywordsHandler handler = new E4PasteKeywordsHandler();

    private final RedClipboard clipboard = new RedClipboardMock();

    private final RobotEditorCommandsStack commandsStack = new RobotEditorCommandsStack();

    @Before
    public void beforeTest() {
        clipboard.clear();
        commandsStack.clear();
    }

    @Test
    public void whenNoKeywordSectionExist_itIsCreatedForPastedKeywords() {
        final RobotKeywordDefinition[] keywords = createKeywordsToPaste();
        clipboard.insertContent((Object) keywords);

        final RobotSuiteFile emptyModel = new RobotSuiteFileCreator().build();
        handler.pasteKeywords(emptyModel, selection(), clipboard, commandsStack);

        assertThat(emptyModel.getSections()).hasSize(1);
        final RobotKeywordsSection section = emptyModel.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenSelectionIsEmpty_keywordsArePastedAtTheEndOfSection() {
        final RobotKeywordDefinition[] keywords = createKeywordsToPaste();
        clipboard.insertContent((Object) keywords);

        final RobotSuiteFile model = createTargetModel();
        handler.pasteKeywords(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3", "kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenNonNestedAddingTokenIsSelected_keywordsArePastedAtTheEndOfSection() {
        final RobotKeywordDefinition[] keywords = createKeywordsToPaste();
        clipboard.insertContent((Object) keywords);

        final RobotSuiteFile model = createTargetModel();
        final IStructuredSelection selection = selection(new AddingToken(null, mock(TokenState.class)));
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3", "kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenKeywordIsSelected_keywordsArePastedBeforeIt() {
        final RobotKeywordDefinition[] keywords = createKeywordsToPaste();
        clipboard.insertContent((Object) keywords);

        final RobotSuiteFile model = createTargetModel();
        final RobotKeywordDefinition selectedKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1);

        final IStructuredSelection selection = selection(selectedKeyword);
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "kw 1", "kw 2", "kw 3",
                "existing kw 2", "existing kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenKeywordCallIsSelected_keywordsArePastedBeforeItsParent() {
        final RobotKeywordDefinition[] keywords = createKeywordsToPaste();
        clipboard.insertContent((Object) keywords);

        final RobotSuiteFile model = createTargetModel();
        final RobotKeywordDefinition parentKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1);
        final RobotKeywordCall selectedCall = parentKeyword.getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCall);
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "kw 1", "kw 2", "kw 3",
                "existing kw 2", "existing kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenNestedAddingTokenIsSelected_keywordsArePastedBeforeItsParent() {
        final RobotKeywordDefinition[] keywords = createKeywordsToPaste();
        clipboard.insertContent((Object) keywords);

        final RobotSuiteFile model = createTargetModel();
        final RobotKeywordDefinition tokenParent = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1);

        final IStructuredSelection selection = selection(new AddingToken(tokenParent, mock(TokenState.class)));
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "kw 1", "kw 2", "kw 3",
                "existing kw 2", "existing kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenNoKeywordsSectionExist_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile emptyModel = new RobotSuiteFileCreator().build();

        handler.pasteKeywords(emptyModel, selection(), clipboard, commandsStack);

        assertThat(emptyModel.getSections()).isEmpty();
    }

    @Test
    public void whenSelectionIsEmpty_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();

        handler.pasteKeywords(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenNonNestedAddingTokenIsSelected_noCallsArePastedAndNothingChanges() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();

        final IStructuredSelection selection = selection(new AddingToken(null, mock(TokenState.class)));
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    @Test
    public void whenKeywordIsSelected_callsArePastedAtTheEndOfIt() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotKeywordDefinition selectedKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1);

        final IStructuredSelection selection = selection(selectedKeyword);
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3");

        final RobotKeywordDefinition fstKeyword = section.getChildren().get(0);
        assertThat(fstKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(fstKeyword.getChildren(), toNames())).containsExactly("a", "b");
        assertThat(fstKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotKeywordDefinition sndKeyword = section.getChildren().get(1);
        assertThat(sndKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(sndKeyword.getChildren(), toNames())).containsExactly("c", "d", "call1", "call2");
        assertThat(sndKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotKeywordDefinition trdKeyword = section.getChildren().get(2);
        assertThat(trdKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(trdKeyword.getChildren(), toNames())).containsExactly("e", "f");
        assertThat(trdKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenKeywordCallIsSelected_callsArePastedBeforeIt() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotKeywordDefinition parentKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1);
        final RobotKeywordCall selectedCall = parentKeyword.getChildren().get(1);

        final IStructuredSelection selection = selection(selectedCall);
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3");

        final RobotKeywordDefinition fstKeyword = section.getChildren().get(0);
        assertThat(fstKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(fstKeyword.getChildren(), toNames())).containsExactly("a", "b");
        assertThat(fstKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotKeywordDefinition sndKeyword = section.getChildren().get(1);
        assertThat(sndKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(sndKeyword.getChildren(), toNames())).containsExactly("c", "call1", "call2", "d");
        assertThat(sndKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotKeywordDefinition trdKeyword = section.getChildren().get(2);
        assertThat(trdKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(trdKeyword.getChildren(), toNames())).containsExactly("e", "f");
        assertThat(trdKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenNestedAddingTokenIsSelected_callsArePastedAtTheEndOfParentKeyword() {
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent((Object) calls);

        final RobotSuiteFile model = createTargetModel();
        final RobotKeywordDefinition parentKeyword = model.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1);

        final IStructuredSelection selection = selection(new AddingToken(parentKeyword, mock(TokenState.class)));
        handler.pasteKeywords(model, selection, clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3");

        final RobotKeywordDefinition fstKeyword = section.getChildren().get(0);
        assertThat(fstKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(fstKeyword.getChildren(), toNames())).containsExactly("a", "b");
        assertThat(fstKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotKeywordDefinition sndKeyword = section.getChildren().get(1);
        assertThat(sndKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(sndKeyword.getChildren(), toNames())).containsExactly("c", "d", "call1", "call2");
        assertThat(sndKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());

        final RobotKeywordDefinition trdKeyword = section.getChildren().get(2);
        assertThat(trdKeyword).has(RobotKeywordDefinitionConditions.properlySetParent());
        assertThat(transform(trdKeyword.getChildren(), toNames())).containsExactly("e", "f");
        assertThat(trdKeyword.getChildren()).have(RobotKeywordCallConditions.properlySetParent());
    }

    @Test
    public void whenThereAreKeywordsAndCallsInClipboard_onlyKeywordsAreInserted() {
        final RobotKeywordDefinition[] keywords = createKeywordsToPaste();
        final RobotKeywordCall[] calls = createCallsToPaste();
        clipboard.insertContent(keywords, calls);

        final RobotSuiteFile model = createTargetModel();
        handler.pasteKeywords(model, selection(), clipboard, commandsStack);

        assertThat(model.getSections()).hasSize(1);
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        assertThat(transform(section.getChildren(), toNames())).containsExactly("existing kw 1", "existing kw 2",
                "existing kw 3", "kw 1", "kw 2", "kw 3");
        assertThat(section.getChildren()).have(children(2)).have(RobotKeywordDefinitionConditions.properlySetParent());
    }

    private static IStructuredSelection selection(final Object... selectedObjects) {
        return new StructuredSelection(selectedObjects);
    }

    private static RobotSuiteFile createTargetModel() {
        return new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("existing kw 1")
                .appendLine("  a  1  2")
                .appendLine("  b  3  4")
                .appendLine("existing kw 2")
                .appendLine("  c  5  6")
                .appendLine("  d  7  8")
                .appendLine("existing kw 3")
                .appendLine("  e  9  10")
                .appendLine("  f  11  12")
                .build();
    }

    private static RobotKeywordDefinition[] createKeywordsToPaste() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  a  1  2")
                .appendLine("  b  3  4")
                .appendLine("kw 2")
                .appendLine("  c  5  6")
                .appendLine("  d  7  8")
                .appendLine("kw 3")
                .appendLine("  e  9  10")
                .appendLine("  f  11  12")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().toArray(new RobotKeywordDefinition[0]);
    }

    private static RobotKeywordCall[] createCallsToPaste() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw x")
                .appendLine("  call1  1  2")
                .appendLine("kw y")
                .appendLine("  call2  3  4")
                .build();
        final RobotKeywordsSection section = model.findSection(RobotKeywordsSection.class).get();
        final List<RobotKeywordCall> calls = new ArrayList<>();
        calls.addAll(section.getChildren().get(0).getChildren());
        calls.addAll(section.getChildren().get(1).getChildren());
        return calls.toArray(new RobotKeywordCall[0]);
    }
}
