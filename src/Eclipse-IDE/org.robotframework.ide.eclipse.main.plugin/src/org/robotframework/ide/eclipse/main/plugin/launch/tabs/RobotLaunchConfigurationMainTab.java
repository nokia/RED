package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * @author mmarzec
 *
 */
public class RobotLaunchConfigurationMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private Text projectText;
    private Text argumentsText;
    private ListViewer viewer;

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        RobotLaunchConfiguration.fillDefaults(configuration);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            final String projectName = robotConfig.getProjectName();

            projectText.setText(projectName);
            argumentsText.setText(robotConfig.getExecutorArguments());
            final List<IPath> suites = Lists.transform(robotConfig.getSuitePaths(), new Function<String, IPath>() {
                @Override
                public IPath apply(final String pathStr) {
                    return Path.fromPortableString(pathStr);
                }
            });
            viewer.setInput(newArrayList(suites));
            viewer.refresh();

        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.setProjectName(projectText.getText());
        robotConfig.setExecutorArguments(argumentsText.getText());
        robotConfig.setSuitePaths(Lists.transform(getSuites(), new Function<IPath, String>() {
            @Override
            public String apply(final IPath path) {
                return path.toPortableString();
            }
        }));
    }

    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

            final String projectName = robotConfig.getProjectName();
            if (projectName.isEmpty()) {
                setErrorMessage("Invalid project specified");
                return false;
            }

            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

            if (!project.exists()) {
                setErrorMessage("Project '" + projectName + "' does not exist in workspace");
                return false;
            }
            final List<String> suitePaths = robotConfig.getSuitePaths();

            if (suitePaths.isEmpty()) {
                setErrorMessage("There are no suites specified");
                return false;
            }

            final List<String> problematic = newArrayList();
            for (final String path : suitePaths) {
                final IResource member = project.findMember(Path.fromPortableString(path));
                if (member == null) {
                    problematic.add(path);
                }
            }
            if (!problematic.isEmpty()) {
                setErrorMessage("Following suites does not exist: " + Joiner.on(',').join(problematic));
                return false;
            }
            return true;
        } catch (final Exception e) {
            setErrorMessage("Invalid file selected");
            return false;
        }
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public Image getImage() {
        return RobotImages.getRobotImage().createImage();
    }

    @Override
    public boolean canSave() {
        return !projectText.getText().isEmpty();
    }

    @Override
    public String getMessage() {
        return "Please select a project";
    }

    @SuppressWarnings("unchecked")
    private List<IPath> getSuites() {
        return (List<IPath>) viewer.getInput();
    }
    
    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        createExecutorGroup(composite);
        createProjectGroup(composite);
        createTestSuitesGroup(composite);

        setControl(composite);
    }

    private void createExecutorGroup(final Composite topControl) {
        final Group executorGroup = new Group(topControl, SWT.NONE);
        executorGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        executorGroup.setText("Executor");
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(executorGroup);

        final Label lblArgs = new Label(executorGroup, SWT.NONE);
        lblArgs.setText("Arguments:");

        argumentsText = new Text(executorGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(argumentsText);
        argumentsText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void createProjectGroup(final Composite composite) {
        final Group projectGroup = new Group(composite, SWT.NONE);
        projectGroup.setText("Project");
        GridDataFactory.fillDefaults().applyTo(projectGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(projectGroup);

        projectText = new Text(projectGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectText);
        projectText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        final Button browseProject = new Button(projectGroup, SWT.NONE);
        browseProject.setText("Browse...");
        browseProject.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setTitle("Select project");
                dialog.setMessage("Select the project hosting your test suites:");
                dialog.addFilter(new ViewerFilter() {
                    @Override
                    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                        return element instanceof IProject;
                    }
                });
                dialog.setAllowMultiple(false);
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    projectText.setText(((IResource) dialog.getFirstResult()).getName());
                    updateLaunchConfigurationDialog();
                }
            }
        });
    }

    private void createTestSuitesGroup(final Composite composite) {
        final Group projectGroup = new Group(composite, SWT.NONE);
        projectGroup.setText("Test Suite(s)");
        GridDataFactory.fillDefaults().applyTo(projectGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(projectGroup);

        viewer = new ListViewer(projectGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(1, 3).hint(SWT.DEFAULT, 100).applyTo(viewer.getList());
        viewer.setContentProvider(new StructuredContentProvider() {
            @Override
            public Object[] getElements(final Object inputElement) {
                return ((List<?>) inputElement).toArray();
            }
        });
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(final Object element) {
                return ((IPath) element).toString();
            }
        });

        final Button browseSuites = new Button(projectGroup, SWT.PUSH);
        browseSuites.setText("Browse...");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(browseSuites);
        browseSuites.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setAllowMultiple(true);
                dialog.setTitle("Select test suite");
                dialog.setMessage("Select the test suite to execute:");
                dialog.addFilter(new ViewerFilter() {
                    @Override
                    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                        return element instanceof IResource
                                && ((IResource) element).getProject().getName().equals(projectText.getText());
                    }
                });
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    final List<IPath> suites = getSuites();
                    for (final Object obj : dialog.getResult()) {
                        suites.add(((IResource) obj).getProjectRelativePath());
                    }
                    viewer.setInput(suites);
                    updateLaunchConfigurationDialog();
                }
            }
        });

        final Button removeSuite = new Button(projectGroup, SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeSuite);
        removeSuite.setText("Remove");
        removeSuite.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final List<IPath> suites = getSuites();
                final List<IPath> selectedElements = Selections.getElements(
                        (IStructuredSelection) viewer.getSelection(), IPath.class);
                suites.removeAll(selectedElements);
                viewer.setInput(suites);
                updateLaunchConfigurationDialog();
            }
        });
    }
}
