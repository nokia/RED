/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
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
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.TreeViewer;
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


    private final RobotDebugTarget target;

    private final LinkedList<RobotDebugVariablesContext> previousVariables;

    private Map<String, String> globalVariables;
    
    private final Map<String, IVariable> nestedGlobalVars;

    private final LinkedList<String> sortedVariablesNames;

    private VariablesViewerUpdateListener variablesViewerUpdateListener;

    public RobotDebugVariablesManager(final RobotDebugTarget target) {
        this.target = target;
        this.previousVariables = new LinkedList<>();
        this.nestedGlobalVars = new LinkedHashMap<>();
        this.sortedVariablesNames = new LinkedList<String>();
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
    public IVariable[] extractRobotDebugVariables(final int stackTraceId, final Map<String, Object> newVariables) {

        final RobotDebugVariablesContext currentVariablesContext = findCurrentVariablesContext(stackTraceId);
        Map<String, IVariable> previousVariablesMap = null;
        if (currentVariablesContext != null) {
            previousVariablesMap = currentVariablesContext.getVariablesMap();
        } else if (!previousVariables.isEmpty()) {
            previousVariablesMap = previousVariables.getLast().getVariablesMap();
        }

        final Map<String, IVariable> nonGlobalVariablesMap = new LinkedHashMap<>();
        final LinkedList<IVariable> currentVariablesList = new LinkedList<IVariable>();
        if (previousVariablesMap == null) {
            for (final String variableName : sortedVariablesNames) {
                if (!globalVariables.containsKey(variableName) && newVariables.containsKey(variableName)) {
                    final RobotDebugVariable newVariable = new RobotDebugVariable(target, variableName,
                            newVariables.get(variableName), null);
                    nonGlobalVariablesMap.put(variableName, newVariable);
                }
            }
        } else {
            for (final String variableName : sortedVariablesNames) {
                if (newVariables.containsKey(variableName)) {
                    if (!globalVariables.containsKey(variableName)) {
                        final RobotDebugVariable newVariable = new RobotDebugVariable(target, variableName,
                                newVariables.get(variableName), null);
                        newVariable
                                .setHasValueChanged(hasValueChanged(variableName, newVariable, previousVariablesMap));
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

    private boolean hasValueChanged(final String newVarName, final RobotDebugVariable newVariable,
            final Map<String, IVariable> previousVariablesMap) {

        if (previousVariablesMap.containsKey(newVarName)) {
            try {
                final RobotDebugVariable previousVariable = (RobotDebugVariable) previousVariablesMap.get(newVarName);
                if(newVariable.getValue().hasVariables() && previousVariable.getValue().hasVariables()) {
                    return compareNestedVariables(newVariable.getValue().getVariables(), previousVariable.getValue().getVariables());
                }
                final String variablePreviousValue = previousVariable.getValue().getValueString();
                final String variableNewValue = newVariable.getValue().getValueString();
                if (variablePreviousValue != null && !variablePreviousValue.equals(variableNewValue)
                        && previousVariable.isValueModificationEnabled()) {
                    return true;
                }
            } catch (final DebugException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    private boolean compareNestedVariables(final IVariable[] newNestedVars, final IVariable[] prevNestedVars) {

        if (newNestedVars.length != prevNestedVars.length) {
            return true;
        }
        boolean hasNestedValueChanged = false;
        try {
            for (int i = 0; i < newNestedVars.length; i++) {
                final IValue newValue = newNestedVars[i].getValue();
                final IValue previousValue = prevNestedVars[i].getValue();
                if (newValue.hasVariables() && previousValue.hasVariables()) {
                    compareNestedVariables(newValue.getVariables(), previousValue.getVariables());
                } else if (previousValue.getValueString() != null
                        && !previousValue.getValueString().equals(newValue.getValueString())) {
                    ((RobotDebugVariable) newNestedVars[i]).setHasValueChanged(true);
                    hasNestedValueChanged = true;
                }
            }
        } catch (final DebugException e) {
            e.printStackTrace();
        }
        return hasNestedValueChanged;
    }

    private RobotDebugVariablesContext findCurrentVariablesContext(final int stackTraceId) {
        for (final RobotDebugVariablesContext variablesContext : previousVariables) {
            if (variablesContext.getStackTraceId() == stackTraceId) {
                return variablesContext;
            }
        }
        return null;
    }

    public Map<String, String> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(final Map<String, String> globalVariables) {
        this.globalVariables = globalVariables;
        final Set<String> set = globalVariables.keySet();
        for (final String key : set) {
            final RobotDebugVariable globalVar = new RobotDebugVariable(target, key, globalVariables.get(key), null);
            globalVar.setValueModificationEnabled(false);
            nestedGlobalVars.put(key, globalVar);
        }
    }
    
    private RobotDebugVariable createGlobalVariable() {
        final RobotDebugVariable variable = new RobotDebugVariable(target, GLOBAL_VARIABLE_NAME, "", null);
        variable.setValueModificationEnabled(false);
        final RobotDebugValue value = new RobotDebugValue(target, "", nestedGlobalVars.values().toArray(new IVariable[nestedGlobalVars.size()]));
        variable.setRobotDebugValue(value);

        return variable;
    }

    public LinkedList<RobotDebugVariablesContext> getPreviousVariables() {
        return previousVariables;
    }

    public void sortVariablesNames(final Map<String, Object> vars) {

        final String[] varArray = vars.keySet().toArray(new String[vars.keySet().size()]);
        for (int i = varArray.length-1; i >=0; i--) {
            final String varName = varArray[i];
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

    public String extractVariableRootAndChilds(final RobotDebugVariable parent, final LinkedList<String> childNameList,
            final String variableName) {
        String parentName = "";
        try {
            parentName = parent.getName();
        } catch (final DebugException e) {
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

    private String extractChildName(final String variableName) {
        if (variableName.indexOf("[") >= 0 && variableName.indexOf("]") >= 0) {
            return variableName.substring(1, variableName.indexOf("]"));
        }
        return variableName;
    }

    public void addVariablesViewerListener() {
        if (variablesViewerUpdateListener == null) {
            registerViewerUpdateListener();
        } else {
            variablesViewerUpdateListener.setViewerUpdateIsNeeded();
        }
    }

    public void removeVariablesViewerListener() {
        unregisterViewerUpdateListener();
    }

    private void registerViewerUpdateListener() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                final IViewPart viewPart = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .findView("org.eclipse.debug.ui.VariableView");
                if (viewPart instanceof IDebugView) {
                    final IDebugView variablesView = (IDebugView) viewPart;
                    final TreeModelViewer variablesTreeModelViewer = (TreeModelViewer) variablesView.getViewer();
                    if (variablesTreeModelViewer != null) {
                        variablesViewerUpdateListener = new VariablesViewerUpdateListener(variablesTreeModelViewer);
                        variablesTreeModelViewer.addViewerUpdateListener(variablesViewerUpdateListener);
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
                final IViewPart viewPart = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .findView("org.eclipse.debug.ui.VariableView");
                if (viewPart instanceof IDebugView) {
                    final IDebugView variablesView = (IDebugView) viewPart;
                    final TreeModelViewer variablesTreeModelViewer = (TreeModelViewer) variablesView.getViewer();
                    if (variablesTreeModelViewer != null) {
                        variablesTreeModelViewer.removeViewerUpdateListener(variablesViewerUpdateListener);
                    }
                }
            }
        });
    }

    private class VariablesViewerUpdateListener implements IViewerUpdateListener {

        private final TreeViewer treeModelViewer;

        private boolean viewerNeedsUpdate;

        public VariablesViewerUpdateListener(final TreeViewer variablesTreeModelViewer) {
            this.treeModelViewer = variablesTreeModelViewer;
        }

        @Override
        public void viewerUpdatesBegin() {
        }

        @Override
        public void viewerUpdatesComplete() {
            if (viewerNeedsUpdate) {
                final int itemCount = treeModelViewer.getTree().getItemCount();
                if (itemCount > 0) {
                    try {
                        treeModelViewer.getTree().deselectAll();
                        treeModelViewer.collapseAll();
                    } catch (final IllegalArgumentException e) {
                        // that's fine
                    } finally {
                        viewerNeedsUpdate = false;
                    }
                }
            }
        }

        @Override
        public void updateStarted(final IViewerUpdate update) {
        }

        @Override
        public void updateComplete(final IViewerUpdate update) {
        }

        void setViewerUpdateIsNeeded() {
            this.viewerNeedsUpdate = true;
        }
    }
}
