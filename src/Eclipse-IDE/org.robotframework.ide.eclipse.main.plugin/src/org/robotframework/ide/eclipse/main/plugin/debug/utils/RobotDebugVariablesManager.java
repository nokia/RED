package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugValue;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable;

/**
 * @author mmarzec
 */
public class RobotDebugVariablesManager {

    public static final String GLOBAL_VARIABLE_NAME = "Global Variables";

    public static final String SUITE_VARIABLE_PREFIX = "SUITE_";

    public static final String TEST_VARIABLE_PREFIX = "TEST_";

    private RobotDebugTarget target;

    private LinkedList<RobotDebugVariablesContext> previousVariables;

    private Map<String, String> globalVariables;
    
    Map<String, IVariable> nestedGlobalVars;

    private LinkedList<String> sortedVariablesNames = new LinkedList<String>();

    private boolean hasVariablesViewerListener;

    private VariablesViewerUpdateListener variablesViewerUpdateListener;

    public RobotDebugVariablesManager(RobotDebugTarget target) {
        this.target = target;
        previousVariables = new LinkedList<>();
        nestedGlobalVars = new LinkedHashMap<>();
        variablesViewerUpdateListener = new VariablesViewerUpdateListener();
    }

    /**
     * Extract and sort variables for given StackTrace level.
     * Every level of StackTrace has its own context in previousVariables map. Current variables are
     * compared with previous state.
     * If current level of StackTrace has not any previous state, then previous state will be
     * variables from level below. This is
     * for saving previous order of variables in higher levels.
     * 
     * @param stackTraceId
     * @param newVariables
     * @return
     */
    public IVariable[] extractRobotDebugVariables(int stackTraceId, Map<String, Object> newVariables) {

        RobotDebugVariablesContext currentVariablesContext = findCurrentVariablesContext(stackTraceId);
        Map<String, IVariable> previousVariablesMap = null;
        if (currentVariablesContext != null) {
            previousVariablesMap = currentVariablesContext.getVariablesMap();
        } else if (!previousVariables.isEmpty()) {
            previousVariablesMap = previousVariables.getLast().getVariablesMap();
        }

        Map<String, IVariable> nonGlobalVariablesMap = new LinkedHashMap<>();
        LinkedList<IVariable> currentVariablesList = new LinkedList<IVariable>();
        if (previousVariablesMap == null) {
            for (String variableName : sortedVariablesNames) {
                if (!globalVariables.containsKey(variableName) && newVariables.containsKey(variableName)) {
                    RobotDebugVariable newVariable = new RobotDebugVariable(target, variableName,
                            newVariables.get(variableName), null);
                    nonGlobalVariablesMap.put(variableName, newVariable);
                }
            }
        } else {
            for (String variableName : sortedVariablesNames) {
                if (newVariables.containsKey(variableName)) {
                    if (!globalVariables.containsKey(variableName)) {
                        RobotDebugVariable newVariable = new RobotDebugVariable(target, variableName,
                                newVariables.get(variableName), null);
                        newVariable.setHasValueChanged(this.hasValueChanged(variableName, newVariable, newVariables,
                                previousVariablesMap));
                        nonGlobalVariablesMap.put(variableName, newVariable);
                    } else {
                        if (!newVariables.get(variableName).equals(globalVariables.get(variableName))) {
                            nestedGlobalVars.put(variableName,
                                    new RobotDebugVariable(target, variableName, newVariables.get(variableName), null));
                        }
                    }
                }
            }
        }
        
        currentVariablesList.addAll(nonGlobalVariablesMap.values());
        currentVariablesList.addLast(createGlobalVariable());

        if (currentVariablesContext != null) {
            currentVariablesContext.setVariablesMap(nonGlobalVariablesMap);
        } else {
            previousVariables.add(new RobotDebugVariablesContext(stackTraceId, nonGlobalVariablesMap));
        }

        return currentVariablesList.toArray(new IVariable[currentVariablesList.size()]);
    }

    private boolean hasValueChanged(String newVarName, RobotDebugVariable newVariable, Map<String, Object> newVariables,
            Map<String, IVariable> previousVariablesMap) {

        if (previousVariablesMap.containsKey(newVarName)) {
            try {
                RobotDebugVariable previousVariable = (RobotDebugVariable) previousVariablesMap.get(newVarName);
                if(newVariable.getValue().hasVariables() && previousVariable.getValue().hasVariables()) {
                    return compareNestedVariables(newVariable.getValue().getVariables(), previousVariable.getValue().getVariables());
                }
                String variablePreviousValue = previousVariable.getValue().getValueString();
                String variableNewValue = newVariable.getValue().getValueString();
                if (variablePreviousValue != null && !variablePreviousValue.equals(variableNewValue)
                        && previousVariable.isValueModificationEnabled()) {
                    return true;
                }
            } catch (DebugException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    private boolean compareNestedVariables(IVariable[] newNestedVars, IVariable[] prevNestedVars) {

        if (newNestedVars.length != prevNestedVars.length) {
            return true;
        }
        boolean hasNestedValueChanged = false;
        try {
            for (int i = 0; i < newNestedVars.length; i++) {
                IValue newValue = newNestedVars[i].getValue();
                IValue previousValue = prevNestedVars[i].getValue();
                if (newValue.hasVariables() && previousValue.hasVariables()) {
                    compareNestedVariables(newValue.getVariables(), previousValue.getVariables());
                } else if (previousValue.getValueString() != null
                        && !previousValue.getValueString().equals(newValue.getValueString())) {
                    ((RobotDebugVariable) newNestedVars[i]).setHasValueChanged(true);
                    hasNestedValueChanged = true;
                }
            }
        } catch (DebugException e) {
            e.printStackTrace();
        }
        return hasNestedValueChanged;
    }

    private RobotDebugVariablesContext findCurrentVariablesContext(int stackTraceId) {
        for (RobotDebugVariablesContext variablesContext : previousVariables) {
            if (variablesContext.getStackTraceId() == stackTraceId) {
                return variablesContext;
            }
        }
        return null;
    }

    public Map<String, String> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, String> globalVariables) {
        this.globalVariables = globalVariables;
        Set<String> set = globalVariables.keySet();
        for (String key : set) {
            RobotDebugVariable globalVar = new RobotDebugVariable(target, key, globalVariables.get(key), null);
            globalVar.setValueModificationEnabled(false);
            nestedGlobalVars.put(key, globalVar);
        }
    }
    
    private RobotDebugVariable createGlobalVariable() {
        RobotDebugVariable variable = new RobotDebugVariable(target, GLOBAL_VARIABLE_NAME, "", null);
        variable.setValueModificationEnabled(false);
        RobotDebugValue value = new RobotDebugValue(target, "", nestedGlobalVars.values().toArray(new IVariable[nestedGlobalVars.size()]));
        variable.setRobotDebugValue(value);

        return variable;
    }

    public LinkedList<RobotDebugVariablesContext> getPreviousVariables() {
        return previousVariables;
    }

    public void sortVariablesNames(Map<String, Object> vars) {

        Set<String> variableNameSet = vars.keySet();
        for (String varName : variableNameSet) {
            if (!sortedVariablesNames.contains(varName)) {
                if (varName.contains(SUITE_VARIABLE_PREFIX)) {
                    sortedVariablesNames.addLast(varName);
                } else if (varName.contains(TEST_VARIABLE_PREFIX)) {
                    sortedVariablesNames.addLast(varName);
                } else {
                    sortedVariablesNames.addFirst(varName);
                }
            }
        }
    }

    public String extractVariableRootAndChilds(RobotDebugVariable parent, LinkedList<String> childNameList,
            String variableName) {
        String parentName = "";
        try {
            parentName = parent.getName();
        } catch (DebugException e) {
            e.printStackTrace();
        }
        if (parent.getParent() == null) {
            childNameList.add(extractChildName(variableName));
            return parentName;
        } else {
            childNameList.addFirst(extractChildName(parentName));
            return extractVariableRootAndChilds(parent.getParent(), childNameList, variableName);
        }
    }

    private String extractChildName(String variableName) {
        if (variableName.indexOf("[") >= 0 && variableName.indexOf("]") >= 0) {
            return variableName.substring(1, variableName.indexOf("]"));
        }
        return variableName;
    }

    public void addVariablesViewerListener() {
        if (!hasVariablesViewerListener) {
            registerViewerUpdateListener();
        }
    }

    public void removeVariablesViewerListener() {
        unregisterViewerUpdateListener();
    }

    public void setIsVariablesViewerUpdated(boolean isUpdated) {
        variablesViewerUpdateListener.setViewerUpdated(isUpdated);
    }

    private void registerViewerUpdateListener() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                IViewPart viewPart = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .findView("org.eclipse.debug.ui.VariableView");
                if (viewPart != null && viewPart instanceof VariablesView) {
                    VariablesView variablesView = (VariablesView) viewPart;
                    final TreeModelViewer variablesTreeModelViewer = (TreeModelViewer) variablesView.getViewer();
                    if (variablesTreeModelViewer != null) {
                        variablesViewerUpdateListener.setTreeModelViewer(variablesTreeModelViewer);
                        variablesTreeModelViewer.addViewerUpdateListener(variablesViewerUpdateListener);
                        hasVariablesViewerListener = true;
                    }
                }
            }
        });
    }

    private void unregisterViewerUpdateListener() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
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
            }
        });
    }

    @SuppressWarnings("restriction")
    public class VariablesViewerUpdateListener implements IViewerUpdateListener {

        private TreeModelViewer treeModelViewer;

        private boolean isViewerUpdated;

        public VariablesViewerUpdateListener() {
        }

        @Override
        public void viewerUpdatesBegin() {
        }

        @Override
        public void viewerUpdatesComplete() {
            if (!isViewerUpdated) {
                int itemCount = treeModelViewer.getTree().getItemCount();
                if (itemCount > 0) {
                    try {
                        treeModelViewer.getTree().deselectAll();
                        treeModelViewer.collapseAll();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } finally {
                        isViewerUpdated = true;
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

        public void setViewerUpdated(boolean isViewerUpdated) {
            this.isViewerUpdated = isViewerUpdated;
        }

        public void setTreeModelViewer(TreeModelViewer treeModelViewer) {
            this.treeModelViewer = treeModelViewer;
        }
    }
}
