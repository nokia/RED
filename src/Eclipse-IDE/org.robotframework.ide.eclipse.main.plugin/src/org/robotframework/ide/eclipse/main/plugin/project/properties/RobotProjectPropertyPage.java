package org.robotframework.ide.eclipse.main.plugin.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.robotframework.ide.eclipse.main.plugin.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;


public class RobotProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {


    @Override
    protected Control createContents(final Composite parent) {
        noDefaultAndApplyButton();
        final IProject project = (IProject) getElement();
        if (RobotProjectNature.hasRobotNature(project)) {
            final RobotProject robotProject = RobotModelManager.getInstance().getModel().createRobotProject(project);

            createConfigurationButton(parent, project, true);
        } else {
            createConfigurationButton(parent, project, false);
        }
        return parent;
    }

    private void createConfigurationButton(final Composite parent, final IProject project, final boolean remove) {
        final String text = remove ? "Deconfigure" : "Configure";
        final String tooltip = remove ? "This project has Robot nature. Do you want to remove it?"
                : "This project is not a Robot Project. Do you want to add Robot nature to it?";

        final Button convertButton = new Button(parent, SWT.NONE);
        convertButton.setText(text);
        convertButton.setToolTipText(tooltip);
        convertButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                try {
                    if (remove) {
                        RobotProjectNature.removeRobotNature(project, new NullProgressMonitor());
                    } else {
                        RobotProjectNature.addRobotNature(project, new NullProgressMonitor());
                    }
                    getShell().close();
                } catch (final CoreException e) {
                    MessageDialog.openError(getShell(), "Conversion problem", "Unable to convert project. Reason: "
                            + e.getMessage());
                }
            }
        });
    }

}