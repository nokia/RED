package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DIParameterizedHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.FindElementUsagesHandler.E4FindUsagesHandler;
import org.robotframework.viewers.Selections;

public class FindElementUsagesHandler extends DIParameterizedHandler<E4FindUsagesHandler> {

    public FindElementUsagesHandler() {
        super(E4FindUsagesHandler.class);
    }

    public static class E4FindUsagesHandler {

        @Execute
        public Object findUsages(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named("org.robotframework.ide.eclipse.findElementUsages.place") final String place) {
            final RobotElement element = Selections.getSingleElement(selection, RobotElement.class);
            final String name = element.getName();

            IResource placeToStart = null;
            if ("workspace".equals(place)) {
                placeToStart = ResourcesPlugin.getWorkspace().getRoot();
            } else if ("project".equals(place)) {
                placeToStart = element.getSuiteFile().getProject().getProject();
            }

            if (place == null) {
                return null;
            }

            try {
                final ISearchQuery query = TextSearchQueryProvider.getPreferred().createQuery(name,
                        new IResource[] { placeToStart });
                NewSearchUI.runQueryInBackground(query);
            } catch (final CoreException e) {
                // TODO : show some message to the user
                return null;
            }
            return null;
        }
    }
}
