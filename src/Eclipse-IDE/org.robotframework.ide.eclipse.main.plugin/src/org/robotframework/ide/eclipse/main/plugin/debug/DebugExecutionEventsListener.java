/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.execution.LogLevel;
import org.rf.ide.core.execution.RedToAgentMessage;
import org.rf.ide.core.execution.RobotAgentEventListener;
import org.rf.ide.core.execution.Status;
import org.rf.ide.core.execution.context.KeywordPosition;
import org.rf.ide.core.execution.context.RobotDebugExecutionContext;
import org.rf.ide.core.testdata.RobotParser;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordExecutionManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;


public class DebugExecutionEventsListener implements RobotAgentEventListener {

    private final RobotDebugTarget debugTarget;

    private final RobotDebugExecutionContext executionContext;

    private final KeywordExecutionManager keywordExecutionManager;

    private boolean isStopping;

    private boolean isBreakpointConditionFulfilled;

    public DebugExecutionEventsListener(final RobotDebugTarget debugTarget, final List<IResource> suiteFilesToDebug,
            final RobotDebugExecutionContext executionContext) {
        this.debugTarget = debugTarget;

        this.executionContext = executionContext;
        this.keywordExecutionManager = new KeywordExecutionManager(suiteFilesToDebug);
    }

    @Override
    public boolean isHandlingEvents() {
        return !debugTarget.isTerminated();
    }

    @Override
    public void terminated() {
        debugTarget.terminated();
    }

    @Override
    public void handleAgentIsReadyToStart() {
        debugTarget.sendMessageToAgent(RedToAgentMessage.START_EXECUTION);
    }

    @Override
    public void handlePid() {
        debugTarget.started();
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final File suiteFilePath) {
        final IPath suitePath = Path.fromOSString(suiteFilePath.getAbsolutePath());

        final IFile currentSuiteFile = keywordExecutionManager.extractCurrentSuite(suitePath);
        if (currentSuiteFile != null) {
            final RobotSuiteFile robotSuiteFile = RedPlugin.getModelManager().createSuiteFile(currentSuiteFile);
            final RobotParser robotParser = robotSuiteFile.getProject().getEagerRobotParser();
            executionContext.startSuite(robotParser.parse(currentSuiteFile.getLocation().toFile()).get(0), robotParser);
        }
    }

    @Override
    public void handleSuiteEnded(final String suiteName, final int elapsedTime, final Status status,
            final String errorMessage) {
        debugTarget.clearStackFrames();
        executionContext.endSuite();
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        executionContext.startTest(testCaseName);
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        executionContext.endTest();
    }

    @Override
    public void handleKeywordStarted(final String name, final String type, final List<String> args) {
        checkKeywordBeforeStart(name, type, args);

        executionContext.startKeyword(name, type, args);

        String executedFileName = keywordExecutionManager.getCurrentSuiteName();

        final KeywordPosition keywordPosition = executionContext.findKeywordPosition();
        final int keywordLineNumber = keywordPosition.getLineNumber();
        final String currentResourceFile = keywordExecutionManager
                .extractCurrentResourceFile(keywordPosition.getFilePath());
        if (currentResourceFile != null) {
            executedFileName = new File(currentResourceFile).getName();
        }

        final boolean hasBreakpoint = keywordExecutionManager.hasBreakpointAtCurrentKeywordPosition(executedFileName,
                keywordLineNumber, debugTarget);

        if (shouldStopExecution(keywordLineNumber, hasBreakpoint)) {
            isStopping = true;
            resetSteppingState();
            resetStackFramesState();
        } else {
            isStopping = false;
        }

        final KeywordContext newKeywordContext = new KeywordContext(
                keywordExecutionManager.extractExecutedFileNameWithParentFolderInfo(executedFileName),
                keywordLineNumber, null);
        debugTarget.getCurrentKeywordsContextMap().put(name, newKeywordContext);
    }

    private void checkKeywordBeforeStart(final String name, final String type, final List<String> args) {
        if (keywordExecutionManager.getCurrentSuiteFile() == null
                && !executionContext.isSuiteSetupTeardownKeyword(type)) {
            String message = "Missing suite file for execution";
            if (keywordExecutionManager.getCurrentSuiteName() != null) {
                message += ", current suite name: '" + keywordExecutionManager.getCurrentSuiteName() + "'";
            }
            message += ", current keyword: '" + name + "' type='" + type + "' args=" + args;
            showError("Robot Event Dispatcher Error", message);
            throw new RobotAgentEventsListenerException(message);
        }

        if (!executionContext.isInSuite() && keywordExecutionManager.getCurrentSuiteLocation() != null) {
            handleInitFile();
        }

        if (executionContext.isTestCaseTeardownKeyword(type)) {
            debugTarget.clearStackFrames();
        }
    }

    private void handleInitFile() {
        final IFile currentInitFile = keywordExecutionManager.getCurrentInitFile();
        if (currentInitFile == null) {
            tryToFindInitSuiteFile();
        } else {
            switchSuite(currentInitFile.getParent(), currentInitFile);
        }
    }

    private void tryToFindInitSuiteFile() {
        final IContainer suiteContainer = ResourcesPlugin.getWorkspace()
                .getRoot()
                .getContainerForLocation(keywordExecutionManager.getCurrentSuiteLocation());
        if (suiteContainer != null
                && (suiteContainer.getType() == IResource.FOLDER || suiteContainer.getType() == IResource.PROJECT)) {
            final Optional<IFile> initFile = findInitSuiteFile(suiteContainer);
            if (initFile.isPresent()) {
                keywordExecutionManager.setCurrentInitFile(initFile.get());
                switchSuite(suiteContainer, initFile.get());
            }
        }
    }

    private Optional<IFile> findInitSuiteFile(final IContainer suiteContainer) {
        for (final String initFileName : Arrays.asList("__init__.robot", "__init__.txt", "__init__.tsv")) {
            final IResource member = suiteContainer.findMember(initFileName);
            if (member != null && member.getType() == IResource.FILE) {
                return Optional.of((IFile) member);
            }
        }
        return Optional.absent();
    }

    private void switchSuite(final IContainer suiteContainer, final IFile suiteFile) {
        keywordExecutionManager.setCurrentSuiteParent(suiteContainer);
        keywordExecutionManager.setCurrentSuiteName(suiteFile.getName());
        keywordExecutionManager.setCurrentSuiteFile(suiteFile);
        final RobotSuiteFile robotSuiteFile = RedPlugin.getModelManager().createSuiteFile(suiteFile);
        final RobotParser robotParser = robotSuiteFile.getProject().getEagerRobotParser();
        executionContext.startSuite(robotParser.parse(suiteFile.getLocation().toFile()).get(0), robotParser);
    }

    private void showError(final String title, final String message) {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        display.syncExec(new Runnable() {

            @Override
            public void run() {
                MessageDialog.openError(display.getActiveShell(), title, message);
            }
        });
    }

    private boolean shouldStopExecution(final int keywordLineNumber, final boolean hasBreakpoint) {
        return hasBreakpoint || (debugTarget.getRobotThread().isStepping() && !debugTarget.hasStepOver()
                && !debugTarget.hasStepReturn() && keywordLineNumber >= 0);
    }

    private void resetSteppingState() {
        if (debugTarget.getRobotThread().isStepping()) {
            debugTarget.getRobotThread().setSteppingOver(false);
            debugTarget.getRobotThread().setSteppingReturn(false);
        }
    }

    private void resetStackFramesState() {
        debugTarget.setHasStackFramesCreated(false);
    }

    @Override
    public void handleKeywordEnded(final String name, final String type) {
        debugTarget.getCurrentKeywordsContextMap().remove(name);
        executionContext.endKeyword(type);
    }

    @Override
    public void handleResourceImport(final File resourceFilePath) {
        executionContext.resourceImport(resourceFilePath);
    }

    @Override
    public void handleGlobalVariables(final Map<String, String> globalVars) {
        debugTarget.getRobotVariablesManager().setGlobalVariables(globalVars);
    }

    @Override
    public void handleVariables(final Map<String, Object> vars) {
        debugTarget.getLastKeywordFromCurrentContextMap().setVariables(vars);
        debugTarget.getRobotVariablesManager().sortVariablesNames(vars);
    }

    @Override
    public void handleLogMessage(final String msg, final LogLevel level, final String timestamp) {
    }

    @Override
    public void handleOutputFile(final File outputFilepath) {
    }

    @Override
    public void handleCheckCondition() {
        if (keywordExecutionManager.hasBreakpointCondition()) {
            debugTarget.sendMessageToAgent(RedToAgentMessage.EVALUATE_CONDITION,
                    keywordExecutionManager.getBreakpointConditionCall());
        } else {
            debugTarget.sendMessageToAgent(
                    isStopping ? RedToAgentMessage.STOP_EXECUTION : RedToAgentMessage.CONTINUE_EXECUTION);
        }
    }

    @Override
    public void handleConditionError(final String error) {
        isBreakpointConditionFulfilled = true;
        showError("Conditional Breakpoint Error", "Reason:\n" + error);
    }

    @Override
    public void handleConditionResult(final boolean result) {
        isBreakpointConditionFulfilled = result;
    }

    @Override
    public void handleConditionChecked() {
        final RedToAgentMessage message = isStopping && isBreakpointConditionFulfilled
                ? RedToAgentMessage.STOP_EXECUTION
                : RedToAgentMessage.CONTINUE_EXECUTION;
        debugTarget.sendMessageToAgent(message);
        isBreakpointConditionFulfilled = false;
        keywordExecutionManager.resetBreakpointCondition();
    }

    @Override
    public void handleClosed() {
        debugTarget.terminated();
    }

    @Override
    public void handlePaused() {
        debugTarget.suspended(DebugEvent.CLIENT_REQUEST);
    }
}
