/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.execution.ExecutionElementsParser;
import org.rf.ide.core.execution.context.RobotDebugExecutionContext;
import org.rf.ide.core.execution.context.RobotDebugExecutionContext.KeywordPosition;
import org.rf.ide.core.testdata.RobotParser;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordExecutionManager;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

/**
 * Listens to events from the TestRunnerAgent and fires corresponding
 * debug events.
 * 
 * @author mmarzec
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.TooManyMethods" })
public class RobotDebugEventDispatcher extends Job {

    private final RobotDebugTarget target;
    
    private final RobotEventBroker robotEventBroker;

    private boolean isStopping;
    
    private boolean isBreakpointConditionFulfilled;
    
    private final RobotDebugExecutionContext executionContext;
    
    private final KeywordExecutionManager keywordExecutionManager;
    
    private final RobotConsoleFacade remoteDebugConsole;
    
    public RobotDebugEventDispatcher(final RobotDebugTarget target, final List<IResource> suiteFilesToDebug,
            final RobotEventBroker robotEventBroker, final RobotConsoleFacade consoleFacade) {
        super("Robot Event Dispatcher");
        setSystem(true);

        this.target = target;
        this.robotEventBroker = robotEventBroker;
        this.remoteDebugConsole = consoleFacade;

        executionContext = new RobotDebugExecutionContext();
        keywordExecutionManager = new KeywordExecutionManager(suiteFilesToDebug);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            final BufferedReader eventReader = target.getEventReader();
            if (eventReader != null) {
                runEventsLoop(eventReader);
            }
        } catch (final IOException | MissingFileToExecuteException e) {
            target.terminated();
        }
        return Status.OK_STATUS;
    }

    private void runEventsLoop(final BufferedReader eventReader) throws IOException, MissingFileToExecuteException {
        String event = eventReader.readLine();
        final ObjectMapper mapper = new ObjectMapper();
        while (!target.isTerminated() && event != null) {
            
            final TypeReference<Map<String, Object>> stringToObjectMapType = new TypeReference<Map<String, Object>>() {};
            final Map<String, Object> eventMap = mapper.readValue(event, stringToObjectMapType);
            final String eventType = getEventType(eventMap);
            if (eventType == null) {
                continue;
            }

            switch (eventType) {
                case "pid":
                    handlePidEvent();
                    break;
                case "resource_import": //since Robot 2.9
                    handleResourceImportEvent(eventMap);
                    break;
                case "start_suite":
                    handleStartSuiteEvent(eventMap);
                    break;
                case "start_test":
                    handleStartTestEvent(eventMap);
                    break;
                case "start_keyword":
                    handleStartKeywordEvent(eventMap);
                    break;
                case "vars":
                    handleVarsEvent(eventMap);
                    break;
                case "global_vars":
                    handleGlobalVarsEvent(eventMap);
                    break;
                case "check_condition":
                    handleCheckConditionEvent();
                    break;
                case "condition_result":
                    handleConditionResultEvent(eventMap);
                    break;
                case "condition_error":
                    handleConditionErrorEvent(eventMap);
                    break;
                case "condition_checked":
                    handleConditionCheckedEvent();
                    break;
                case "paused":
                    handlePausedEvent();
                    break;
                case "end_keyword":
                    handleEndKeywordEvent(eventMap);
                    break;
                case "end_test":
                    handleEndTestEvent(eventMap);
                    break;
                case "end_suite":
                    handleEndSuiteEvent(eventMap);
                    break;
                case "close":
                    handleCloseEvent();
                    break;
                case "log_message":
                    handleLogMessageEvent(eventMap);
                    break;
                case "output_file":
                    handleOutputFile(eventMap);
                    break;
                case "error":
                    break;
                default:
                    break;
            }

            event = eventReader.readLine();
        }
    }

    private void handlePidEvent() {
        robotEventBroker.sendClearEventToMessageLogView();
        target.started();
    }
    
    private void handleResourceImportEvent(final Map<String, ?> eventMap) {
        final List<?> importList = (List<?>) eventMap.get("resource_import");
        final Map<?, ?> importElements = (Map<?, ?>) importList.get(1);
        final String resourceFilePath = (String) importElements.get("source");
        executionContext.resourceImport(resourceFilePath);
    }

    private void handleStartSuiteEvent(final Map<String, ?> eventMap) {
        final List<?> suiteList = (List<?>) eventMap.get("start_suite");
        final Map<?, ?> suiteElements = (Map<?, ?>) suiteList.get(1);
        final IPath suiteFilePath = new Path((String) suiteElements.get("source"));
        printRemoteDebugSuiteMessage(suiteFilePath);
        
        final IFile currentSuiteFile = keywordExecutionManager.extractCurrentSuite(suiteFilePath);
        
        if (currentSuiteFile != null) {
            final RobotSuiteFile robotSuiteFile = RedPlugin.getModelManager().createSuiteFile(currentSuiteFile);
            final RobotParser robotParser = robotSuiteFile.getProject().getEagerRobotParser();
            executionContext.startSuite(robotParser.parse(currentSuiteFile.getLocation().toFile()).get(0), robotParser);
        }

        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createStartSuiteExecutionElement(
                (String) suiteList.get(0), (String) suiteElements.get("source")));
    }
    
    private void handleStartTestEvent(final Map<String, ?> eventMap) {
        final List<?> testList = (List<?>) eventMap.get("start_test");
        final Map<?, ?> testElements = (Map<?, ?>) testList.get(1);
        final String line = "Starting test: " + testElements.get("longname") + '\n';
        final String testCaseName = (String) testList.get(0);
        
        final boolean hasTestCase = executionContext.startTest(testCaseName);
        printRemoteDebugTestCaseMessage(testCaseName, hasTestCase);
        
        robotEventBroker.sendAppendLineEventToMessageLogView(line);
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createStartTestExecutionElement(testCaseName));
    }
    
    private void handleStartKeywordEvent(final Map<String, ?> eventMap) {
        final List<?> startList = (List<?>) eventMap.get("start_keyword");
        final String currentKeywordName = (String) startList.get(0);
        final Map<?, ?> startElements = (Map<?, ?>) startList.get(1);
        final String keywordType = (String) startElements.get("type");
        if (keywordExecutionManager.getCurrentSuiteFile() == null
                && !executionContext.isSuiteSetupTeardownKeyword(keywordType)) {
            throw new MissingFileToExecuteException("Missing suite file for execution");
        }
        if (executionContext.isTestCaseTeardownKeyword(keywordType)) {
            target.clearStackFrames();
        }

        executionContext.startKeyword(currentKeywordName, keywordType, (List<String>) startElements.get("args"));

        String executedFileName = keywordExecutionManager.getCurrentSuiteName();

        final KeywordPosition keywordPosition = executionContext.findKeywordPosition();
        final int keywordLineNumber = keywordPosition.getLineNumber();
        final String currentResourceFile = keywordExecutionManager.extractCurrentResourceFile(keywordPosition.getFilePath());
        if (currentResourceFile != null) {
            executedFileName = new File(currentResourceFile).getName();
        }

        final boolean hasBreakpoint = keywordExecutionManager.hasBreakpointAtCurrentKeywordPosition(executedFileName,
                keywordLineNumber, target);

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
        target.getCurrentKeywordsContextMap().put(currentKeywordName, newKeywordContext);
    }

    private boolean shouldStopExecution(final int keywordLineNumber, final boolean hasBreakpoint) {
        return hasBreakpoint || (target.getRobotThread().isStepping() && !target.hasStepOver()
                && !target.hasStepReturn() && keywordLineNumber >= 0);
    }
    
    @SuppressWarnings("unchecked")
    private void handleVarsEvent(final Map<String, ?> eventMap) {
        final List<?> varList = (List<?>) eventMap.get("vars");
        final Map<?, ?> vars = (Map<?, ?>) varList.get(1);
        target.getLastKeywordFromCurrentContextMap().setVariables((Map<String, Object>) vars);
        target.getRobotVariablesManager().sortVariablesNames((Map<String, Object>) vars);
    }

    @SuppressWarnings("unchecked")
    private void handleGlobalVarsEvent(final Map<String, ?> eventMap) {
        final List<?> globalVarList = (List<?>) eventMap.get("global_vars");
        final Map<?, ?> globalVars = (Map<?, ?>) globalVarList.get(1);
        target.getRobotVariablesManager().setGlobalVariables((Map<String, String>) globalVars);
    }

    private void handleCheckConditionEvent() {
        if (!keywordExecutionManager.getBreakpointCondition().isEmpty()) {
            target.sendEventToAgent(keywordExecutionManager.createJsonFromBreakpointCondition());
        } else {
            target.sendExecutionEventToAgent(isStopping ? ExecutionEvent.STOP_EXECUTION
                    : ExecutionEvent.CONTINUE_EXECUTION);
        }
    }

    private void handleConditionResultEvent(final Map<String, ?> eventMap) {
        final List<?> resultList = (List<?>) eventMap.get("condition_result");
        final Object result = resultList.get(0);
        if (result instanceof Boolean) {
            isBreakpointConditionFulfilled = (Boolean) result;
        }
    }

    private void handleConditionErrorEvent(final Map<String, ?> eventMap) {
        isBreakpointConditionFulfilled = true;
        final List<?> errorList = (List<?>) eventMap.get("condition_error");
        final Display display = PlatformUI.getWorkbench().getDisplay();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(display.getActiveShell(), "Conditional Breakpoint Error", "Reason:\n"
                        + errorList.get(0));
            }
        });
    }

    private void handleConditionCheckedEvent() {
        if (isStopping && isBreakpointConditionFulfilled) {
            target.sendExecutionEventToAgent(ExecutionEvent.STOP_EXECUTION);
        } else {
            target.sendExecutionEventToAgent(ExecutionEvent.CONTINUE_EXECUTION);
        }
        resetBreakpointConditionState();
    }

    private void handlePausedEvent() {
        target.suspended(DebugEvent.CLIENT_REQUEST);
    }

    private void handleEndKeywordEvent(final Map<String, ?> eventMap) {
        final List<?> endList = (List<?>) eventMap.get("end_keyword");
        final String keyword = (String) endList.get(0);
        target.getCurrentKeywordsContextMap().remove(keyword);
        
        final Map<?, ?> endElements = (Map<?, ?>) endList.get(1);
        final String keywordType = (String) endElements.get("type");
        executionContext.endKeyword(keywordType);
    }

    private void handleEndTestEvent(final Map<String, ?> eventMap) {
        final List<?> testList = (List<?>) eventMap.get("end_test");
        final Map<?, ?> testElements = (Map<?, ?>) testList.get(1);
        
        executionContext.endTest();
        
        final String line = "Ending test: " + testElements.get("longname") + "\n\n";
        robotEventBroker.sendAppendLineEventToMessageLogView(line);
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createEndTestExecutionElement(
                (String) testList.get(0), testElements));
    }
    
    private void handleEndSuiteEvent(final Map<String, ?> eventMap) {
        final List<?> suiteList = (List<?>) eventMap.get("end_suite");
        target.clearStackFrames();
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createEndSuiteExecutionElement(
                (String) suiteList.get(0), (Map<?, ?>) suiteList.get(1)));
        executionContext.endSuite();
    }


    private void handleCloseEvent() {
        target.terminated();
    }

    private void handleLogMessageEvent(final Map<String, ?> eventMap) {
        final List<?> messageList = (List<?>) eventMap.get("log_message");
        final Map<?, ?> messageElements = (Map<?, ?>) messageList.get(0);
        final String line = messageElements.get("timestamp") + " : " + messageElements.get("level") + " : "
                + messageElements.get("message") + '\n';
        robotEventBroker.sendAppendLineEventToMessageLogView(line);
    }
    
    private void handleOutputFile(final Map<?, ?> eventMap) {
        final List<?> outputFileList = (List<?>) eventMap.get("output_file");
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createOutputFileExecutionElement((String) outputFileList.get(0)));
    }

    private String getEventType(final Map<String, ?> eventMap) {
        if (eventMap == null) {
            return null;
        }
        final Set<String> keySet = eventMap.keySet();
        return keySet.isEmpty() ? null : keySet.iterator().next();
    }
    
    private void resetSteppingState() {
        if (target.getRobotThread().isStepping()) {
            target.getRobotThread().setSteppingOver(false);
            target.getRobotThread().setSteppingReturn(false);
        }
    }
    
    private void resetStackFramesState() {
        target.setHasStackFramesCreated(false);
    }
    
    private void resetBreakpointConditionState() {
        isBreakpointConditionFulfilled = false;
        keywordExecutionManager.setBreakpointCondition("");
    }
    
    private void printRemoteDebugSuiteMessage(final IPath suiteFilePath) {
        try {
            if (suiteFilePath != null && suiteFilePath.getFileExtension() != null) {
                remoteDebugConsole.writeLine("Debugging test suite: " + suiteFilePath.toOSString());
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    private void printRemoteDebugTestCaseMessage(final String testCaseName, final boolean isTestCaseAvailable) {
        if (!isTestCaseAvailable) {
            try {
                remoteDebugConsole
                        .writeLine("Test case \"" + testCaseName + "\" not available. Check the files content!");
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static class MissingFileToExecuteException extends RuntimeException {

        public MissingFileToExecuteException(final String message) {
            super(message);
        }
    }
    
    public static enum ExecutionEvent {
        CONTINUE_EXECUTION("continue"),
        STOP_EXECUTION("stop"),
        RESUME_EXECUTION("resume"),
        INTERRUPT_EXECUTION("interrupt");

        private final String message;

        private ExecutionEvent(final String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
