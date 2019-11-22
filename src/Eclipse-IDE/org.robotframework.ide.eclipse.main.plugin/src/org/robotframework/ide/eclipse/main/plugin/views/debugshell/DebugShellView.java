/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.Optional;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugElement;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.ExpressionEvaluationResult.ExpressionEvaluationResultListener;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

import com.google.common.collect.Range;

public class DebugShellView {

    private static final String ECLIPSE_DEBUG_VIEW_ID = "org.eclipse.debug.ui.DebugView";
    static final String ID = "org.robotframework.ide.DebugShell";

    static Optional<RobotDebugTarget> getActiveTarget() {
        final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IViewPart debugView = activePage.findView(ECLIPSE_DEBUG_VIEW_ID);
        return Optional.ofNullable(debugView)
                .map(IViewPart::getViewSite)
                .map(IViewSite::getSelectionProvider)
                .map(ISelectionProvider::getSelection)
                .filter(IStructuredSelection.class::isInstance)
                .map(IStructuredSelection.class::cast)
                .map(IStructuredSelection::getFirstElement)
                .filter(RobotDebugElement.class::isInstance)
                .map(RobotDebugElement.class::cast)
                .map(RobotDebugElement::getDebugTarget);
    }

    private final ExprIdGenerator idGenerator = new ExprIdGenerator();
    private final Semaphore exprEvalSemaphore = new Semaphore(1, true);

    private IWorkbenchPartSite site;
    private IHandlerActivation handlerActivation;

    private SourceViewer sourceViewer;

    private AssistListener assistListener;

    private ShellDocument getDocument() {
        return (ShellDocument) sourceViewer.getDocument();
    }

    @PostConstruct
    public void postConstruct(final Composite parent, final IViewPart part) {
        GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
        GridLayoutFactory.fillDefaults().applyTo(parent);

        this.site = part.getSite();
        this.assistListener = new AssistListener();

        this.sourceViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        final StyledText styledText = sourceViewer.getTextWidget();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(styledText);
        styledText.setFont(JFaceResources.getTextFont());
        styledText.addVerifyKeyListener(this::verifyKey);
        styledText.addVerifyListener(this::verifyModification);
        styledText.addCaretListener(this::highlightBackgroundLines);

        sourceViewer.configure(new ShellSourceViewerConfig());
        sourceViewer.setDocument(new ShellDocument());
        sourceViewer.getContentAssistantFacade().addCompletionListener(assistListener);

        createActions(part);
        styledText.setCaretOffset(styledText.getCharCount());
        getDocument().addListener(() -> SwtThread.syncExec(() -> refreshAndMoveToTheEnd()));

        site.setSelectionProvider(sourceViewer);
        final IContextService service = site.getService(IContextService.class);
        service.activateContext("org.robotframework.red.view.debug.shell");
    }

    private void verifyKey(final VerifyEvent e) {
        final StyledText styledText = sourceViewer.getTextWidget();
        final int caretOffset = styledText.getCaretOffset();
        final boolean isInEditEnabledRegion = getDocument().isInEditEnabledRegion(caretOffset);

        if (e.keyCode == SWT.HOME) {
            final Optional<Integer> homeOffset = getDocument().getLineStartOffsetOmittingPrompt(caretOffset);
            if (homeOffset.isPresent()) {
                styledText.setCaretOffset(homeOffset.get().intValue());
                e.doit = false;
            }

        } else if (isInEditEnabledRegion && !getDocument().isInEditEnabledRegion(caretOffset - 1)
                && e.keyCode == SWT.BS) {
            // do not allow to use BACKSPACE when at first editable offset
            e.doit = false;

        } else if (isInEditEnabledRegion && (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
                && !assistListener.isSessionActive()) {
            if ((e.stateMask & SWT.SHIFT) != 0 && getDocument().getType() != ExpressionType.VARIABLE) {
                getDocument().continueExpressionInNewLine();
            } else {
                final EvaluationRequester requester = new EvaluationRequester(idGenerator.getNextId(),
                        exprEvalSemaphore, getDocument());
                getDocument().executeExpression(requester::requestEvaluation);
            }
            refreshAndMoveToTheEnd();

            e.doit = false;

        } else if (isInEditEnabledRegion && e.keyCode == SWT.ARROW_UP && !assistListener.isSessionActive()) {
            getDocument().switchToPreviousExpression();
            styledText.setCaretOffset(styledText.getCharCount());
            styledText.setTopIndex(getDocument().getNumberOfLines());
            e.doit = false;

        } else if (isInEditEnabledRegion && e.keyCode == SWT.ARROW_DOWN && !assistListener.isSessionActive()) {
            getDocument().switchToNextExpression();
            styledText.setCaretOffset(styledText.getCharCount());
            styledText.setTopIndex(getDocument().getNumberOfLines());
            e.doit = false;

        } else if (!isInEditEnabledRegion && isEditModifyingKey(e.keyCode)) {
            e.doit = false;

        } else if (!isInEditEnabledRegion && !isCaretNavigationKey(e.keyCode)) {
            styledText.setCaretOffset(styledText.getCharCount());
        }
    }

    private boolean isEditModifyingKey(final int keyCode) {
        return keyCode == SWT.CR || keyCode == SWT.KEYPAD_CR || keyCode == SWT.BS || keyCode == SWT.DEL
                || keyCode == SWT.CTRL || keyCode == SWT.ALT || keyCode == SWT.SHIFT || keyCode == SWT.ESC;
    }

    private boolean isCaretNavigationKey(final int keyCode) {
        return keyCode == SWT.ARROW_LEFT || keyCode == SWT.ARROW_RIGHT || keyCode == SWT.ARROW_UP
                || keyCode == SWT.ARROW_DOWN || keyCode == SWT.PAGE_UP || keyCode == SWT.PAGE_DOWN
                || keyCode == SWT.HOME || keyCode == SWT.END;
    }

    private void verifyModification(final VerifyEvent e) {
        if (getDocument().getType() == ExpressionType.ROBOT && e.end - e.start == 0 && e.text.equals("\t")) {
            // exchange tabs in ROBOT mode for separator
            e.text = ShellDocument.SEPARATOR;
            return;

        } else if (!getDocument().isInEditEnabledRegion(e.start) || !getDocument().isInEditEnabledRegion(e.end)) {
            // do not allow to modify anything in non-editable area
            e.doit = false;
            return;

        } else if (e.text.contains("\n") || e.text.contains("\r")) {
            // take only first line if adding multiline content (e.g. by pasting)
            final int firstIndexOfLf = e.text.indexOf('\n');
            final int firstIndexOfCr = e.text.indexOf('\r');
            final int firstLineEnd = firstIndexOfLf > -1 && firstIndexOfCr > -1
                    ? Math.min(firstIndexOfLf, firstIndexOfCr)
                    : Math.max(firstIndexOfLf, firstIndexOfCr);
            e.text = e.text.substring(0, firstLineEnd);
        }

        if (e.text.length() > 1) {
            if (getDocument().getType() == ExpressionType.PYTHON) {
                e.text = e.text.replaceAll("\\s*$", ""); // trim right only
            } else {
                e.text = e.text.trim();
            }
        }
    }

    private void highlightBackgroundLines(final CaretEvent e) {
        final StyledText textWidget = sourceViewer.getTextWidget();
        final RGB background = textWidget.getBackground().getRGB();
        final double factor = ColorsManager.isDarkColor(background) ? 1.2 : 0.95;

        final RGB lineBackground = ColorsManager.factorRgb(background, factor);

        final Range<Integer> range = getDocument().getLinesOfExpression(e.caretOffset);
        textWidget.setLineBackground(0, getDocument().getNumberOfLines(), null);
        textWidget.setLineBackground(range.lowerEndpoint(), range.upperEndpoint() - range.lowerEndpoint() + 1,
                ColorsManager.getColor(lineBackground));
    }

    private void createActions(final IViewPart part) {
        final ShellViewViewAction contentAssistAction = new ShellViewViewAction(sourceViewer,
                ISourceViewer.CONTENTASSIST_PROPOSALS);
        contentAssistAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        contentAssistAction.setText("Co&ntent Assist");
        contentAssistAction.setDescription("Content Assist");
        contentAssistAction.setToolTipText("Content Assist");
        contentAssistAction
                .setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ELCL_CONTENT_ASSIST));
        contentAssistAction
                .setHoverImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_LCL_CONTENT_ASSIST));
        contentAssistAction
                .setDisabledImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_DLCL_CONTENT_ASSIST));
        part.getViewSite().getActionBars().updateActionBars();

        final IHandlerService handlerService = site.getService(IHandlerService.class);
        this.handlerActivation = handlerService
                .activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new AbstractHandler() {

                    @Override
                    public Object execute(final ExecutionEvent event) {
                        contentAssistAction.run();
                        return null;
                    }
                });
    }

    @Focus
    public void onFocus() {
        sourceViewer.getTextWidget().setFocus();
    }

    @PreDestroy
    public void dispose() {
        final IHandlerService handlerService = site.getService(IHandlerService.class);
        if (handlerService != null) {
            handlerService.deactivateHandler(handlerActivation);
        }
    }

    void refreshAndMoveToTheEnd() {
        sourceViewer.refresh();
        final StyledText styledText = sourceViewer.getTextWidget();
        styledText.setCaretOffset(styledText.getCharCount());
        styledText.setTopIndex(getDocument().getNumberOfLines());
    }

    protected void clear() {
        getDocument().reset();
        refreshAndMoveToTheEnd();
    }

    protected void switchToNextMode() {
        getDocument().switchToNextMode();
        refreshAndMoveToTheEnd();
    }

    protected void putExpression(final ExpressionType type, final String expression) {
        getDocument().switchTo(type, expression);
        refreshAndMoveToTheEnd();
    }

    private static class ExprIdGenerator {

        private int id;

        private int getNextId() {
            return id++;
        }
    }

    private static class EvaluationRequester implements ExpressionEvaluationResultListener {

        private final int id;

        private final ShellDocument shellDocument;

        private final Semaphore exprEvalSemaphore;

        public EvaluationRequester(final int id, final Semaphore exprEvalSemaphore, final ShellDocument shellDocument) {
            this.id = id;
            this.exprEvalSemaphore = exprEvalSemaphore;
            this.shellDocument = shellDocument;
        }

        private int requestEvaluation(final String expression) {
            Job.createSystem("Evaluating expression", monitor -> {
                try {
                    exprEvalSemaphore.acquire();
                } catch (final InterruptedException e) {
                    // nothing to do - there will be no result
                    return;
                }

                final ExpressionEvaluationResult result = getResult();
                final Optional<RobotDebugTarget> target = SwtThread.syncEval(Evaluation.of(() -> getActiveTarget()));
                if (result != null && target.isPresent() && target.get().isSuspended()) {
                    result.addListener(id, this);

                    target.get().evaluate(id, shellDocument.getType(), expression);
                    // semaphore will be released after evaluation is done and debugger pauses again
                } else {
                    handleResult(id, ExpressionType.PYTHON, Optional.empty(),
                            Optional.of("[no suspended Robot execution is selected]"));
                    exprEvalSemaphore.release();
                }
            }).schedule();
            return id;
        }

        @Override
        public void handleResult(final int id, final ExpressionType type, final Optional<String> result,
                final Optional<String> error) {
            SwtThread.syncExec(() -> shellDocument.putEvaluationResult(id, type, result, error));
        }

        @Override
        public void evaluatorFinished() {
            exprEvalSemaphore.release();
        }

        private ExpressionEvaluationResult getResult() {
            final RobotTestExecutionService service = RedPlugin.getTestExecutionService();
            final Optional<RobotTestsLaunch> launch = service.getLastLaunch();
            if (!launch.isPresent() || launch.get().isTerminated()) {
                return null;
            }
            return launch.get().getExecutionData(ExpressionEvaluationResult.class, ExpressionEvaluationResult::new);
        }
    }

    private static class AssistListener implements ICompletionListener {

        private boolean isSessionActive = false;

        private boolean isSessionActive() {
            return isSessionActive;
        }

        @Override
        public void assistSessionStarted(final ContentAssistEvent event) {
            this.isSessionActive = true;
        }

        @Override
        public void assistSessionEnded(final ContentAssistEvent event) {
            this.isSessionActive = false;
        }

        @Override
        public void selectionChanged(final ICompletionProposal proposal, final boolean smartToggle) {
            // nothing to do
        }
    }

    private class ShellViewViewAction extends Action implements IUpdate {

        private final int operationCode;

        private final ITextOperationTarget operationTarget;

        private ShellViewViewAction(final ITextOperationTarget target, final int operationCode) {
            this.operationTarget = target;
            this.operationCode = operationCode;
            update();
        }

        @Override
        public void run() {
            operationTarget.doOperation(operationCode);
        }

        @Override
        public void update() {
            setEnabled(operationTarget.canDoOperation(operationCode));
        }
    }
}
