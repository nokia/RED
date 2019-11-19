/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.junit.Test;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.InspectElementHandler.E4InspectElementHandler;

public class InspectElementHandlerTest {

    @Test
    public void debugShellViewIsActivatedAndReturnedIfAlreadyOpen() {
        final DebugShellView view = mock(DebugShellView.class);
        
        final DebugShellViewWrapper viewWrapper = mock(DebugShellViewWrapper.class);
        when(viewWrapper.getView()).thenReturn(view);

        final IWorkbenchPage page = mock(IWorkbenchPage.class);
        when(page.findView(DebugShellView.ID)).thenReturn(viewWrapper);

        assertThat(E4InspectElementHandler.getView(page)).isSameAs(view);

        verify(page).activate(viewWrapper);
    }

    @Test
    public void debugShellViewIsOpenedAndReturnedIfNotYetOpen() throws PartInitException {
        final DebugShellView view = mock(DebugShellView.class);

        final DebugShellViewWrapper viewWrapper = mock(DebugShellViewWrapper.class);
        when(viewWrapper.getView()).thenReturn(view);

        final IWorkbenchPage page = mock(IWorkbenchPage.class);
        when(page.findView(DebugShellView.ID)).thenReturn(null);
        when(page.showView(DebugShellView.ID)).thenReturn(viewWrapper);

        assertThat(E4InspectElementHandler.getView(page)).isSameAs(view);

        verify(page).activate(viewWrapper);
    }

    @Test
    public void viewHasVariableProvided_whenVariableIsSelected() {
        final IDocument document = new Document(
                "*** Test Cases***",
                "test",
                "  Log  ${variable}",
                "  ${x}=  Set Variable  3");

        final DebugShellView view = mock(DebugShellView.class);
        E4InspectElementHandler.inspectElement(view, createModel(document), document, new TextSelection(36, 0));
        
        verify(view).putExpression(ExpressionType.VARIABLE, "${variable}");
    }

    @Test
    public void viewHasKeywordProvided_whenKeywordIsSelectedOutsideOfVariable_1() {
        final IDocument document = new Document(
                "*** Test Cases***",
                "test",
                "  Log  ${variable}",
                "  ${x}=  Set Variable  3");

        final DebugShellView view = mock(DebugShellView.class);
        E4InspectElementHandler.inspectElement(view, createModel(document), document, new TextSelection(28, 0));

        verify(view).putExpression(ExpressionType.ROBOT, "Log    ${variable}");
    }

    @Test
    public void viewHasKeywordProvided_whenKeywordIsSelectedOutsideOfVariable_2() {
        final IDocument document = new Document(
                "*** Test Cases***",
                "test",
                "  Log  ${variable}",
                "  ${x}=  Set Variable  3");

        final DebugShellView view = mock(DebugShellView.class);
        E4InspectElementHandler.inspectElement(view, createModel(document), document, new TextSelection(56, 0));

        verify(view).putExpression(ExpressionType.ROBOT, "Set Variable    3");
    }

    @Test
    public void nothingIsProvided_whenSelectedElementIsNotACall() {
        final IDocument document = new Document(
                "*** Test Cases***",
                "test",
                "  Log  ${variable}",
                "  ${x}=  Set Variable  3");

        final DebugShellView view = mock(DebugShellView.class);
        E4InspectElementHandler.inspectElement(view, createModel(document), document, new TextSelection(5, 0));

        verifyZeroInteractions(view);
    }

    private RobotSuiteFile createModel(final IDocument document) {
        final RobotSuiteFileCreator creator = new RobotSuiteFileCreator();
        for (final String line : document.get().split("\\r?\\n")) {
            creator.appendLine(line);
        }
        return creator.build();
    }
}
