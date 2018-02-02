/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.general.VariableMappingDialog;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

/**
 * @author Michal Anglart
 *
 */
public class DefineGlobalVariableInConfigFixer extends RedXmlConfigMarkerResolution {

    private final String variable;

    public static List<DefineGlobalVariableInConfigFixer> createFixers(final String parameterizedPath) {
        final List<String> variables = RobotExpressions.getVariables(parameterizedPath);
        final Map<String, String> vars = newHashMap();

        final List<DefineGlobalVariableInConfigFixer> fixers = newArrayList();
        for (final String var : variables) {
            if (!vars.containsKey(var)) {
                fixers.add(new DefineGlobalVariableInConfigFixer(var));
            }
        }
        return fixers;
    }

    public DefineGlobalVariableInConfigFixer(final String variable) {
        this.variable = variable;
    }

    @Override
    public String getLabel() {
        return "Define '" + variable + "' variable";
    }

    @Override
    protected ICompletionProposal asContentProposal(final IMarker marker, final IFile externalFile) {
        return new DefineLibraryProposal(marker, externalFile, variable, getLabel());
    }

    private class DefineLibraryProposal extends RedConfigFileCompletionProposal {

        private final String name;
        private VariableMapping variableMapping;
        private List<VariableMapping> changedMappings;

        public DefineLibraryProposal(final IMarker marker, final IFile externalFile, final String variableName,
                final String shortDescription) {
            super(marker, externalFile, shortDescription, null);
            this.name = variableName;
        }

        @Override
        public boolean apply(final IFile externalFile, final RobotProjectConfig config) {
            variableMapping = SwtThread.syncEval(new Evaluation<VariableMapping>() {
                @Override
                public VariableMapping runCalculation() {
                    final Shell shell = Display.getCurrent().getActiveShell();
                    final VariableMappingDialog dialog = new VariableMappingDialog(shell, name);
                    return dialog.open() == Window.OK ? dialog.getMapping() : null;
                }
            });

            if (variableMapping == null) {
                return false;
            }
            config.addVariableMapping(variableMapping);
            changedMappings = config.getVariableMappings();
            return true;
        }

        @Override
        protected void openDesiredPageInEditor(final RedProjectEditor editor) {
            editor.openGeneralPage();
        }

        @Override
        protected void fireEvents() {
            eventBroker.post(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_STRUCTURE_CHANGED, changedMappings);
        }

        @Override
        public String getAdditionalProposalInfo() {
            return "Add " + name + " variable mapping to red.xml file";
        }

        @Override
        public Image getImage() {
            return ImagesManager.getImage(RedImages.getRobotVariableImage());
        }
    }
}
