/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.validation;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Viewers;

/**
 * @author Michal Anglart
 */
public class ProjectValidationFormFragment implements ISectionFormFragment {

    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.redxmleditor.validation.context";

    @Inject
    private IEditorSite site;

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private RowExposingTreeViewer viewer;

    private Button excludeFilesBtn;

    private Text excludeFilesTxt;

    ISelectionProvider getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = createSection(parent);
        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().applyTo(internalComposite);

        createViewer(internalComposite);
        createColumns();
        createContextMenu();
        setInput();
        installResourceChangeListener();

        final Composite excludeFilesComposite = toolkit.createComposite(internalComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 10).applyTo(excludeFilesComposite);
        createExcludeFilesControls(excludeFilesComposite);
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Excluded project parts");
        section.setDescription("Specify parts of the project which shouldn't be validated.");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private void createViewer(final Composite parent) {
        viewer = new RowExposingTreeViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).indent(0, 10).applyTo(viewer.getTree());
        viewer.setUseHashlookup(true);
        viewer.setAutoExpandLevel(2);
        viewer.getTree().setEnabled(false);
        viewer.setComparator(new ViewerSorter());

        viewer.setContentProvider(new WorkbenchContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, CONTEXT_ID);
    }

    private void createColumns() {
        ViewerColumnsFactory.newColumn("")
                .withWidth(300)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(100)
                .labelsProvidedBy(new ProjectValidationPathsLabelProvider(editorInput))
                .createFor(viewer);
    }

    private void createExcludeFilesControls(final Composite parent) {
        final RobotProjectConfig projectConfiguration = editorInput.getProjectConfiguration();
        excludeFilesBtn = toolkit.createButton(parent, "Exclude files by size [KB] greater than:", SWT.CHECK);
        excludeFilesBtn.setSelection(projectConfiguration.isValidatedFileSizeCheckingEnabled());

        excludeFilesTxt = toolkit.createText(parent, projectConfiguration.getValidatedFileMaxSize());
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(excludeFilesTxt);

        excludeFilesTxt.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(final VerifyEvent e) {
                final String string = e.text;
                if (string != null) {
                    final char[] chars = new char[string.length()];
                    string.getChars(0, chars.length, chars, 0);
                    for (int i = 0; i < chars.length; i++) {
                        if (!('0' <= chars[i] && chars[i] <= '9')) {
                            e.doit = false;
                            return;
                        }
                    }
                }
            }
        });
        excludeFilesTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                try {
                    final String fileMaxSizeTxt = excludeFilesTxt.getText();
                    Long.parseLong(fileMaxSizeTxt);
                    editorInput.getProjectConfiguration().setValidatedFileMaxSize(fileMaxSizeTxt);
                    setDirty(true);
                } catch (final NumberFormatException e1) {
                    // nothing to do
                }
            }
        });

        excludeFilesBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final boolean selection = excludeFilesBtn.getSelection();
                excludeFilesTxt.setEnabled(selection);
                editorInput.getProjectConfiguration().setIsValidatedFileSizeCheckingEnabled(selection);
                setDirty(true);
            }
        });
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.redxmleditor.validation.contextMenu";

        final Tree control = viewer.getTree();
        final MenuManager manager = new MenuManager("Red.xml file editor validation context menu", menuId);
        manager.setRemoveAllWhenShown(true);
        final IMenuListener menuListener = menuManager -> menuManager.add(new Separator("additions"));
        manager.addMenuListener(menuListener);
        control.addDisposeListener(e -> manager.removeMenuListener(menuListener));
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        if (viewer.getTree() == null || viewer.getTree().isDisposed()
                || !editorInput.getRobotProject().getProject().exists()) {
            return;
        }

        try {
            viewer.getTree().setRedraw(false);

            final ISelection selection = viewer.getSelection();

            final TreeItem topTreeItem = viewer.getTree().getTopItem();
            final Object topItem = topTreeItem == null ? null : topTreeItem.getData();

            final IProject project = editorInput.getRobotProject().getProject();
            final IWorkspaceRoot wsRoot = project.getWorkspace().getRoot();
            final ProjectTreeElement wrappedRoot = new ProjectTreeElement(wsRoot, false);

            buildTreeFor(wrappedRoot, project);
            addMissingEntriesToTree(wrappedRoot, wrappedRoot.getAll());

            viewer.setInput(wrappedRoot);
            if (topItem != null) {
                viewer.setTopItem(topItem);
            }

            viewer.setSelection(selection);

        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to read project structure", e);
        } finally {
            if (viewer.getTree() != null && !viewer.getTree().isDisposed()) {
                viewer.getTree().setRedraw(true);
            }
        }
    }

    private void buildTreeFor(final ProjectTreeElement parent, final IResource resource) throws CoreException {
        if (resource.getName().startsWith(".")
                || LibspecsFolder.get(resource.getProject()).getResource().equals(resource)) {
            return;
        }

        final boolean isExcluded = editorInput.getProjectConfiguration()
                .isExcludedFromValidation(resource.getProjectRelativePath().toPortableString());

        final ProjectTreeElement wrappedChild = new ProjectTreeElement(resource, isExcluded);
        parent.addChild(wrappedChild);

        if (!isExcluded && resource instanceof IContainer) {
            final IContainer childContainer = (IContainer) resource;
            for (final IResource child : childContainer.members()) {
                buildTreeFor(wrappedChild, child);
            }
        }
    }

    private void addMissingEntriesToTree(final ProjectTreeElement wrappedRoot,
            final Collection<ProjectTreeElement> allElements) {
        final Set<ProjectTreeElement> excludedShownInTree = getExcludedElementsInTheTree(allElements);
        final List<ExcludedFolderPath> allExcluded = editorInput.getProjectConfiguration().getExcludedPath();
        final List<ExcludedFolderPath> excludedNotShownInTree = getExcludedNotShownInTheTree(allExcluded,
                excludedShownInTree);

        for (final ExcludedFolderPath excludedNotShown : excludedNotShownInTree) {
            wrappedRoot.createVirtualNodeFor(Path.fromPortableString(excludedNotShown.getPath()));
        }
    }

    private Set<ProjectTreeElement> getExcludedElementsInTheTree(final Collection<ProjectTreeElement> allElements) {
        return newHashSet(filter(allElements, ProjectTreeElement::isExcluded));
    }

    private List<ExcludedFolderPath> getExcludedNotShownInTheTree(final List<ExcludedFolderPath> allExcluded,
            final Set<ProjectTreeElement> excludedShownInTree) {
        final List<ExcludedFolderPath> paths = newArrayList();

        for (final ExcludedFolderPath excludedPath : allExcluded) {
            boolean isInTree = false;
            for (final ProjectTreeElement element : excludedShownInTree) {
                if (element.getPath().equals(Path.fromPortableString(excludedPath.getPath()))) {
                    isInTree = true;
                    break;
                }
            }
            if (!isInTree) {
                paths.add(excludedPath);
            }
        }
        return paths;
    }

    private void installResourceChangeListener() {
        final IResourceChangeListener resourceListener = new IResourceChangeListener() {

            @Override
            public void resourceChanged(final IResourceChangeEvent event) {
                final AtomicBoolean shouldRefresh = new AtomicBoolean(false);

                if (event.getType() != IResourceChangeEvent.POST_CHANGE || event.getDelta() == null) {
                    return;
                }

                try {
                    event.getDelta().accept(new IResourceDeltaVisitor() {

                        @Override
                        public boolean visit(final IResourceDelta delta) throws CoreException {
                            if (editorInput.getRobotProject().getProject().equals(delta.getResource().getProject())) {
                                shouldRefresh.set(true);
                                return false;
                            }
                            return true;
                        }
                    });
                } catch (final CoreException e) {
                    // nothing to do
                }
                if (shouldRefresh.get() && viewer.getTree() != null && !viewer.getTree().isDisposed()) {
                    SwtThread.syncExec(viewer.getTree().getDisplay(), new Runnable() {

                        @Override
                        public void run() {
                            setInput();
                        }
                    });
                }
            }
        };
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
        viewer.getTree()
                .addDisposeListener(e -> ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener));
    }

    @Override
    public void setFocus() {
        viewer.getTree().setFocus();
    }

    private void setDirty(final boolean isDirty) {
        dirtyProviderService.setDirtyState(isDirty);
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        return null;
    }

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        setInput();
        viewer.getTree().setEnabled(false);
        excludeFilesBtn.setEnabled(false);
        excludeFilesTxt.setEditable(false);
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        viewer.getTree().setEnabled(editorInput.isEditable());
        excludeFilesBtn.setEnabled(editorInput.isEditable());
        excludeFilesTxt.setEditable(editorInput.isEditable());
    }

    @Inject
    @Optional
    private void whenExclusionListChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED) final RedProjectConfigEventData<Collection<IPath>> eventData) {
        // some other file model has changed
        if (!eventData.getUnderlyingFile().equals(editorInput.getRobotProject().getConfigurationFile())) {
            return;
        }

        setDirty(true);
        setInput();
    }

    private static final class ViewerSorter extends ViewerComparator {

        @Override
        public int category(final Object element) {
            return ((ProjectTreeElement) element).isInternalFolder() ? 0 : 1;
        }

        @Override
        public int compare(final Viewer viewer, final Object e1, final Object e2) {
            final int cat1 = category(e1);
            final int cat2 = category(e2);

            if (cat1 != cat2) {
                return cat1 - cat2;
            }
            final ProjectTreeElement elem1 = (ProjectTreeElement) e1;
            final ProjectTreeElement elem2 = (ProjectTreeElement) e2;

            final String name1 = elem1.getPath().removeFileExtension().lastSegment();
            final String name2 = elem2.getPath().removeFileExtension().lastSegment();

            return name1.compareToIgnoreCase(name2);
        }
    }
}
