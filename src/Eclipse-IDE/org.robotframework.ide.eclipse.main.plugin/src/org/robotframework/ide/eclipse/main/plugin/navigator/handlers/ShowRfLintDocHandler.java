/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import javax.inject.Named;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ShowRfLintDocHandler.E4ShowRfLintDocHandler;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.Documentations;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.RfLintRuleInput;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class ShowRfLintDocHandler extends DIParameterizedHandler<E4ShowRfLintDocHandler> {

    public ShowRfLintDocHandler() {
        super(E4ShowRfLintDocHandler.class);
    }

    public static class E4ShowRfLintDocHandler {

        @Execute
        public void showRuleDocumentation(final IWorkbenchPage page,
                final @Named(Selections.SELECTION) IStructuredSelection selection) throws CoreException {
            final IMarker selectedMarker = Selections.getAdaptableElements(selection, IMarker.class).get(0);

            final String ruleName = (String) selectedMarker.getAttribute(RfLintProblem.RULE_NAME);
            Documentations.showDoc(page, new RfLintRuleInput(ruleName));
        }
    }
}
