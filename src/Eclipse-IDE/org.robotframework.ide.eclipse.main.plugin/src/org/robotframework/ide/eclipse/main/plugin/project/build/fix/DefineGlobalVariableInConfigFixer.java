/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.List;
import java.util.stream.Collectors;

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
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingDialog;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariablesProjectConfigurationEditorPart;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

/**
 * @author Michal Anglart
 *
 */
public class DefineGlobalVariableInConfigFixer extends RedXmlConfigMarkerResolution {

    private final String name;

    public static List<DefineGlobalVariableInConfigFixer> createFixers(final String parameterizedPath) {
        final List<String> variables = RobotExpressions.getVariables(parameterizedPath);
        return variables.stream().map(DefineGlobalVariableInConfigFixer::new).collect(Collectors.toList());
    }

    public DefineGlobalVariableInConfigFixer(final String name) {
        this.name = name;
    }

    @Override
    public String getLabel() {
        return "Define '" + name + "' variable";
    }

    @Override
    protected ICompletionProposal asContentProposal(final IMarker marker, final IFile externalFile) {
        return new DefineVariableMappingProposal(marker, externalFile, getLabel());
    }

    private class DefineVariableMappingProposal extends RedConfigFileCompletionProposal {

        private VariableMapping variableMapping;

        private List<VariableMapping> changedMappings;

        public DefineVariableMappingProposal(final IMarker marker, final IFile externalFile,
                final String shortDescription) {
            super(marker, externalFile, shortDescription, null);
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
            final boolean wasAdded = config.addVariableMapping(variableMapping);
            if (wasAdded) {
                changedMappings = config.getVariableMappings();
            }
            return wasAdded;
        }

        @Override
        protected void openDesiredPageInEditor(final RedProjectEditor editor) {
            editor.openPage(VariablesProjectConfigurationEditorPart.class);
        }

        @Override
        protected void fireEvents() {
            final RedProjectConfigEventData<List<VariableMapping>> eventData = new RedProjectConfigEventData<>(
                    externalFile, changedMappings);
            eventBroker.post(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_STRUCTURE_CHANGED, eventData);
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
