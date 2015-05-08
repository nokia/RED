package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private KeywordFinder keywordFinder = new KeywordFinder();

    private Map<String, List<Integer>> currentExecutionLinesInFile;

    private Map<String, List<BreakpointContext>> executedBreakpointsInFile;
    
    private Map<String, String> currentResourceFiles;

    public RobotDebugEventDispatcher(RobotDebugTarget target, IFile executedFile,
            RobotEventBroker robotEventBroker) {
        super("Robot Event Dispatcher");
        setSystem(true);

        this.target = target;
        this.executedFile = executedFile;
        this.robotEventBroker = robotEventBroker;

        currentExecutionLinesInFile = new LinkedHashMap<>();
        executedBreakpointsInFile = new LinkedHashMap<>();
        currentResourceFiles = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            BufferedReader messageReader = target.getMessageReader();
            String event;
            while (!target.isTerminated() && messageReader != null && (event = messageReader.readLine()) != null) {

                Map<String, Object> map = mapper.readValue(event, Map.class);

                if (map.containsKey("pid")) {
                    robotEventBroker.sendClearEventToMessageLogView();
                    target.started();
                }

                if (map.containsKey("start_suite")) {
                    List<Object> list = (List<Object>) map.get("start_suite");
                    Map<String, String> elements = (Map<String, String>) list.get(1);
                    currentSuite = new File(elements.get("source")).getName();
                }

                if (map.containsKey("start_keyword")) {
                    List<Object> list = (List<Object>) map.get("start_keyword");
                    currentKeyword = (String) list.get(0);
                    Map<String, Object> elements = (Map<String, Object>) list.get(1);
                    List<String> args = (List<String>) elements.get("args");

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
                                
                                if (keywordFinder.isKeywordInBreakpointLine(currentBreakpoint, breakpointLineNum,
                                        currentKeyword, args, keywordLine)) {

                                    List<BreakpointContext> executedBreakpoints = executedBreakpointsInFile.get(currentFile.getName());
                                    if (executedBreakpoints == null) {
                                        executedBreakpoints = new ArrayList<BreakpointContext>();
                                        executedBreakpointsInFile.put(currentFile.getName(), executedBreakpoints);
                                    }

                                    BreakpointContext breakpointContext = null;
                                    for (BreakpointContext context : executedBreakpoints) {
                                        if (context.getBreakpoint().equals(currentBreakpoint)) {
                                            breakpointContext = context;
                                        }
                                    }
                                    if (breakpointContext == null) {
                                        breakpointContext = new BreakpointContext(currentBreakpoint);
                                        executedBreakpoints.add(breakpointContext);
                                    }
                                    breakpointContext.incrementCurrentHitCount();

                                    boolean hasHitCount = false;
                                    int breakpointHitCount = (Integer) currentBreakpoint.getMarker().getAttribute(
                                            RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                                    int currentHitCount = breakpointContext.getCurrentHitCount();
                                    if (currentHitCount == breakpointHitCount) {
                                        hasHitCount = true;
                                    }

                                    if (hasHitCount) {
                                        isBreakpoint = true;
                                        target.breakpointHit(currentBreakpoint);
                                        robotEventBroker.sendHighlightLineEventToTextEditor(executedSuite,
                                                breakpointLineNum);
                                    }
                                }
                            }
                        } catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }

                    if ((isBreakpoint || target.getRobotThread().isStepping()) && !target.hasStepOver() && !target.hasStepReturn()) {

                        if (target.getRobotThread().isStepping()) {
                            target.getRobotThread().setSteppingOver(false);
                            target.getRobotThread().setSteppingReturn(false);
                            robotEventBroker.sendHighlightLineEventToTextEditor(executedSuite, keywordLine);
                        }
                        target.clearStackFrames();
                        target.sendMessageToAgent("stop");
                    } else {
                        target.sendMessageToAgent("run");
                    }

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
                }

                if (map.containsKey("vars")) {
                    List<Object> list = (List<Object>) map.get("vars");
                    Map<String, String> vars = (Map<String, String>) list.get(1);

                    // String fileName = currentSuite;
                    // if(!currentResourceFiles.isEmpty()) {
                    // fileName = (String)
                    // currentResourceFiles.values().toArray()[currentResourceFiles.size()-1];
                    // }
                    // currentFrames.put(currentKeyword, new ActiveKeyword(vars, fileName));

                    target.getLastKeywordFromCurrentFrames().setVariables(vars);
                }

                if (map.containsKey("end_keyword")) {
                    List<Object> list = (List<Object>) map.get("end_keyword");
                    String keyword = (String) list.get(0);
                    target.getCurrentFrames().remove(keyword);

                    String[] keywordNameParts = keyword.split("\\.");
                    if (keywordNameParts.length > 1 && !keywordNameParts[0].equals("BuiltIn")) {
                        String fileName = currentResourceFiles.get(keyword);
                        currentExecutionLinesInFile.remove(fileName);
                        executedBreakpointsInFile.remove(fileName);
                        robotEventBroker.sendClearEventToTextEditor(fileName);

                        currentResourceFiles.remove(keyword);
                    }
                }

                if (map.containsKey("paused")) {
                    target.suspended(DebugEvent.CLIENT_REQUEST);
                }

                if (map.containsKey("close")) {
                    robotEventBroker.sendClearAllEventToTextEditor();
                    target.terminated();
                }

                if (map.containsKey("log_message")) {
                    List<Object> list = (List<Object>) map.get("log_message");
                    Map<String, String> elements = (Map<String, String>) list.get(0);
                    String line = elements.get("timestamp") + " : " + elements.get("level") + " : "
                            + elements.get("message") + '\n';
                    robotEventBroker.sendAppendLineEventToMessageLogView(line);
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

}
