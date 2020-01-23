/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.RowType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.InspectElementHandler.E4InspectElementHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Michal Anglart
 */
public class InspectElementHandler extends DIParameterizedHandler<E4InspectElementHandler> {

    public static final String COMMAND_ID = "org.robotframework.red.inspectElementFromSource";

    public static final String COMMAND_MODE_PARAMETER = COMMAND_ID + ".mode";

    public InspectElementHandler() {
        super(E4InspectElementHandler.class);
    }

    public static class E4InspectElementHandler {

        @Execute
        public void inspectElement(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile suiteModel) {

            final IWorkbenchPage page = editor.getSite().getPage();
            final DebugShellView view = getView(page);
            if (view != null) {
                final IDocument document = editor.getSourceEditor().getDocument();
                final ITextSelection selection = (ITextSelection) editor.getSourceEditor()
                        .getSelectionProvider()
                        .getSelection();

                inspectElement(view, suiteModel, document, selection);
            }
        }

        @VisibleForTesting
        static void inspectElement(final DebugShellView view, final RobotSuiteFile suiteModel, final IDocument document,
                final ITextSelection selection) {

            try {
                final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document,
                        suiteModel.isTsvFile(), selection.getOffset());
                if (variableRegion.isPresent()) {
                    final String var = document.get(variableRegion.get().getOffset(), variableRegion.get().getLength());

                    view.putExpression(ExpressionType.VARIABLE, var);
                    return;
                }
            } catch (final BadLocationException e) {
                // we'll look for the element
            }

            final Optional<? extends RobotElement> element = suiteModel.findElement(selection.getOffset());
            if (element.isPresent() && element.get().getClass() == RobotKeywordCall.class) {
                final RobotKeywordCall call = (RobotKeywordCall) element.get();
                final RobotExecutableRow<?> execRow = (RobotExecutableRow<?>) call.getLinkedElement();
                final IExecutableRowDescriptor<?> descriptor = execRow.buildLineDescription();
                if (descriptor.getRowType() == RowType.SIMPLE) {
                    final List<String> inspectedCall = new ArrayList<>();
                    inspectedCall.add(descriptor.getKeywordAction().getText());
                    descriptor.getKeywordArguments().stream().map(RobotToken::getText).forEach(inspectedCall::add);

                    view.putExpression(ExpressionType.ROBOT, String.join("    ", inspectedCall));
                }
            }
        }

        @VisibleForTesting
        static DebugShellView getView(final IWorkbenchPage page) {
            final DebugShellViewWrapper shellView = openDebugShellViewIfNeeded(page);
            if (shellView != null) {
                page.activate(shellView);
                return shellView.getView();
            }
            return null;
        }

        private static DebugShellViewWrapper openDebugShellViewIfNeeded(final IWorkbenchPage page) {
            final IViewPart shellViewPart = page.findView(DebugShellView.ID);
            if (shellViewPart == null) {
                try {
                    return ((DebugShellViewWrapper) page.showView(DebugShellView.ID));
                } catch (final PartInitException e) {
                    RedPlugin.logError("Unable to show Debug Shell View.", e);
                    return null;
                }
            } else {
                return (DebugShellViewWrapper) shellViewPart;
            }
        }
    }
}
