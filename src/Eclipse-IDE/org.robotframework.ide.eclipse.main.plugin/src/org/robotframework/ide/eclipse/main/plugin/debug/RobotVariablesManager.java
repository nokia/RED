package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;

/**
 * @author mmarzec
 */
public class RobotVariablesManager {

    public static final String GLOBAL_VARIABLE_NAME = "Global Variables";

    public static final String SUITE_VARIABLE_PREFIX = "SUITE_";

    public static final String TEST_VARIABLE_PREFIX = "TEST_";

    private RobotDebugTarget target;

    private LinkedList<RobotVariablesContext> previousVariables;

    private Map<String, String> globalVariables;

    private Map<String, Integer> variablesPosition;

    private boolean hasVariablesViewerListener;

    private VariablesViewerUpdateListener variablesViewerUpdateListener;

    public RobotVariablesManager(RobotDebugTarget target) {
        this.target = target;
        previousVariables = new LinkedList<>();
        variablesPosition = new LinkedHashMap<>();
        variablesViewerUpdateListener = new VariablesViewerUpdateListener();
    }

    /**
     * Extract and sort variables for given StackTrace level.
     * Every level of StackTrace has its own context in previousVariables map. Current variables are
     * compared with previous state.
     * If current level of StackTrace has not any previous state, then previous state will be
     * variables from level below. This is
     * for saving previous order of variables in above levels.
     * 
     * @param stackTraceId
     * @param newVariables
     * @return
     */
    public IVariable[] extractRobotDebugVariables(int stackTraceId, Map<String, Object> newVariables) {
        Map<String, IVariable> previousVariablesMap = null;
        RobotVariablesContext currentVariablesContext = null;
        for (RobotVariablesContext variablesContext : previousVariables) {
            if (variablesContext.getStackTraceId() == stackTraceId) {
                previousVariablesMap = variablesContext.getVariablesMap();
                currentVariablesContext = variablesContext;
                break;
            }
        }
        if (currentVariablesContext == null && !previousVariables.isEmpty()) {
            previousVariablesMap = previousVariables.getLast().getVariablesMap();
        }

        Map<String, IVariable> nonGlobalVariablesMap = new LinkedHashMap<>();
        Map<String, IVariable> currentVariablesMap = new LinkedHashMap<>();
        if (previousVariablesMap == null) {
            currentVariablesMap.put(GLOBAL_VARIABLE_NAME, createGlobalVariable(createNestedGlobalVariables()));
            int position = 1;
            for (String newVarName : newVariables.keySet()) {
                if (!globalVariables.containsKey(newVarName)) {
                    position = variablesPosition.get(newVarName);
                    nonGlobalVariablesMap.put(newVarName,
                            new RobotDebugVariable(target, newVarName, newVariables.get(newVarName), null, position));
                }
            }
        } else {

            IVariable[] nestedGlobalVariables = new IVariable[globalVariables.size()];
            int nestedIndex = 0;

            for (String newVarName : newVariables.keySet()) {
                if (!globalVariables.containsKey(newVarName)) {
                    RobotDebugVariable newVariable = new RobotDebugVariable(target, newVarName,
                            newVariables.get(newVarName), null);
                    if (previousVariablesMap.containsKey(newVarName)) {
                        try {
                            RobotDebugVariable previousVariable = (RobotDebugVariable) previousVariablesMap.get(newVarName);
                            String variableOldValue = previousVariable.getValue().getValueString();
                            String variableNewValue = newVariables.get(newVarName).toString();
                            if (variableOldValue != null && !variableOldValue.equals(variableNewValue)
                                    && previousVariable.isValueModificationEnabled()) {
                                newVariable.setHasValueChanged(true);
                            }
                            newVariable.setPosition(previousVariable.getPosition());
                        } catch (DebugException e) {
                            e.printStackTrace();
                        }
                    } else {
                        newVariable.setPosition(variablesPosition.get(newVarName));
                    }
                    nonGlobalVariablesMap.put(newVarName, newVariable);
                } else {
                    nestedGlobalVariables[nestedIndex] = new RobotDebugVariable(target, newVarName,
                            newVariables.get(newVarName), null);
                    nestedIndex++;
                }
            }

            RobotDebugVariable globalVariable = createGlobalVariable(nestedGlobalVariables);
            currentVariablesMap.put(GLOBAL_VARIABLE_NAME, globalVariable);
        }

        nonGlobalVariablesMap = sortNonGlobalVariablesMap(nonGlobalVariablesMap);
        currentVariablesMap.putAll(nonGlobalVariablesMap);

        if (currentVariablesContext != null) {
            currentVariablesContext.setVariablesMap(nonGlobalVariablesMap);
        } else {
            previousVariables.add(new RobotVariablesContext(stackTraceId, nonGlobalVariablesMap));
        }

        return currentVariablesMap.values().toArray(new IVariable[currentVariablesMap.size()]);
    }

    private Map<String, IVariable> sortNonGlobalVariablesMap(final Map<String, IVariable> nonGlobalVariablesMap) {

        List<String> keyList = new LinkedList<String>(nonGlobalVariablesMap.keySet());
        Collections.sort(keyList, new Comparator<String>() {

            @Override
            public int compare(String key1, String key2) {
                if (key1.contains(TEST_VARIABLE_PREFIX) && key2.contains(SUITE_VARIABLE_PREFIX)) {
                    return 1;
                }
                if (key1.contains(SUITE_VARIABLE_PREFIX) || key1.contains(TEST_VARIABLE_PREFIX)) {
                    return -1;
                }
                if (key2.contains(SUITE_VARIABLE_PREFIX) || key2.contains(TEST_VARIABLE_PREFIX)) {
                    return 1;
                }
                if (((RobotDebugVariable) nonGlobalVariablesMap.get(key1)).getPosition() < ((RobotDebugVariable) nonGlobalVariablesMap.get(key2)).getPosition()) {
                    return -1;
                }
                if (((RobotDebugVariable) nonGlobalVariablesMap.get(key2)).getPosition() < ((RobotDebugVariable) nonGlobalVariablesMap.get(key1)).getPosition()) {
                    return 1;
                }

                return 0;
            }
        });

        Map<String, IVariable> sortedMap = new LinkedHashMap<String, IVariable>();
        for (Iterator<String> it = keyList.iterator(); it.hasNext();) {
            String key = it.next();
            sortedMap.put(key, nonGlobalVariablesMap.get(key));
        }
        return sortedMap;
    }

    public Map<String, String> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, String> globalVariables) {
        this.globalVariables = globalVariables;
    }

    private RobotDebugVariable createGlobalVariable(IVariable[] nestedVariables) {
        RobotDebugVariable variable = new RobotDebugVariable(target, GLOBAL_VARIABLE_NAME, "", null);
        RobotDebugValue value = new RobotDebugValue(target, "");
        value.setNestedVariables(nestedVariables);
        variable.setNewRobotDebugValue(value);

        return variable;
    }

    private IVariable[] createNestedGlobalVariables() {
        IVariable[] nested = new IVariable[globalVariables.size()];
        Set<String> set = globalVariables.keySet();
        int i = 0;
        for (String key : set) {
            nested[i] = new RobotDebugVariable(target, key, globalVariables.get(key), null);
            i++;
        }
        return nested;
    }

    public LinkedList<RobotVariablesContext> getPreviousVariables() {
        return previousVariables;
    }

    public Map<String, Integer> getVariablesPosition() {
        return variablesPosition;
    }

    public void extractVariablesPositions(Map<String, Object> vars) {
        int position = 1;
        if (variablesPosition.size() > 0) {
            position = variablesPosition.size() + 1;
        }
        Set<String> variableNameSet = vars.keySet();
        for (String varName : variableNameSet) {
            if (!variablesPosition.containsKey(varName)) {
                variablesPosition.put(varName, position);
                position++;
            }
        }
    }

    public void startAddVariablesViewerListenerJob() {
        if (!hasVariablesViewerListener) {
            new AddVariablesViewerListenerJob().schedule();
        }
    }

    public void startRemoveVariablesViewerListenerJob() {
        new RemoveVariablesViewerListenerJob().schedule();
    }

    public void setIsItemVisibleInVariablesViewer(boolean isVisible) {
        variablesViewerUpdateListener.setItemVisible(isVisible);
    }

    private class AddVariablesViewerListenerJob extends UIJob {

        public AddVariablesViewerListenerJob() {
            super("Add Variables Viewer Listener");
            setSystem(true);
            setPriority(Job.INTERACTIVE);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @SuppressWarnings("restriction")
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            IViewPart viewPart = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .findView("org.eclipse.debug.ui.VariableView");
            if (viewPart != null && viewPart instanceof VariablesView) {
                VariablesView variablesView = (VariablesView) viewPart;
                final TreeModelViewer variablesTreeModelViewer = (TreeModelViewer) variablesView.getViewer();
                if (variablesTreeModelViewer != null) {
                    variablesViewerUpdateListener.setTree(variablesTreeModelViewer.getTree());
                    variablesTreeModelViewer.addViewerUpdateListener(variablesViewerUpdateListener);
                    hasVariablesViewerListener = true;
                }
            }
            return Status.OK_STATUS;
        }
    }

    private class RemoveVariablesViewerListenerJob extends UIJob {

        public RemoveVariablesViewerListenerJob() {
            super("Remove Variables Viewer Listener");
            setSystem(true);
            setPriority(Job.INTERACTIVE);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @SuppressWarnings("restriction")
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            IViewPart viewPart = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .findView("org.eclipse.debug.ui.VariableView");
            if (viewPart != null && viewPart instanceof VariablesView) {
                VariablesView variablesView = (VariablesView) viewPart;
                final TreeModelViewer variablesTreeModelViewer = (TreeModelViewer) variablesView.getViewer();
                if (variablesTreeModelViewer != null) {
                    variablesTreeModelViewer.removeViewerUpdateListener(variablesViewerUpdateListener);
                }
            }
            return Status.OK_STATUS;
        }
    }

    @SuppressWarnings("restriction")
    public class VariablesViewerUpdateListener implements IViewerUpdateListener {

        private Tree tree;

        private boolean isItemVisible;

        public VariablesViewerUpdateListener() {
        }

        @Override
        public void viewerUpdatesBegin() {
        }

        @Override
        public void viewerUpdatesComplete() {
            if (!isItemVisible && tree != null) {
                int itemCount = tree.getItemCount();
                if (itemCount > 0) {
                    try {
                        TreeItem item = tree.getItem(itemCount - 1);
                        tree.showItem(item);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } finally {
                        isItemVisible = true;
                    }
                }
            }
        }

        @Override
        public void updateStarted(IViewerUpdate update) {
        }

        @Override
        public void updateComplete(IViewerUpdate update) {
        }

        public void setItemVisible(boolean isItemVisible) {
            this.isItemVisible = isItemVisible;
        }

        public void setTree(Tree tree) {
            this.tree = tree;
        }

    }
}
