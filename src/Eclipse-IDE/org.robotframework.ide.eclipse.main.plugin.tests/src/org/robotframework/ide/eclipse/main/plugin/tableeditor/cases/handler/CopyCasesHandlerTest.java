/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyCasesHandler.E4CopyCasesHandler;

public class CopyCasesHandlerTest {

    private final E4CopyCasesHandler handler = new E4CopyCasesHandler();

    private RedClipboardMock clipboard;

    @Before
    public void beforeTest() {
        clipboard = new RedClipboardMock();
    }

    @Test
    public void nothingIsCopied_whenNothingIsSelected() {
        final IStructuredSelection selection = new StructuredSelection(newArrayList());

        final boolean copied = handler.copyCases(selection, clipboard);
        assertThat(copied).isFalse();
        assertThat(clipboard.isEmpty());
    }

    @Test
    public void casesAreCopied_whenOnlyTheyAreSelected() {
        final List<RobotCase> cases = createCases();
        final RobotCase selectedCase1 = cases.get(0);
        final RobotCase selectedCase2 = cases.get(1);

        final IStructuredSelection selection = new StructuredSelection(
                newArrayList(selectedCase1, selectedCase2));

        final boolean copied = handler.copyCases(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getCases()).hasSize(2);
        assertThat(clipboard.getCases()[0]).has(nullParent()).has(noFilePositions()).has(name("case 1"));
        assertThat(clipboard.getCases()[1]).has(nullParent()).has(noFilePositions()).has(name("case 2"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();
    }

    @Test
    public void callsAreCopied_whenOnlyTheyAreSelected() {
        final List<RobotCase> cases = createCases();
        final RobotKeywordCall selectedCall1 = cases.get(1).getChildren().get(0);
        final RobotKeywordCall selectedCall2 = cases.get(1).getChildren().get(2);

        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedCall1, selectedCall2));

        final boolean copied = handler.copyCases(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.hasKeywordDefinitions()).isFalse();

        assertThat(clipboard.getKeywordCalls()).hasSize(2);
        assertThat(clipboard.getKeywordCalls()[0]).has(nullParent()).has(noFilePositions()).has(name("tags"));
        assertThat(clipboard.getKeywordCalls()[1]).has(nullParent()).has(noFilePositions()).has(name("d"));
    }

    @Test
    public void casesAreCopied_whenBothKeywordsAndCallsAreSelected() {
        final List<RobotCase> cases = createCases();
        final RobotCase selectedCase = cases.get(0);
        final RobotKeywordCall selectedCall = cases.get(0).getChildren().get(1);

        @SuppressWarnings("unchecked")
        final IStructuredSelection selection = new StructuredSelection(newArrayList(selectedCase, selectedCall));

        final boolean copied = handler.copyCases(selection, clipboard);
        assertThat(copied).isTrue();

        assertThat(clipboard.getCases()).hasSize(1);
        assertThat(clipboard.getCases()[0]).has(nullParent()).has(noFilePositions()).has(name("case 1"));

        assertThat(clipboard.hasKeywordCalls()).isFalse();
    }

    private static List<RobotCase> createCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [tags]  tag1")
                .appendLine("  a  1")
                .appendLine("  b  2")
                .appendLine("case 2")
                .appendLine("  [tags]  tag2")
                .appendLine("  c  3")
                .appendLine("  d  4")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren();
    }
}
