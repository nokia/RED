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
import java.util.Map.Entry;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class DefineVariableFixer extends RedSuiteMarkerResolution {

    private final String variable;

    public static List<DefineVariableFixer> createFixers(final String parameterizedPath) {
        final List<String> variables = RobotExpressions.getVariables(parameterizedPath);
        final Map<String, String> vars = newHashMap();

        final List<DefineVariableFixer> fixers = newArrayList();
        for (final String var : variables) {
            if (!vars.containsKey(var)) {
                fixers.add(new DefineVariableFixer(var));
            }
        }
        return fixers;
    }

    public DefineVariableFixer(final String variable) {
        this.variable = variable;
    }

    @Override
    public String getLabel() {
        return "Define '" + variable + "' variable";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        System.out.println("Variable defined: " + variable);

        return Optional.<ICompletionProposal> of(createProposal());
    }

    private EmptyCompletionProposal createProposal() {
        return new EmptyCompletionProposal(getLabel()) {
            @Override
            public void apply(final IDocument document) {
                final StringBuilder builder = new StringBuilder();
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        final Shell shell = Display.getCurrent().getActiveShell();
                        final InputDialog dialog = new InputDialog(shell, "blah", "msg", "", null);
                        dialog.open();
                        builder.append(dialog.getValue());
                    }
                });
                final Map<String, String> variables = newHashMap();
                variables.put(variable, builder.toString());
                final List<String> vars = newArrayList();
                for (final Entry<String, String> entry : variables.entrySet()) {
                    vars.add(entry.getKey());
                    vars.add(entry.getValue());
                }
                final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
                store.setValue("variablesInResolution", Joiner.on(';').join(vars));
            }
        };
    }

}
