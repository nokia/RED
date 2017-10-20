package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.RfLintMarkersClearHandler.E4RfLintMarkersClearHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


public class RfLintMarkersClearHandler extends DIParameterizedHandler<E4RfLintMarkersClearHandler> {

    public RfLintMarkersClearHandler() {
        super(E4RfLintMarkersClearHandler.class);
    }

    public static class E4RfLintMarkersClearHandler {

        @Execute
        public void clearMarkers(final @Named(Selections.SELECTION) IStructuredSelection selection) {
            final List<IResource> selectedResources = Selections.getAdaptableElements(selection, IResource.class);
            RfLintProblem.cleanProblems(selectedResources);
        }
    }
}
