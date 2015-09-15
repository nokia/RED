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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
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
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugElement;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordFinder;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;

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
    
    private final List<IResource> suiteResources;
    
    private final RobotEventBroker robotEventBroker;

    private String currentSuite = "";

    private String breakpointCondition = "";

    private boolean isBreakpointConditionFulfilled;

    private KeywordContext currentKeywordContext = new KeywordContext();

    private final Map<String, List<Integer>> currentExecutionLinesInFile;

    private final Map<String, List<Integer>> executedBreakpointsInFile;

    private final Map<IBreakpoint, Integer> breakpointHitCounts;

    private final Map<String, String> currentResourceFiles;

    private boolean isStopping;
    
    public RobotDebugEventDispatcher(final RobotDebugTarget target, final List<IResource> suiteResources, final RobotEventBroker robotEventBroker) {
        super("Robot Event Dispatcher");
        setSystem(true);

        this.target = target;
        this.suiteResources = suiteResources;
        this.robotEventBroker = robotEventBroker;

        currentExecutionLinesInFile = new LinkedHashMap<>();
        executedBreakpointsInFile = new LinkedHashMap<>();
        currentResourceFiles = new LinkedHashMap<>();
        breakpointHitCounts = new LinkedHashMap<>();
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
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createStartSuiteExecutionElement(
                (String) suiteList.get(0), (String) suiteElements.get("source")));
    }

    private void handleStartTestEvent(final Map<?, ?> eventMap) {
        final List<?> testList = (List<?>) eventMap.get("start_test");
        final Map<?, ?> testElements = (Map<?, ?>) testList.get(1);
        final String line = "Starting test: " + testElements.get("longname") + '\n';
        robotEventBroker.sendAppendLineEventToMessageLogView(line);
        robotEventBroker.sendExecutionEventToExecutionView(ExecutionElementsParser.createStartTestExecutionElement((String) testList.get(0)));
    }
    
    private void handleStartKeywordEvent(final Map<?, ?> eventMap) {
        if (executedFile == null) {
            throw new MissingFileToExecuteException("Missing suite file for execution");
        }
        final List<?> startList = (List<?>) eventMap.get("start_keyword");
        final String currentKeyword = (String) startList.get(0);
        final Map<?, ?> startElements = (Map<?, ?>) startList.get(1);
        final List<String> args = (List<String>) startElements.get("args");
        
        String executedSuite = "";
        IFile currentFile = null;
        if (!currentResourceFiles.isEmpty()) {
            final String resource = (String) currentResourceFiles.values().toArray()[currentResourceFiles.size() - 1];
            currentFile = executedFile.getProject().getFile(resource);
            if (!currentFile.exists()) {
                currentFile = executedFile.getProject().getFile(
                        executedFile.getParent().getName() + "/" + resource);
            }
            executedSuite = resource;
        } else {
            currentFile = executedFile;
            executedSuite = currentSuite;
        }

        // TODO: check keywords in currentFrames and search keywords only after
        // parent keywords
        final int keywordLine = new KeywordFinder().getKeywordLine(currentFile, currentKeyword, args,
                currentExecutionLinesInFile.get(currentFile.getName()));
        if (keywordLine >= 0) {
            List<Integer> executionLines = currentExecutionLinesInFile.get(currentFile.getName());
            if (executionLines == null) {
                executionLines = new ArrayList<Integer>();
                currentExecutionLinesInFile.put(currentFile.getName(), executionLines);
            }
            executionLines.add(keywordLine);
        }

        boolean isBreakpoint = false;
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

                    List<Integer> executedBreakpointsLines = executedBreakpointsInFile.get(currentFile.getName());
                    if (executedBreakpointsLines == null) {
                        executedBreakpointsLines = new ArrayList<Integer>();
                        executedBreakpointsInFile.put(currentFile.getName(), executedBreakpointsLines);
                    }
                    if (!executedBreakpointsLines.contains(breakpointLineNum)
                            && new KeywordFinder().isKeywordInBreakpointLine(currentBreakpoint, breakpointLineNum,
                                    currentKeyword, args, keywordLine)) {

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
                            executedBreakpointsLines.add(breakpointLineNum);
                            breakpointCondition = currentBreakpoint.getMarker().getAttribute(
                                    RobotLineBreakpoint.CONDITIONAL_ATTRIBUTE, "");
                            isBreakpoint = true;
                            target.breakpointHit(currentBreakpoint);
                        }
                    }
                }
            } catch (final CoreException e) {
                e.printStackTrace();
            }
        }

        if (isBreakpoint || (target.getRobotThread().isStepping() && !target.hasStepOver()
                && !target.hasStepReturn())) {

            if (target.getRobotThread().isStepping()) {
                target.getRobotThread().setSteppingOver(false);
                target.getRobotThread().setSteppingReturn(false);
            }
            target.setHasStackFramesCreated(false);
            isStopping = true;
        } else {
            isStopping = false;
        }

        currentKeywordContext = new KeywordContext(null, executedSuite, keywordLine);
        target.getPartListener().setKeywordContext(currentKeywordContext);
        target.getCurrentFrames().put(currentKeyword, currentKeywordContext);

        // first keyword with resource name is in old file, so until second keyword
        // there is a need to switch between files
        final String[] keywordNameParts = currentKeyword.split("\\.");
        if (keywordNameParts.length > 1 && !keywordNameParts[0].equals("BuiltIn")) {
            // next keyword from here will be in another file
            final String resourceName = keywordNameParts[0];

            // TODO: get somehow name with extension of resource file
            final String resourceFileName = findResourceName(resourceName);
            if (!resourceFileName.equals("")) {
                currentResourceFiles.put(currentKeyword, resourceFileName);
            }
        }
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

        final String[] endKeywordNameParts = keyword.split("\\.");
        if (endKeywordNameParts.length > 1 && !endKeywordNameParts[0].equals("BuiltIn")) {
            final String resourceFileName = currentResourceFiles.get(keyword);
            currentExecutionLinesInFile.remove(resourceFileName);
            executedBreakpointsInFile.remove(resourceFileName);
            robotEventBroker.sendClearEventToTextEditor(resourceFileName);

            currentResourceFiles.remove(keyword);
        }
    }

    private void handleEndTestEvent(final Map<?, ?> eventMap) {
        final List<?> testList = (List<?>) eventMap.get("end_test");
        final Map<?, ?> testElements = (Map<?, ?>) testList.get(1);
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

    private String findResourceName(final String resourceName) {
        final IProject project = executedFile.getProject();
        final String txtFile = resourceName + ".txt";
        final String robotFile = resourceName + ".robot";
        if (project.getFile(robotFile).exists()) {
            return robotFile;
        } else if (project.getFile(txtFile).exists()) {
            return txtFile;
        }

        final IContainer parent = executedFile.getParent();
        if (parent != null && parent.getType() == IResource.FOLDER) {
            if (project.getFile(parent.getName() + "/" + robotFile).exists()) {
                return robotFile;
            } else if (project.getFile(parent.getName() + "/" + txtFile).exists()) {
                return txtFile;
            }
        }

        return "";
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
    
    private static class MissingFileToExecuteException extends RuntimeException {

        public MissingFileToExecuteException(final String message) {
            super(message);
        }
    }
}
