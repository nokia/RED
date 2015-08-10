package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.EnumSet;
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
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.robotframework.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * @author mmarzec
 *
 */
public class RobotLaunchConfigurationMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private Combo comboExecutorName;
    private Text argumentsText;
    private Text projectText;
    private ListViewer viewer;
    private Button includeTagsBtn;
    private Text includeTagsText;
    private Button excludeTagsBtn;
    private Text excludeTagsText;
    private ControlDecoration decoration;

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        RobotLaunchConfiguration.fillDefaults(configuration);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            final String projectName = robotConfig.getProjectName();

            final List<String> executorNames = getSuiteExecutorNames();
            final List<IPath> suites = Lists.transform(robotConfig.getSuitePaths(), new Function<String, IPath>() {
                @Override
                public IPath apply(final String pathStr) {
                    return Path.fromPortableString(pathStr);
                }
            });

            comboExecutorName.setItems(executorNames.toArray(new String[0]));
            comboExecutorName.select(executorNames.indexOf(robotConfig.getExecutor().name()));

            projectText.setText(projectName);
            argumentsText.setText(robotConfig.getExecutorArguments());
            viewer.setInput(newArrayList(suites));
            viewer.refresh();
            
            includeTagsBtn.setSelection(robotConfig.isIncludeTagsEnabled());
            includeTagsText.setText(Joiner.on(", ").join(robotConfig.getIncludedTags()));
            excludeTagsBtn.setSelection(robotConfig.isExcludeTagsEnabled());
            excludeTagsText.setText(Joiner.on(", ").join(robotConfig.getExcludedTags()));

        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    private List<String> getSuiteExecutorNames() {
        final EnumSet<SuiteExecutor> executors = EnumSet.allOf(SuiteExecutor.class);
        return newArrayList(Collections2.transform(executors, new Function<SuiteExecutor, String>() {
            @Override
            public String apply(final SuiteExecutor executor) {
                return executor.name();
            }
        }));
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig
                .setExecutor(SuiteExecutor.fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex())));
        robotConfig.setProjectName(projectText.getText());
        robotConfig.setExecutorArguments(argumentsText.getText());
        robotConfig.setSuitePaths(Lists.transform(getSuites(), new Function<IPath, String>() {
            @Override
            public String apply(final IPath path) {
                return path.toPortableString();
            }
        }));
        robotConfig.setIsIncludeTagsEnabled(includeTagsBtn.getSelection());
        robotConfig.setIncludedTags(extractTags(includeTagsText.getText()));
        robotConfig.setIsExcludeTagsEnabled(excludeTagsBtn.getSelection());
        robotConfig.setExcludedTags(extractTags(excludeTagsText.getText()));
    }
    
    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);
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

            final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
            final SuiteExecutor selectedExecutor = SuiteExecutor.fromName(comboExecutorName.getText());
            final SuiteExecutor projectInterpreter = robotProject.getRuntimeEnvironment().getInterpreter();
            if (selectedExecutor != projectInterpreter) {
                setWarningMessage("The selected '" + comboExecutorName.getText() + "' interpreter is different "
                        + "than the interpreter used by '" + projectName + "' (" + projectInterpreter + "). The test "
                        + "will  be launched using " + comboExecutorName.getText()
                        + " interpreter as defined in PATH environment variable");
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
        return ImagesManager.getImage(RedImages.getRobotImage());
    }

    @Override
    public boolean canSave() {
        return !projectText.getText().isEmpty();
    }

    @Override
    public String getMessage() {
        return "Please select a project";
    }

    @Override
    public void dispose() {
        if (decoration != null) {
            decoration.dispose();
        }
        super.dispose();
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
        GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 3).applyTo(executorGroup);

        comboExecutorName = new Combo(executorGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboExecutorName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        final Label lblArgs = new Label(executorGroup, SWT.NONE);
        lblArgs.setText("Arguments:");

        argumentsText = new Text(executorGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(450, SWT.DEFAULT).applyTo(argumentsText);
        argumentsText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        includeTagsBtn = new Button(executorGroup, SWT.CHECK);
        includeTagsBtn.setText("Only run tests with these tags:");
        includeTagsBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        includeTagsText = new Text(executorGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(includeTagsText);
        includeTagsText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        decoration = new ControlDecoration(includeTagsText, SWT.RIGHT | SWT.TOP);
        decoration.setDescriptionText("Separate multiple tags with a comma character like 'tag1, tag2...'");
        decoration.setImage(FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
        
        excludeTagsBtn = new Button(executorGroup, SWT.CHECK);
        excludeTagsBtn.setText("Skip tests with these tags:");
        excludeTagsBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        excludeTagsText = new Text(executorGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(excludeTagsText);
        excludeTagsText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
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
                    final IProject project = (IProject) dialog.getFirstResult();
                    final RobotProject robotProject = RedPlugin.getModelManager().getModel()
                            .createRobotProject(project);
                    final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
                    if (interpreter != null) {
                        final int index = newArrayList(comboExecutorName.getItems()).indexOf(interpreter.name());
                        comboExecutorName.select(index);
                    }
                    projectText.setText(project.getName());
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
                        final IPath pathToAdd = ((IResource) obj).getProjectRelativePath();
                        if (!suites.contains(pathToAdd)) {
                            suites.add(pathToAdd);
                        }
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
    
    private List<String> extractTags(final String tagsTxt) {
        List<String> tagsList = newArrayList();
        if(tagsTxt!=null && !tagsTxt.equals("")) {
            String[] tags = tagsTxt.split(",");
            for (int i = 0; i < tags.length; i++) {
                tagsList.add(tags[i].trim());
            }
        }
        return tagsList;
    }
}
