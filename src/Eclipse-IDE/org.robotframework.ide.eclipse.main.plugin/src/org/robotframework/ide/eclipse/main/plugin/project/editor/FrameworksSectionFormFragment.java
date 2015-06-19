package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsContentProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsPreferencesPage;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class FrameworksSectionFormFragment implements ISectionFormFragment {

    private static final String IMAGE_FOR_LINK = "image";
    private static final String PATH_LINK = "systemPath";
    private static final String PREFERENCES_LINK = "preferences";

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private IMessageManager messageManager;

    @Inject
    private RedProjectEditorInput editorInput;


    private CheckboxTableViewer viewer;

    private FormText currentFramework;

    private Button sourceButton;

    @Override
    public void initialize(final Composite parent) {
        final Section section = toolkit.createSection(parent, Section.EXPANDED | Section.TITLE_BAR
                | Section.DESCRIPTION);
        section.setText("Robot framework");
        section.setDescription("In this section Robot framework location can be specified. The selected framework will"
                + " be used by this project. Currently following framework is in use:");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);

        final Composite sectionInternal = toolkit.createComposite(section);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(sectionInternal);
        GridLayoutFactory.fillDefaults().applyTo(sectionInternal);
        section.setClient(sectionInternal);

        createCurrentFrameworkInfo(sectionInternal);
        createSeparator(sectionInternal);
        createSourceButton(sectionInternal);
        createViewer(sectionInternal);
    }

    void whenConfigurationFiledChanged() {
        currentFramework.setText("", false, false);
        sourceButton.setEnabled(false);
        viewer.getTable().setEnabled(false);
    }

    private void createCurrentFrameworkInfo(final Composite parent) {
        currentFramework = toolkit.createFormText(parent, true);
        currentFramework.setImage(IMAGE_FOR_LINK, RobotImages.getRobotImage().createImage());
        GridDataFactory.fillDefaults().grab(true, false).indent(15, 5).applyTo(currentFramework);

        final IHyperlinkListener hyperlinkListener = createHyperlinkListener();
        currentFramework.addHyperlinkListener(hyperlinkListener);
        currentFramework.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                currentFramework.removeHyperlinkListener(hyperlinkListener);
            }
        });
    }

    private IHyperlinkListener createHyperlinkListener() {
        return new HyperlinkAdapter() {
            @Override
            public void linkActivated(final HyperlinkEvent event) {
                final Shell shell = viewer.getTable().getShell();
                if (PATH_LINK.equals(event.getHref())) {
                    if (Desktop.isDesktopSupported()) {
                        final File file = new File(event.getLabel());
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (final IOException e) {
                            ErrorDialog.openError(shell, "Unable to open location",
                                    "Unable to open location: " + file.getAbsolutePath(), null);
                        }
                    }

                } else if (PREFERENCES_LINK.equals(event.getHref())) {
                    PreferencesUtil.createPreferenceDialogOn(shell, InstalledRobotsPreferencesPage.ID,
                            new String[] { InstalledRobotsPreferencesPage.ID }, null).open();
                }
            }
        };
    }

    private void createSeparator(final Composite parent) {
        final Label separator = toolkit.createSeparator(parent, SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(separator);
    }

    private void createSourceButton(final Composite parent) {
        final RobotProjectConfig configuration = editorInput.getProjectConfiguration();

        // "Use local settings for this project (if not checked, then active framework from preferences is used)."
        sourceButton = toolkit.createButton(parent, "Use local settings for this project", SWT.CHECK);
        sourceButton.setEnabled(false);
        sourceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                viewer.getTable().setEnabled(sourceButton.getSelection());

                final Object[] elements = viewer.getCheckedElements();
                File file;
                if (elements.length == 1 && sourceButton.getSelection()) {
                    final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) elements[0];
                    file = env.getFile();
                } else {
                    file = null;
                }
                configuration.assignPythonLocation(file);
                dirtyProviderService.setDirtyState(true);
            }
        });
    }

    private void createViewer(final Composite tableParent) {
        final Table table = new Table(tableParent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL);
        viewer = new CheckboxTableViewer(table);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setEnabled(false);

        final ICheckStateListener checkListener = createCheckListener();
        viewer.addCheckStateListener(checkListener);
        viewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeCheckStateListener(checkListener);
            }
        });

        ColumnViewerToolTipSupport.enableFor(viewer);

        viewer.setContentProvider(new InstalledRobotsContentProvider());
        ViewerColumnsFactory.newColumn("Name").withWidth(300)
                .labelsProvidedBy(new InstalledRobotsNamesLabelProvider(viewer)).createFor(viewer);
        ViewerColumnsFactory.newColumn("Path").withWidth(200)
                .labelsProvidedBy(new InstalledRobotsPathsLabelProvider(viewer)).createFor(viewer);
    }

    private ICheckStateListener createCheckListener() {
        final RobotProjectConfig configuration = editorInput.getProjectConfiguration();

        final ICheckStateListener checkListener = new ICheckStateListener() {
            @Override
            public void checkStateChanged(final CheckStateChangedEvent event) {
                File file;
                if (event.getChecked()) {
                    viewer.setCheckedElements(new Object[] { event.getElement() });
                    final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) event.getElement();
                    file = env.getFile();
                } else {
                    sourceButton.setSelection(false);
                    viewer.getTable().setEnabled(false);
                    file = null;
                }
                configuration.assignPythonLocation(file);
                dirtyProviderService.setDirtyState(true);
                viewer.refresh();
            }
        };
        return checkListener;
    }

    void whenEnvironmentWasLoaded(final RobotRuntimeEnvironment env, final List<?> allEnvironments) {
        final RobotProjectConfig configuration = editorInput.getProjectConfiguration();
        final boolean isEditable = editorInput.isEditable();

        if (viewer.getTable() == null || viewer.getTable().isDisposed()) {
            return;
        }
        final boolean isUsingPrefs = configuration.usesPreferences();

        sourceButton.setEnabled(isEditable);
        sourceButton.setSelection(!isUsingPrefs);

        viewer.setInput(allEnvironments);
        if (env != null) {
            viewer.setChecked(env, true);
        }
        viewer.getTable().setEnabled(isEditable && !isUsingPrefs);
        viewer.refresh();

        if (env == null) {
            currentFramework.setText(createCurrentFrameworkText("unknown"), true, true);
            int i = 0;
            for (final String problem : getProblems()) {
                messageManager.addMessage("problems_" + i, problem, null, IMessageProvider.ERROR, currentFramework);
                i++;
            }
        } else {
            final String activeText = createActiveFrameworkText(env, isUsingPrefs);
            currentFramework.setText(createCurrentFrameworkText(activeText), true, true);
        }
        currentFramework.getParent().layout();
    }

    private List<String> getProblems() {
        final RobotProject project = editorInput.getRobotProject();
        final IFile file = project != null ? project.getConfigurationFile() : null;
        if (file != null) {
            try {
                final IMarker[] markers = file.findMarkers(RobotProblem.TYPE_ID, true, 1);
                return Lists.transform(newArrayList(markers), new Function<IMarker, String>() {
                    @Override
                    public String apply(final IMarker marker) {
                        try {
                            return (String) marker.getAttribute("message");
                        } catch (final CoreException e) {
                            return "";
                        }
                    }
                });
            } catch (final CoreException e) {
                return newArrayList();
            }
        }
        return newArrayList();
    }

    private String createCurrentFrameworkText(final String path) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<form>");
        builder.append("<p><img href=\"" + IMAGE_FOR_LINK + "\"/> " + path + "</p>");
        builder.append("</form>");
        return builder.toString();
    }

    private String createActiveFrameworkText(final RobotRuntimeEnvironment env, final boolean isUsingPrefs) {
        final StringBuilder activeText = new StringBuilder();
        activeText.append("<a href=\"" + PATH_LINK + "\">");
        activeText.append(env.getFile().getAbsolutePath());
        activeText.append("</a>");
        activeText.append(" " + env.getVersion());
        if (isUsingPrefs) {
            activeText.append(" (<a href=\"" + PREFERENCES_LINK + "\">from Preferences</a>)");
        }
        return activeText.toString();
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    public TableViewer getViewer() {
        return viewer;
    }
}
