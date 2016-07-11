package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ConfigureRobotNatureHandler.E4ConfigureRobotNatureHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ConfigureRobotNatureHandler extends DIParameterizedHandler<E4ConfigureRobotNatureHandler> {

    public ConfigureRobotNatureHandler() {
        super(E4ConfigureRobotNatureHandler.class);
    }

    public static class E4ConfigureRobotNatureHandler {

        @Execute
        public void configure(final @Named(Selections.SELECTION) IStructuredSelection selection,
                @Named("org.robotframework.red.configureRobotNature.enablement") final String enablement)
                throws CoreException {

            final List<IProject> projects = Selections.getElements(selection, IProject.class);

            if ("enable".equalsIgnoreCase(enablement)) {

                for (final IProject project : projects) {
                    RobotProjectNature.addRobotNature(project, new NullProgressMonitor());
                }
            } else if ("disable".equalsIgnoreCase(enablement)) {

                for (final IProject project : projects) {
                    RobotProjectNature.removeRobotNature(project, new NullProgressMonitor(),
                            shouldRedXmlBeRemoved(project.getName()));
                }
            }
        }

        private static boolean shouldRedXmlBeRemoved(final String projectName) {
            return MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                    "Confirm configuration file removal", "You have deconfigured the project '" + projectName
                            + "' as a Robot project. Do you want to remove project configuration file 'red.xml' too?");
        }
    }
}
