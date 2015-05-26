package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author mmarzec
 *
 */
public class RobotLaunchConfigurationMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    public static final String PROJECT_NAME_ATTRIBUTE = "Project name";

    public static final String RESOURCE_NAME_ATTRIBUTE = "Resource name";

    public static final String EXECUTOR_NAME_ATTRIBUTE = "Executor name";
    
    public static final String EXECUTOR_ARGUMENTS_ATTRIBUTE = "Executor arguments";
    
    public static final String RESOURCES_SEPARATOR = ";";

    public static final String PYBOT_NAME = "pybot";

    public static final String JYBOT_NAME = "jybot";

    private Text txtResources;

    private Text txtProject;
    
    private Text txtArgs;

    private Combo comboExecutorName;

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(PROJECT_NAME_ATTRIBUTE, "");
        configuration.setAttribute(RESOURCE_NAME_ATTRIBUTE, "");
        configuration.setAttribute(EXECUTOR_NAME_ATTRIBUTE, "");
        configuration.setAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, "");
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            txtProject.setText(configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, ""));
            txtResources.setText(configuration.getAttribute(RESOURCE_NAME_ATTRIBUTE, ""));
            comboExecutorName.select(comboExecutorName.indexOf(configuration.getAttribute(EXECUTOR_NAME_ATTRIBUTE, "")));
            txtArgs.setText(configuration.getAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, ""));
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(PROJECT_NAME_ATTRIBUTE, txtProject.getText());
        configuration.setAttribute(RESOURCE_NAME_ATTRIBUTE, txtResources.getText());
        if (comboExecutorName.getSelectionIndex() > -1) {
            configuration.setAttribute(EXECUTOR_NAME_ATTRIBUTE,
                    comboExecutorName.getItem(comboExecutorName.getSelectionIndex()));
        }
        configuration.setAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, txtArgs.getText());
    }

    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {

        setErrorMessage(null);
        try {
            String projectNameAttribute = configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, "");
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNameAttribute);
            if (project.exists()) {
                String resourceNameAttribute = configuration.getAttribute(RESOURCE_NAME_ATTRIBUTE, "");
                if (resourceNameAttribute.equals("")) {
                    return true;
                }
                String[] resourceNames = resourceNameAttribute.split(RESOURCES_SEPARATOR);
                for (int i = 0; i < resourceNames.length; i++) {
                    
                    String name = resourceNames[i];
                    if (!name.equals("") && !project.getFile(name).exists() && !project.getFolder(name).exists()) {
                        return false;
                    }
                }
                
                return true;
            }
        } catch (Exception e) {
            setErrorMessage("Invalid file selected.");
        }

        setErrorMessage("Invalid file selected.");
        return false;
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public boolean canSave() {
        return !txtProject.getText().isEmpty();
    }

    @Override
    public String getMessage() {
        return "Please select a project.";
    }
    
    @Override
    public void createControl(final Composite parent) {

        Composite topControl = new Composite(parent, SWT.NONE);
        topControl.setLayout(new GridLayout(1, false));

        Group executorGroup = new Group(topControl, SWT.NONE);
        executorGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        executorGroup.setText("Executor");
        executorGroup.setLayout(new GridLayout(3, false));

        comboExecutorName = new Combo(executorGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboExecutorName.add(PYBOT_NAME);
        comboExecutorName.add(JYBOT_NAME);
        comboExecutorName.select(0);
        comboExecutorName.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        Label lblArgs = new Label(executorGroup, SWT.NONE);
        lblArgs.setText("Arguments:");
        
        txtArgs = new Text(executorGroup, SWT.BORDER);
        txtArgs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtArgs.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        

        Group projectGroup = new Group(topControl, SWT.NONE);
        projectGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        projectGroup.setText("Project");
        projectGroup.setLayout(new GridLayout(2, false));

        txtProject = new Text(projectGroup, SWT.BORDER);
        txtProject.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        txtProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnBrowseProject = new Button(projectGroup, SWT.NONE);
        btnBrowseProject.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(parent.getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setTitle("Select project");
                dialog.setMessage("Select the project hosting your test suites:");
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    txtProject.setText(((IResource) dialog.getFirstResult()).getName());
                }
            }
        });
        btnBrowseProject.setText("Browse...");

        Group testGroup = new Group(topControl, SWT.NONE);
        testGroup.setLayout(new GridLayout(2, false));
        testGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        testGroup.setText("Test Suite");

        txtResources = new Text(testGroup, SWT.BORDER);
        txtResources.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        txtResources.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnBrowseTest = new Button(testGroup, SWT.NONE);
        btnBrowseTest.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(parent.getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setAllowMultiple(true);
                dialog.setTitle("Select test suite");
                dialog.setMessage("Select the test suite to execute:");
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    txtResources.setText("");
                    Object[] results = dialog.getResult();
                    for (int i = 0; i < results.length; i++) {
                        txtResources.append(((IResource) results[i]).getProjectRelativePath().toPortableString());
                        if(results.length > 1 && i < results.length-1) {
                            txtResources.append(RESOURCES_SEPARATOR);
                        }
                    }
                }
            }
        });
        btnBrowseTest.setText("Browse...");

        setControl(topControl);
    }

}
