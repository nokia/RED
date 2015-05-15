package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugElement;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;

/**
 * Listens to events from the TestRunnerAgent and fires corresponding
 * debug events.
 * 
 * @author mmarzec
 */
public class RobotDebugEventDispatcher extends Job {

    private RobotDebugTarget target;

    private IFile executedFile;

    private RobotEventBroker robotEventBroker;

    private ObjectMapper mapper = new ObjectMapper();

    private String currentKeyword = "";

    private String currentSuite = "";

    private String breakpointCondition = "";

    private boolean isBreakpointConditionFulfilled;

    private BreakpointContext currentBreakpointContext = new BreakpointContext();

    private KeywordFinder keywordFinder = new KeywordFinder();

    private Map<String, List<Integer>> currentExecutionLinesInFile;

    private Map<String, List<Integer>> executedBreakpointsInFile;

    private Map<IBreakpoint, Integer> breakpointHitCounts;

    private Map<String, String> currentResourceFiles;

    private boolean isStopping;

    public RobotDebugEventDispatcher(RobotDebugTarget target, IFile executedFile, RobotEventBroker robotEventBroker) {
        super("Robot Event Dispatcher");
        setSystem(true);

        this.target = target;
        this.executedFile = executedFile;
        this.robotEventBroker = robotEventBroker;

        currentExecutionLinesInFile = new LinkedHashMap<>();
        executedBreakpointsInFile = new LinkedHashMap<>();
        currentResourceFiles = new LinkedHashMap<>();
        breakpointHitCounts = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            BufferedReader eventReader = target.getEventReader();
            Map<String, Object> eventMap = null;
            String event;
            while (!target.isTerminated() && eventReader != null && (event = eventReader.readLine()) != null) {

                eventMap = mapper.readValue(event, Map.class);
                String eventType = null;
                if (eventMap != null) {
                    Set<String> keySet = eventMap.keySet();
                    if (!keySet.isEmpty()) {
                        eventType = keySet.iterator().next();
                    }
                }
                if (eventType == null) {
                    continue;
                }

                switch (eventType) {
                    case "pid":
                        robotEventBroker.sendClearEventToMessageLogView();
                        target.started();
                        break;
                    case "start_suite":
                        List<Object> suiteList = (List<Object>) eventMap.get("start_suite");
                        Map<String, String> suiteElements = (Map<String, String>) suiteList.get(1);
                        currentSuite = new File(suiteElements.get("source")).getName();
                        break;
                    case "start_keyword":
                        List<Object> startList = (List<Object>) eventMap.get("start_keyword");
                        currentKeyword = (String) startList.get(0);
                        Map<String, Object> startElements = (Map<String, Object>) startList.get(1);
                        List<String> args = (List<String>) startElements.get("args");

                        String executedSuite = "";
                        IFile currentFile = null;
                        if (!currentResourceFiles.isEmpty()) {
                            String resource = (String) currentResourceFiles.values().toArray()[currentResourceFiles.size() - 1];
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
                        int keywordLine = keywordFinder.getKeywordLine(currentFile, currentKeyword, args,
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
                        IBreakpoint[] currentBreakpoints = DebugPlugin.getDefault()
                                .getBreakpointManager()
                                .getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID);
                        for (int i = 0; i < currentBreakpoints.length; i++) {
                            IBreakpoint currentBreakpoint = currentBreakpoints[i];
                            String breakpointResourceName = currentBreakpoint.getMarker().getResource().getName();
                            try {
                                if (breakpointResourceName.equals(executedSuite) && currentBreakpoint.isEnabled()) {
                                    int breakpointLineNum = (Integer) currentBreakpoint.getMarker().getAttribute(
                                            IMarker.LINE_NUMBER);

                                    List<Integer> executedBreakpointsLines = executedBreakpointsInFile.get(currentFile.getName());
                                    if (executedBreakpointsLines == null) {
                                        executedBreakpointsLines = new ArrayList<Integer>();
                                        executedBreakpointsInFile.put(currentFile.getName(), executedBreakpointsLines);
                                    }
                                    if (!executedBreakpointsLines.contains(breakpointLineNum)
                                            && keywordFinder.isKeywordInBreakpointLine(currentBreakpoint,
                                                    breakpointLineNum, currentKeyword, args, keywordLine)) {

                                        boolean hasHitCount = false;
                                        int breakpointHitCount = (Integer) currentBreakpoint.getMarker().getAttribute(
                                                RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                                        if (breakpointHitCount > 1) {
                                            if (breakpointHitCounts.containsKey(currentBreakpoint)) {
                                                int currentHitCount = breakpointHitCounts.get(currentBreakpoint) + 1;
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
                                            currentBreakpointContext.setContext(executedSuite, breakpointLineNum);
                                        }
                                    }
                                }
                            } catch (CoreException e) {
                                e.printStackTrace();
                            }
                        }

                        if (isBreakpoint || (target.getRobotThread().isStepping() && !target.hasStepOver()
                                && !target.hasStepReturn())) {

                            if (target.getRobotThread().isStepping()) {
                                target.getRobotThread().setSteppingOver(false);
                                target.getRobotThread().setSteppingReturn(false);
                                currentBreakpointContext.setContext(executedSuite, keywordLine);
                            }
                            target.clearStackFrames();
                            isStopping = true;
                        } else {
                            isStopping = false;
                        }

                        target.getPartListener().setBreakpointContext(currentBreakpointContext);

                        String fileName = currentSuite;
                        if (!currentResourceFiles.isEmpty()) {
                            fileName = (String) currentResourceFiles.values().toArray()[currentResourceFiles.size() - 1];
                        }
                        target.getCurrentFrames().put(currentKeyword, new KeywordContext(null, fileName, keywordLine));

                        // first keyword with resource name is in old file, so until second keyword
                        // there is a need to switch between files
                        String[] keywordNameParts = currentKeyword.split("\\.");
                        if (keywordNameParts.length > 1 && !keywordNameParts[0].equals("BuiltIn")) {
                            // next keyword from here will be in another file
                            String resourceName = keywordNameParts[0];

                            // TODO: get somehow name with extension of resource file
                            String resourceFileName = findResourceName(resourceName);
                            if (!resourceFileName.equals("")) {
                                currentResourceFiles.put(currentKeyword, resourceFileName);
                            }
                        }
                        break;
                    case "vars":
                        List<Object> varList = (List<Object>) eventMap.get("vars");
                        Map<String, String> vars = (Map<String, String>) varList.get(1);
                        target.getLastKeywordFromCurrentFrames().setVariables(vars);
                        break;
                    case "global_vars":
                        List<Object> globalVarList = (List<Object>) eventMap.get("global_vars");
                        Map<String, String> globalVars = (Map<String, String>) globalVarList.get(1);
                        target.getRobotVariablesManager().setGlobalVariables(globalVars);
                        break;
                    case "check_condition":
                        if (!"".equals(breakpointCondition) && !target.getRobotThread().isStepping()) {
                            String conditionJson = createJsonFromBreakpointCondition();
                            target.sendEventToAgent(conditionJson);
                        } else {
                            if (isStopping) {
                                target.sendEventToAgent("stop");
                                robotEventBroker.sendHighlightLineEventToTextEditor(currentBreakpointContext.getFile(),
                                        currentBreakpointContext.getLine());
                            } else {
                                target.sendEventToAgent("run");
                            }
                        }
                        break;
                    case "condition_result":
                        List<Object> resultList = (List<Object>) eventMap.get("condition_result");
                        Boolean result = (Boolean) resultList.get(0);
                        if (result) {
                            isBreakpointConditionFulfilled = true;
                        }
                        break;
                    case "condition_error":
                        isBreakpointConditionFulfilled = false;
                        List<Object> errorList = (List<Object>) eventMap.get("condition_error");
                        String error = (String) errorList.get(0);
                        ShowDebugErrorJob showDebugErrorJob = new ShowDebugErrorJob("Conditional Breakpoint Error",
                                "Reason:\n" + error);
                        showDebugErrorJob.schedule();
                        break;
                    case "condition_checked":
                        if (isStopping && isBreakpointConditionFulfilled) {
                            target.sendEventToAgent("stop");
                            robotEventBroker.sendHighlightLineEventToTextEditor(currentBreakpointContext.getFile(),
                                    currentBreakpointContext.getLine());
                        } else {
                            target.sendEventToAgent("run");
                        }
                        isBreakpointConditionFulfilled = false;
                        breakpointCondition = "";
                        break;
                    case "paused":
                        target.suspended(DebugEvent.CLIENT_REQUEST);
                        break;
                    case "end_keyword":
                        List<Object> endList = (List<Object>) eventMap.get("end_keyword");
                        String keyword = (String) endList.get(0);
                        target.getCurrentFrames().remove(keyword);

                        String[] endKeywordNameParts = keyword.split("\\.");
                        if (endKeywordNameParts.length > 1 && !endKeywordNameParts[0].equals("BuiltIn")) {
                            String resourceFileName = currentResourceFiles.get(keyword);
                            currentExecutionLinesInFile.remove(resourceFileName);
                            executedBreakpointsInFile.remove(resourceFileName);
                            robotEventBroker.sendClearEventToTextEditor(resourceFileName);

                            currentResourceFiles.remove(keyword);
                        }
                        break;
                    case "close":
                        robotEventBroker.sendClearAllEventToTextEditor();
                        target.terminated();
                        break;
                    case "log_message":
                        List<Object> messageList = (List<Object>) eventMap.get("log_message");
                        Map<String, String> messageElements = (Map<String, String>) messageList.get(0);
                        String line = messageElements.get("timestamp") + " : " + messageElements.get("level") + " : "
                                + messageElements.get("message") + '\n';
                        robotEventBroker.sendAppendLineEventToMessageLogView(line);
                        break;
                    default:
                        break;
                }

            }
        } catch (IOException e) {
            target.terminated();
        }
        return Status.OK_STATUS;
    }

    private String findResourceName(String resourceName) {
        IProject project = executedFile.getProject();
        String txtFile = resourceName + ".txt";
        String robotFile = resourceName + ".robot";
        if (project.getFile(robotFile).exists()) {
            return robotFile;
        } else if (project.getFile(txtFile).exists()) {
            return txtFile;
        }

        IContainer parent = executedFile.getParent();
        if (parent != null && parent.getType() == IResource.FOLDER) {
            if (project.getFile(parent.getName() + "/" + robotFile).exists()) {
                return robotFile;
            } else if (project.getFile(parent.getName() + "/" + txtFile).exists()) {
                return txtFile;
            }
        }

        return "";
    }

    private String createJsonFromBreakpointCondition() {
        String[] conditionElements = breakpointCondition.split("(\\s{2,}|\t)"); // two or more spaces or tab

        StringBuilder conditionJson = new StringBuilder();
        conditionJson.append("{\"keywordCondition\":[");
        conditionJson.append("\"" + conditionElements[0] + "\"");
        if (conditionElements.length > 1) {
            conditionJson.append(", [");
            for (int i = 1; i < conditionElements.length; i++) {
                conditionJson.append("\"" + conditionElements[i] + "\"");
                if (!(i + 1 == conditionElements.length)) {
                    conditionJson.append(",");
                }
            }
            conditionJson.append("]]}");
        } else {
            conditionJson.append("]}");
        }

        return conditionJson.toString();
    }

    private class ShowDebugErrorJob extends UIJob {

        private String title;

        private String message;

        public ShowDebugErrorJob(String title, String message) {
            super("Show Debug Error");
            setSystem(true);
            setPriority(Job.INTERACTIVE);
            this.message = message;
            this.title = title;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), title, message);
            return Status.OK_STATUS;
        }
    }
}
