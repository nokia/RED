/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.TreeContentProvider;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * @author mmarzec
 */
class SuitesToRunComposite2 extends Composite {

    private String projectName;

    private CheckboxTreeViewer viewer;

    private final Map<EButton, Button> buttons = new HashMap<>();

    private final List<SuiteLaunchElement> suitesToLaunch = new ArrayList<>();

    private final Collection<SuitesListener> listeners = new ArrayList<>();

    SuitesToRunComposite2(final Composite parent) {
        super(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(2).margins(2, 1).applyTo(this);
        createViewer();
        createAllButtons();
    }

    void addSuitesListener(final SuitesListener listener) {
        listeners.add(listener);
    }

    void removeSuitesListener(final SuitesListener listener) {
        listeners.remove(listener);
    }

    private void informListeners() {
        for (final SuitesListener listener : listeners) {
            listener.suitesChanged();
        }
    }

    private void createViewer() {
        viewer = new CheckboxTreeViewer(this, SWT.MULTI | SWT.BORDER | SWT.CHECK);
        viewer.setUseHashlookup(true);
        ViewersConfigurator.enableDeselectionPossibility(viewer);
        GridDataFactory.fillDefaults().grab(true, true).span(1, 5).minSize(200, 130).applyTo(viewer.getTree());

        viewer.setCheckStateProvider(new CheckStateProvider());
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new CheckboxTreeViewerLabelProvider()));
        viewer.setContentProvider(new CheckboxTreeViewerContentProvider());
        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(final CheckStateChangedEvent event) {
                final Object element = event.getElement();
                final boolean isElementChecked = event.getChecked();

                if (element instanceof SuiteLaunchElement) {
                    ((SuiteLaunchElement) element).updateChecked(isElementChecked);
                } else if (element instanceof TestCaseLaunchElement) {
                    ((TestCaseLaunchElement) element).updateChecked(isElementChecked);
                }
                informListeners();
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final Button removeButton = buttons.get(EButton.REMOVE);
                final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection.isEmpty()) {
                    removeButton.setEnabled(false);
                    return;
                }
                final List<SuiteLaunchElement> suites = Selections.getElements(selection, SuiteLaunchElement.class);
                final List<TestCaseLaunchElement> tests = Selections.getElements(selection,
                        TestCaseLaunchElement.class);
                if (!suites.isEmpty() && tests.isEmpty()) {
                    removeButton.setEnabled(true);
                } else if (suites.isEmpty() && !tests.isEmpty()) {
                    boolean allAreMissing = true;
                    for (final TestCaseLaunchElement test : tests) {
                        if (!test.isMissing) {
                            allAreMissing = false;
                            break;
                        }
                    }
                    removeButton.setEnabled(allAreMissing);
                } else {
                    removeButton.setEnabled(false);
                }
            }
        });
    }

    private void createAllButtons() {
        buttons.put(EButton.BROWSE, createBrowseButton());
        buttons.put(EButton.REMOVE, createRemoveButton());
        buttons.put(EButton.SELECT_ALL, createSelectButton());
        buttons.put(EButton.DESELECT_ALL, createDeselectButton());
    }

    private Button createBrowseButton() {
        final Button browseSuitesButton = new Button(this, SWT.PUSH);
        browseSuitesButton.setText("Browse...");
        GridDataFactory.fillDefaults().applyTo(browseSuitesButton);
        browseSuitesButton.addSelectionListener(new SelectionAdapter() {

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
                                && ((IResource) element).getProject().getName().equals(projectName);
                    }
                });
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    for (final Object obj : dialog.getResult()) {
                        final IResource chosenResource = (IResource) obj;
                        if (chosenResource.getType() == IResource.PROJECT) {
                            continue;
                        }

                        final SuiteLaunchElement suite = new SuiteLaunchElement(chosenResource);
                        final List<RobotCase> cases = extractCases(chosenResource);
                        final List<TestCaseLaunchElement> tests = transform(cases,
                                new Function<RobotCase, TestCaseLaunchElement>() {

                                    @Override
                                    public TestCaseLaunchElement apply(final RobotCase test) {
                                        return new TestCaseLaunchElement(suite, test.getName(), false, false);
                                    }
                                });
                        for (final TestCaseLaunchElement test : tests) {
                            suite.addChild(test);
                        }
                        if (!suitesToLaunch.contains(suite)) {
                            suitesToLaunch.add(suite);
                        }
                    }
                    informListeners();
                }
            }
        });
        return browseSuitesButton;
    }

    private Button createRemoveButton() {
        final Button removeSuite = new Button(this, SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(removeSuite);
        removeSuite.setText("Remove");
        removeSuite.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                final List<SuiteLaunchElement> suites = Selections.getElements(selection, SuiteLaunchElement.class);

                boolean changed = false;
                if (!suites.isEmpty()) {
                    suitesToLaunch.removeAll(suites);
                    changed = true;
                }
                final List<TestCaseLaunchElement> tests = Selections.getElements(selection,
                        TestCaseLaunchElement.class);
                for (final TestCaseLaunchElement test : tests) {
                    test.getParent().getChildren().remove(test);
                    changed = true;
                }
                if (changed) {
                    informListeners();
                }
            }
        });
        return removeSuite;
    }

    private Button createSelectButton() {
        final Button selectAll = new Button(this, SWT.PUSH);
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(selectAll);
        selectAll.setText("Select All");
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                setLaunchElementsChecked(true);
                informListeners();
            }
        });
        return selectAll;
    }

    private Button createDeselectButton() {
        final Button deselectAll = new Button(this, SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(deselectAll);
        deselectAll.setText("Deselect All");
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                setLaunchElementsChecked(false);
                informListeners();
            }
        });
        return deselectAll;
    }

    @Override
    public void dispose() {
        buttons.clear();
        super.dispose();
    }

    private void setLaunchElementsChecked(final boolean isChecked) {
        final List<TestCaseLaunchElement> testsToCheck = newArrayList();
        final List<SuiteLaunchElement> suitesToCheck = newArrayList();

        if (viewer.getTree().getSelectionCount() == 0) {
            suitesToCheck.addAll(suitesToLaunch);
        } else {
            suitesToCheck
                    .addAll(Selections.getElements((TreeSelection) viewer.getSelection(), SuiteLaunchElement.class));
            testsToCheck
                    .addAll(Selections.getElements((TreeSelection) viewer.getSelection(), TestCaseLaunchElement.class));
        }
        for (final TestCaseLaunchElement test : testsToCheck) {
            test.updateChecked(isChecked);
        }
        for (final SuiteLaunchElement suite : suitesToCheck) {
            suite.updateChecked(isChecked);
        }
    }

    void initialize(final String projectName, final Map<IResource, List<String>> suitesToRun) {
        this.projectName = projectName;
        try {
            viewer.getTree().setRedraw(false);
            final Object[] checked = viewer.getExpandedElements();

            suitesToLaunch.clear();
            viewer.setInput(null);
            viewer.refresh();

            if (projectName.isEmpty()) {
                return;
            }

            for (final Entry<IResource, List<String>> entry : suitesToRun.entrySet()) {
                final IResource resource = entry.getKey();
                final SuiteLaunchElement suite = new SuiteLaunchElement(resource);

                final List<String> allCases = newArrayList(entry.getValue());

                final List<RobotCase> casesFromFile = extractCases(resource);
                for (final RobotCase testCase : casesFromFile) {
                    final String name = testCase.getName();
                    suite.addChild(new TestCaseLaunchElement(suite, name,
                            entry.getValue().isEmpty() || entry.getValue().contains(name.toLowerCase()), false));
                    allCases.remove(name);
                }
                for (final String missingSuite : allCases) {
                    suite.addChild(new TestCaseLaunchElement(suite, missingSuite, true, true));
                }
                suitesToLaunch.add(suite);
            }
            viewer.setInput(suitesToLaunch);
            viewer.refresh();
            viewer.setExpandedElements(checked);
        } finally {
            viewer.getTree().setRedraw(true);
        }
    }

    void switchTo(final String projectName) {
        this.projectName = projectName;

        for (final SuiteLaunchElement suite : suitesToLaunch) {
            suite.updateProject(projectName);
        }

        viewer.refresh();
    }

    private static List<RobotCase> extractCases(final IResource resource) {
        final List<RobotCase> cases = new ArrayList<>();

        if (resource.exists() && resource.getType() == IResource.FILE) {
            final RobotSuiteFile suiteModel = RobotModelManager.getInstance().createSuiteFile((IFile) resource);
            final Optional<RobotCasesSection> section = suiteModel.findSection(RobotCasesSection.class);
            if (section.isPresent()) {
                cases.addAll(section.get().getChildren());
            }
        }
        return cases;
    }

    Map<String, List<String>> extractSuitesToRun() {
        final LinkedHashMap<String, List<String>> suitesToRun = new LinkedHashMap<>();

        for (final SuiteLaunchElement suite : suitesToLaunch) {
            if (suite.isChecked()) {
                final ArrayList<String> tests = new ArrayList<String>();
                if (!suite.hasCheckedAllChildren()) {
                    for (final TestCaseLaunchElement test : suite.getChildren()) {
                        if (test.isChecked()) {
                            tests.add(test.getName().toLowerCase());
                        }
                    }
                }
                suitesToRun.put(suite.getPath(), tests);
            }
        }
        return suitesToRun;
    }

    private static class CheckboxTreeViewerContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            return ((List<?>) inputElement).toArray();
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            if (parentElement instanceof SuiteLaunchElement) {
                final List<TestCaseLaunchElement> children = ((SuiteLaunchElement) parentElement).getChildren();
                return children.toArray(new TestCaseLaunchElement[children.size()]);
            }
            return null;
        }

        @Override
        public Object getParent(final Object element) {
            if (element instanceof TestCaseLaunchElement) {
                return ((TestCaseLaunchElement) element).getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(final Object element) {
            return element instanceof SuiteLaunchElement && ((SuiteLaunchElement) element).getChildren().size() > 0;
        }
    }

    private static class CheckboxTreeViewerLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof SuiteLaunchElement) {
                return new StyledString(((SuiteLaunchElement) element).getPath());
            } else if (element instanceof TestCaseLaunchElement) {
                return new StyledString(((TestCaseLaunchElement) element).getName());
            }
            return new StyledString();
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof SuiteLaunchElement) {
                return ((SuiteLaunchElement) element).getImage();
            } else if (element instanceof TestCaseLaunchElement) {
                return ((TestCaseLaunchElement) element).getImage();
            }
            return null;
        }
    }

    private static class CheckStateProvider implements ICheckStateProvider {

        @Override
        public boolean isChecked(final Object element) {
            if (element instanceof SuiteLaunchElement) {
                return ((SuiteLaunchElement) element).isChecked();
            } else if (element instanceof TestCaseLaunchElement) {
                return ((TestCaseLaunchElement) element).isChecked();
            }
            return false;
        }

        @Override
        public boolean isGrayed(final Object element) {
            if (element instanceof SuiteLaunchElement) {
                final SuiteLaunchElement suite = (SuiteLaunchElement) element;
                return suite.hasCheckedChild() && !suite.hasCheckedAllChildren();
            }
            return false;
        }
    }

    private static final class SuiteLaunchElement {

        private IResource resource;

        private final List<TestCaseLaunchElement> children;

        private boolean isChecked = true;

        SuiteLaunchElement(final IResource resource) {
            this.resource = resource;
            this.children = new ArrayList<>();
        }

        Image getImage() {
            final ImageDescriptor baseImage = isFolder() ? RedImages.getFolderImage() : RedImages.getRobotFileImage();
            if (resource.exists()) {
                return ImagesManager.getImage(baseImage);
            } else {
                return ImagesManager.getImage(new DecorationOverlayIcon(ImagesManager.getImage(baseImage),
                        RedImages.getErrorImage(), IDecoration.BOTTOM_LEFT));
            }
        }

        String getPath() {
            return resource.getProjectRelativePath().toPortableString();
        }

        boolean isFolder() {
            return resource.getType() == IResource.FOLDER;
        }

        List<TestCaseLaunchElement> getChildren() {
            return children;
        }

        void addChild(final TestCaseLaunchElement child) {
            children.add(child);
        }

        boolean isChecked() {
            return isChecked;
        }

        void setChecked(final boolean isChecked) {
            this.isChecked = isChecked;
        }

        void updateChecked(final boolean isChecked) {
            this.isChecked = isChecked;
            for (final TestCaseLaunchElement testCaseElement : children) {
                testCaseElement.setChecked(isChecked);
            }
        }

        private boolean hasCheckedChild() {
            return any(children, hasCheck());
        }

        private boolean hasCheckedAllChildren() {
            return all(children, hasCheck());
        }

        private static Predicate<TestCaseLaunchElement> hasCheck() {
            return new Predicate<TestCaseLaunchElement>() {

                @Override
                public boolean apply(final TestCaseLaunchElement test) {
                    return test.isChecked();
                }
            };
        }

        public void updateProject(final String projectName) {
            final IProject newProject = resource.getWorkspace().getRoot().getProject(projectName);
            final IPath path = resource.getProjectRelativePath();
            this.resource = path.getFileExtension() == null ? newProject.getFolder(path) : newProject.getFile(path);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() == getClass()) {
                final SuiteLaunchElement other = (SuiteLaunchElement) obj;
                return Objects.equals(resource, other.resource);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource);
        }
    }

    private static final class TestCaseLaunchElement {

        private final String name;

        private final SuiteLaunchElement parent;

        private boolean isChecked;

        private final boolean isMissing;

        TestCaseLaunchElement(final SuiteLaunchElement parent, final String name, final boolean isChecked,
                final boolean isMissing) {
            this.name = name;
            this.parent = parent;
            this.isChecked = isChecked;
            this.isMissing = isMissing;
        }

        Image getImage() {
            final ImageDescriptor baseImage = RedImages.getTestCaseImage();
            if (isMissing) {
                return ImagesManager.getImage(new DecorationOverlayIcon(ImagesManager.getImage(baseImage),
                        RedImages.getErrorImage(), IDecoration.BOTTOM_LEFT));
            } else {
                return ImagesManager.getImage(baseImage);
            }
        }

        String getName() {
            return name;
        }

        SuiteLaunchElement getParent() {
            return parent;
        }

        boolean isChecked() {
            return isChecked;
        }

        void setChecked(final boolean isChecked) {
            this.isChecked = isChecked;
        }

        private void updateChecked(final boolean isChecked) {
            this.isChecked = isChecked;
            if (isChecked) {
                parent.setChecked(isChecked);
            } else if (!parent.hasCheckedChild()) {
                parent.setChecked(false);
            }
        }
    }

    private enum EButton {
        BROWSE,
        REMOVE,
        SELECT_ALL,
        DESELECT_ALL;
    }

    interface SuitesListener {

        void suitesChanged();
    }
}
