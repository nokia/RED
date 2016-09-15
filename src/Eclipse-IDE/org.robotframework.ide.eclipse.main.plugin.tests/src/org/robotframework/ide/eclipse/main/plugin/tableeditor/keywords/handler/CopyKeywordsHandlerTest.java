/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.name;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.RedClipboardMock;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CopyKeywordsHandler.E4CopyKeywordsHandler;

public class CopyKeywordsHandlerTest {

    private final E4CopyKeywordsHandler handler = new E4CopyKeywordsHandler();

    private RedClipboardMock clipboard;

    @Before
    public void beforeTest() {
        clipboard = new RedClipboardMock();
    }

    @Test
    public void nothingIsCopied_whenNothingIsSelected() {
        final IStructuredSelection selection = new StructuredSelection(newArrayList());

        final boolean copied = handler.copyKeywords(selection, clipboard);
        assertThat(copied).isFalse();
        assertThat(clipboard.isEmpty());
    }

    @Test
    public void keywordsAreCopied_whenOnlyTheyAreSelected() {
        final List<RobotKeywordDefinition> keywords = createKeywords();
        final RobotKeywordDefinition selectedKeyword1 = keywords.get(0);
        final RobotKeywordDefinition selectedKeyword2 = keywords.get(1);

        final IStructuredSelection selection = new StructuredSelection(
                newArrayList(selectedKeyword1, selectedKeyword2));

        final boolean copied = handler.copyKeywords(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getKeywordDefinitions()).hasSize(2);
        assertThat(clipboard.getKeywordDefinitions()[0]).has(nullParent()).has(noFilePositions()).has(name("kw 1"));
        assertThat(clipboard.getKeywordDefinitions()[1]).has(nullParent()).has(noFilePositions()).has(name("kw 2"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();
    }

    @Test
    public void callsAreCopied_whenOnlyTheyAreSelected() {
        final List<RobotKeywordDefinition> keywords = createKeywords();
        final RobotKeywordCall selectedCall1 = keywords.get(1).getChildren().get(0);
        final RobotKeywordCall selectedCall2 = keywords.get(1).getChildren().get(2);

        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedCall1, selectedCall2));

        final boolean copied = handler.copyKeywords(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.hasKeywordDefinitions()).isFalse();

        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("d"));
    }

    @Test
    public void keywordsAreCopied_whenBothKeywordsAndCallsAreSelected() {
        final List<RobotKeywordDefinition> keywords = createKeywords();
        final RobotKeywordDefinition selectedKeyword = keywords.get(0);
        final RobotKeywordCall selectedCall = keywords.get(0).getChildren().get(1);

        @SuppressWarnings("unchecked")
        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedKeyword, selectedCall));

        final boolean copied = handler.copyKeywords(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getKeywordDefinitions()).hasSize(1);
        assertThat(clipboard.getKeywordDefinitions()[0]).has(nullParent()).has(noFilePositions()).has(name("kw 1"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();
    }

    private static List<RobotKeywordDefinition> createKeywords() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw 1")
                .appendLine("  [tags]  tag1")
                .appendLine("  a  1")
                .appendLine("  b  2")
                .appendLine("kw 2")
                .appendLine("  [tags]  tag2")
                .appendLine("  c  3")
                .appendLine("  d  4")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren();
    }
}
