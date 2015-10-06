/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.core.execution.ExecutionElementsParser;
import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext;
import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.KeywordPosition;
import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugElement;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Joiner;

/**
 * Listens to events from the TestRunnerAgent and fires corresponding
 * debug events.
 * 
 * @author mmarzec
 */
public class RobotDebugEventDispatcher extends Job {

    private final RobotDebugTarget target;

    private IFile executedFile;
    
    private final RobotEventBroker robotEventBroker;
    
    private final List<IResource> suiteResources;
    
    private final Map<IBreakpoint, Integer> breakpointHitCounts;

    private String currentSuite = "";

    private String breakpointCondition = "";
    
    private String currentResourceFile;

    private boolean isBreakpointConditionFulfilled;

    private boolean isStopping;
    
    private KeywordContext currentKeywordContext = new KeywordContext();
    
    private final RobotDebugExecutionContext executionContext;
    
    public RobotDebugEventDispatcher(final RobotDebugTarget target, final List<IResource> suiteResources,
            final RobotEventBroker robotEventBroker) {
        super("Robot Event Dispatcher");
        setSystem(true);

        this.target = target;
        this.suiteResources = suiteResources;
        this.robotEventBroker = robotEventBroker;

        breakpointHitCounts = new LinkedHashMap<>();
        executionContext = new RobotDebugExecutionContext();
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

            final Map<?, ?> eventMap = mapper.readValue(event, Map.class);
            final String eventType = getEventType(eventMap);
            if (eventType == null) {
                continue;
            }

            switch (eventType) {
                case "pid":
                    handlePidEvent();
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

    private void handleStartSuiteEvent(final Map<?, ?> eventMap) {
        final List<?> suiteList = (List<?>) eventMap.get("start_suite");
        final Map<?, ?> suiteElements = (Map<?, ?>) suiteList.get(1);
        currentSuite = new File((String) suiteElements.get("source")).getName();
        executedFile = extractSuiteFile(currentSuite, suiteResources);
        
        if (executedFile != null) {
            final RobotSuiteFile robotSuiteFile = RedPlugin.getModelManager().createSuiteFile(executedFile);
            final RobotParser robotParser = robotSuiteFile.getProject().getEagerRobotParser();
            executionContext.startSuite(robotParser.parse(executedFile.getLocation().toFile()).get(0));
        }

        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createStartSuiteExecutionElement(
                (String) suiteList.get(0), (String) suiteElements.get("source")));
    }

    private void handleStartTestEvent(final Map<?, ?> eventMap) {
        final List<?> testList = (List<?>) eventMap.get("start_test");
        final Map<?, ?> testElements = (Map<?, ?>) testList.get(1);
        final String line = "Starting test: " + testElements.get("longname") + '\n';
        final String testCaseName = (String) testList.get(0);
        
        executionContext.startTest(testCaseName);
        
        robotEventBroker.sendAppendLineEventToMessageLogView(line);
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createStartTestExecutionElement(testCaseName));
    }
    
    private void handleStartKeywordEvent(final Map<?, ?> eventMap) {
        if (executedFile == null) {
            throw new MissingFileToExecuteException("Missing suite file for execution");
        }
        final List<?> startList = (List<?>) eventMap.get("start_keyword");
        final String currentKeyword = (String) startList.get(0);
        final Map<?, ?> startElements = (Map<?, ?>) startList.get(1);
        final String keywordType = (String) startElements.get("type");
        if(keywordType.equals(RobotDebugExecutionContext.TEARDOWN_KEYWORD_TYPE)) {
            target.clearStackFrames();
        }
        
        executionContext.startKeyword(currentKeyword, keywordType, (List<String>) startElements.get("args"));

        String executedSuite = currentSuite;
        
        final KeywordPosition keywordPosition = executionContext.findKeywordPosition();
        final int keywordLineNumber = keywordPosition.getLineNumber();
        currentResourceFile = keywordPosition.getFilePath();
        if(currentResourceFile != null) {
            executedSuite = new File(currentResourceFile).getName();
        }

        boolean hasBreakpoint = hasBreakpointAtCurrentKeywordPosition(executedSuite, keywordLineNumber);

        if (hasBreakpoint || (target.getRobotThread().isStepping() && !target.hasStepOver()
                && !target.hasStepReturn() && keywordLineNumber >= 0)) {

            if (target.getRobotThread().isStepping()) {
                target.getRobotThread().setSteppingOver(false);
                target.getRobotThread().setSteppingReturn(false);
            }
            target.setHasStackFramesCreated(false);
            isStopping = true;
        } else {
            isStopping = false;
        }

        currentKeywordContext = new KeywordContext(null, executedSuite, keywordLineNumber);
        target.getPartListener().setKeywordContext(currentKeywordContext);
        target.getCurrentFrames().put(currentKeyword, currentKeywordContext);
    }

    @SuppressWarnings("unchecked")
    private void handleVarsEvent(final Map<?, ?> eventMap) {
        final List<?> varList = (List<?>) eventMap.get("vars");
        final Map<?, ?> vars = (Map<?, ?>) varList.get(1);
        target.getLastKeywordFromCurrentFrames().setVariables((Map<String, Object>) vars);
        target.getRobotVariablesManager().sortVariablesNames((Map<String, Object>) vars);
    }

    @SuppressWarnings("unchecked")
    private void handleGlobalVarsEvent(final Map<?, ?> eventMap) {
        final List<?> globalVarList = (List<?>) eventMap.get("global_vars");
        final Map<?, ?> globalVars = (Map<?, ?>) globalVarList.get(1);
        target.getRobotVariablesManager().setGlobalVariables((Map<String, String>) globalVars);
    }

    private void handleCheckConditionEvent() {
        if (!breakpointCondition.isEmpty()) {
            target.sendEventToAgent(createJsonFromBreakpointCondition(breakpointCondition));
        } else {
            if (isStopping) {
                target.sendEventToAgent("stop");
                robotEventBroker.sendHighlightLineEventToTextEditor(currentKeywordContext.getFileName(),
                        currentKeywordContext.getLineNumber(), currentKeywordContext.getVariables());
            } else {
                target.sendEventToAgent("run");
            }
        }
    }

    private String createJsonFromBreakpointCondition(final String condition) {
        // two or more spaces or tab
        final List<String> conditionElements = newArrayList(condition.split("(\\s{2,}|\t)"));
        if (conditionElements.isEmpty()) {
            return "{\"keywordCondition\":[]}";
        }

        final String keywordName = conditionElements.remove(0);
        return "{\"keywordCondition\":[\"" + keywordName + "\", [\"" + Joiner.on("\", \"").join(conditionElements)
                + "\"]]}";
    }

    private void handleConditionResultEvent(final Map<?, ?> eventMap) {
        final List<?> resultList = (List<?>) eventMap.get("condition_result");
        final Object result = resultList.get(0);
        if (result instanceof Boolean) {
            isBreakpointConditionFulfilled = (Boolean) result;
        }
    }

    private void handleConditionErrorEvent(final Map<?, ?> eventMap) {
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
            target.sendEventToAgent("stop");
            robotEventBroker.sendHighlightLineEventToTextEditor(currentKeywordContext.getFileName(),
                    currentKeywordContext.getLineNumber(), currentKeywordContext.getVariables());
        } else {
            target.sendEventToAgent("run");
        }
        isBreakpointConditionFulfilled = false;
        breakpointCondition = "";
    }

    private void handlePausedEvent() {
        target.suspended(DebugEvent.CLIENT_REQUEST);
        target.getRobotVariablesManager().setIsVariablesViewerUpdated(false);
        target.getRobotVariablesManager().addVariablesViewerListener();
    }

    private void handleEndKeywordEvent(final Map<?, ?> eventMap) {
        final List<?> endList = (List<?>) eventMap.get("end_keyword");
        final String keyword = (String) endList.get(0);
        target.getCurrentFrames().remove(keyword);
        
        executionContext.endKeyword();

        if (currentResourceFile != null) {
            robotEventBroker.sendClearEventToTextEditor(currentResourceFile);
        }
    }

    private void handleEndTestEvent(final Map<?, ?> eventMap) {
        final List<?> testList = (List<?>) eventMap.get("end_test");
        final Map<?, ?> testElements = (Map<?, ?>) testList.get(1);
        
        executionContext.endTest();
        
        final String line = "Ending test: " + testElements.get("longname") + "\n\n";
        robotEventBroker.sendAppendLineEventToMessageLogView(line);
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createEndTestExecutionElement(
                (String) testList.get(0), testElements));
    }
    
    private void handleEndSuiteEvent(final Map<?, ?> eventMap) {
        final List<?> suiteList = (List<?>) eventMap.get("end_suite");
        target.clearStackFrames();
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createEndSuiteExecutionElement(
                (String) suiteList.get(0), (Map<?, ?>) suiteList.get(1)));
    }


    private void handleCloseEvent() {
        robotEventBroker.sendClearAllEventToTextEditor();
        target.getRobotVariablesManager().removeVariablesViewerListener();
        target.terminated();
    }

    private void handleLogMessageEvent(final Map<?, ?> eventMap) {
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

    private String getEventType(final Map<?, ?> eventMap) {
        if (eventMap == null) {
            return null;
        }
        final Set<?> keySet = eventMap.keySet();
        if (!keySet.isEmpty()) {
            return (String) keySet.iterator().next();
        }
        return null;
    }
    
    private IFile extractSuiteFile(final String suiteName, final List<IResource> resources) {
        for (final IResource resource : resources) {
            if (resource.getName().equalsIgnoreCase(suiteName) && resource instanceof IFile) {
                return (IFile) resource;
            } else if (resource instanceof IContainer) {
                try {
                    extractSuiteFile(suiteName, Arrays.asList(((IContainer) resource).members()));
                } catch (final CoreException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    private boolean hasBreakpointAtCurrentKeywordPosition(final String executedSuite, final int keywordLineNumber) {
        boolean hasBreakpoint = false;
        final IBreakpoint[] currentBreakpoints = DebugPlugin.getDefault()
                .getBreakpointManager()
                .getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID);
        for (int i = 0; i < currentBreakpoints.length; i++) {
            final IBreakpoint currentBreakpoint = currentBreakpoints[i];
            final String breakpointResourceName = currentBreakpoint.getMarker().getResource().getName();
            try {
                if (breakpointResourceName.equals(executedSuite) && currentBreakpoint.isEnabled()) {
                    final int breakpointLineNum = (Integer) currentBreakpoint.getMarker().getAttribute(
                            IMarker.LINE_NUMBER);
                    if(keywordLineNumber == breakpointLineNum) {
                        boolean hasHitCount = false;
                        final int breakpointHitCount = currentBreakpoint.getMarker().getAttribute(
                                RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                        if (breakpointHitCount > 1) {
                            if (breakpointHitCounts.containsKey(currentBreakpoint)) {
                                final int currentHitCount = breakpointHitCounts.get(currentBreakpoint) + 1;
                                if (currentHitCount == breakpointHitCount) {
                                    hasHitCount = true;
                                }
                                breakpointHitCounts.put(currentBreakpoint, currentHitCount);
                            } else {
                                breakpointHitCounts.put(currentBreakpoint, 1);
                            }
                        } else {
                            hasHitCount = true;
                        }

                        if (hasHitCount) {
                            breakpointCondition = currentBreakpoint.getMarker().getAttribute(
                                    RobotLineBreakpoint.CONDITIONAL_ATTRIBUTE, "");
                            hasBreakpoint = true;
                            target.breakpointHit(currentBreakpoint);
                        }
                    }
                }
            } catch (final CoreException e) {
                e.printStackTrace();
            }
        }
        return hasBreakpoint;
    }
    
    private static class MissingFileToExecuteException extends RuntimeException {

        public MissingFileToExecuteException(final String message) {
            super(message);
        }
    }
}
