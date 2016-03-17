/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.Selections;

/**
 * @author mmarzec
 *
 */
@SuppressWarnings("PMD.GodClass")
public class SuitesToRunComposite {

    private CheckboxTreeViewer treeViewer;

    private List<SuiteLaunchElement> treeViewerInput = newArrayList();

    public CheckboxTreeViewer createCheckboxTreeViewer(final Composite parent) {
        treeViewer = new CheckboxTreeViewer(parent, SWT.MULTI | SWT.BORDER | SWT.CHECK);
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .span(1, 4)
                .hint(SWT.DEFAULT, 130)
                .applyTo(treeViewer.getTree());

        treeViewer.setCheckStateProvider(new CheckStateProvider());
        treeViewer.setLabelProvider(new CheckboxTreeViewerLabelProvider());
        treeViewer.setContentProvider(new CheckboxTreeViewerContentProvider());
        
        treeViewer.setInput(treeViewerInput.toArray(new SuiteLaunchElement[treeViewerInput.size()]));
        
        ViewersConfigurator.enableDeselectionPossibility(treeViewer);

        return treeViewer;
    }

    public void initLaunchElements(final String projectName, final List<String> suites, final List<String> testCases) {
        treeViewerInput.clear();
        treeViewer.setInput(null);
        treeViewer.refresh();
 
        if (projectName != null && !projectName.equals("")) {
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            for (String suitePath : suites) {
                final IPath path = Path.fromPortableString(suitePath);
                final IResource resource = project.findMember(path);
                String suiteName = "";
                if (resource != null) {
                    suiteName = RobotLaunchConfigurationDelegate.createSuiteName(resource);
                }
                SuiteLaunchElement suiteElement = null;
                if (resource instanceof IFile) {
                    suiteElement = new SuiteLaunchElement(suitePath, suiteName, new ArrayList<TestCaseLaunchElement>());
                    createTestCasesLaunchElements((IFile) resource, suiteName, suiteElement, testCases);
                } else if (resource instanceof IFolder) {
                    suiteElement = new SuiteLaunchElement(suitePath, suiteName, new ArrayList<TestCaseLaunchElement>());
                    suiteElement.setIsFolder(true);
                }
                if (suiteElement != null) {
                    treeViewerInput.add(suiteElement);
                }
            }
            if(suites.isEmpty()) {
                TagsProposalsSupport.setProject(project);
            }
            treeViewer.setInput(treeViewerInput.toArray(new SuiteLaunchElement[treeViewerInput.size()]));
            treeViewer.refresh();
        }
    }
    
    private void createTestCasesLaunchElements(final IFile suiteFile, final String suiteName,
            final SuiteLaunchElement suiteElement, final List<String> testCasesFromLaunchConfig) {
        final List<RobotCase> testCasesFromSuite = extractTestCasesFromSuiteFile(suiteFile, suiteElement);
        boolean hasAllTestCasesIncludedInLaunchConfig = true;
        if (testCasesFromLaunchConfig != null) {
            hasAllTestCasesIncludedInLaunchConfig = hasAllTestCasesIncluded(testCasesFromLaunchConfig, testCasesFromSuite, suiteName);
        }
        for (RobotCase testCase : testCasesFromSuite) {
            final String testCaseFullName = suiteName + "." + testCase.getName();
            final TestCaseLaunchElement testCaseElement = new TestCaseLaunchElement(testCase.getName(), testCaseFullName,
                    suiteElement);
            if (hasAllTestCasesIncludedInLaunchConfig) {
                testCaseElement.setChecked(true);
            } else if (testCasesFromLaunchConfig != null && !testCasesFromLaunchConfig.contains(testCaseFullName)) {
                testCaseElement.setChecked(false);
            }
            suiteElement.addChild(testCaseElement);
        }
    }
    
    private List<RobotCase> extractTestCasesFromSuiteFile(final IFile suiteFile, final SuiteLaunchElement suiteElement) {
        final List<RobotCase> testCasesList = newArrayList();
        final RobotSuiteFile robotSuiteFile = RedPlugin.getModelManager().createSuiteFile(suiteFile);
        if (robotSuiteFile != null) {
            TagsProposalsSupport.clearProjectTagProposals();
            TagsProposalsSupport.extractTagProposalsFromSettingsTable(robotSuiteFile);
            for (RobotSuiteFileSection robotSection : robotSuiteFile.getSections()) {
                if (robotSection instanceof RobotCasesSection) {
                    for (RobotElement testCasesElement : robotSection.getChildren()) {
                        if (testCasesElement instanceof RobotCase) {
                            testCasesList.add((RobotCase) testCasesElement);
                            TagsProposalsSupport.extractTagProposalsFromTestCaseTable(testCasesElement,
                                    suiteElement.getPath());
                        }
                    }
                    return testCasesList;
                }
            }
        }
        return testCasesList;
    }
    
    public void addSuiteElement(final Object dialogResult, final String suitePath, final String suiteName) {

        SuiteLaunchElement suiteElement = new SuiteLaunchElement(suitePath, suiteName,
                new ArrayList<TestCaseLaunchElement>());
        if (dialogResult instanceof IFile) {
            createTestCasesLaunchElements((IFile) dialogResult, suiteName, suiteElement, null);
        } else {
            suiteElement.setIsFolder(true);
        }

        if (!treeViewerInput.contains(suiteElement)) {
            treeViewerInput.add(suiteElement);
            treeViewer.setInput(treeViewerInput.toArray(new SuiteLaunchElement[treeViewerInput.size()]));
            treeViewer.refresh();
        }
    }
    
    public void removeSuiteElements(List<SuiteLaunchElement> selectedElements) {
        final List<String> suitesPathList = new ArrayList<>();
        for (SuiteLaunchElement suiteLaunchElement : selectedElements) {
            suitesPathList.add(suiteLaunchElement.getPath());
        }
        TagsProposalsSupport.removeTagsProposals(suitesPathList);
        
        treeViewerInput.removeAll(selectedElements);
        treeViewer.setInput(treeViewerInput.toArray(new SuiteLaunchElement[treeViewerInput.size()]));
        treeViewer.refresh();
    }

    public void setLaunchElementsChecked(final boolean isChecked) {
        if (treeViewer.getTree().getSelectionCount() > 0) {
            List<SuiteLaunchElement> selectedSuites = Selections.getElements((TreeSelection) treeViewer.getSelection(),
                    SuiteLaunchElement.class);
            setSuitesChecked(selectedSuites, isChecked);
            List<TestCaseLaunchElement> selectedTestCases = Selections.getElements(
                    (TreeSelection) treeViewer.getSelection(), TestCaseLaunchElement.class);
            for (TestCaseLaunchElement testCaseLaunchElement : selectedTestCases) {
                if (!testCaseLaunchElement.getParent().isChecked()) {
                    testCaseLaunchElement.getParent().setChecked(isChecked);
                }
                testCaseLaunchElement.setChecked(isChecked);
            }
        } else {
            setSuitesChecked(treeViewerInput, isChecked);
        }
        treeViewer.refresh();
    }

    private void setSuitesChecked(List<SuiteLaunchElement> suiteList, final boolean isChecked) {
        for (SuiteLaunchElement suiteLaunchElement : suiteList) {
            suiteLaunchElement.setChecked(isChecked);
            for (TestCaseLaunchElement testCaseElement : suiteLaunchElement.getChildren()) {
                testCaseElement.setChecked(isChecked);
            }
        }
    }

    public void updateCheckState(final Object element, final boolean isElementChecked) {
        if (element instanceof SuiteLaunchElement) {
            final SuiteLaunchElement suite = ((SuiteLaunchElement) element);
            if (isElementChecked == false) {
                suite.setChecked(false);
                for (TestCaseLaunchElement testCaseElement : suite.getChildren()) {
                    testCaseElement.setChecked(false);
                }
            } else {
                suite.setChecked(true);
            }
        } else if (element instanceof TestCaseLaunchElement) {
            final TestCaseLaunchElement testCase = (TestCaseLaunchElement) element;
            if (!testCase.getParent().isChecked) {
                testCase.getParent().setChecked(isElementChecked);
            }
            testCase.setChecked(isElementChecked);
        }
        treeViewer.refresh();
    }

    public CheckboxTreeViewer getViewer() {
        return treeViewer;
    }
    
    public List<String> extractCheckedSuitesPaths() {
        final List<String> suitesPaths = newArrayList();
        for (SuiteLaunchElement suite : treeViewerInput) {
            if (suite.isChecked() && (suite.isFolder() || hasCheckedChildren(suite))) {
                suitesPaths.add(suite.getPath());
            }
        }
        return suitesPaths;
    }

    public List<String> extractCheckedTestCasesNames() {
        final List<String> testCasesNames = newArrayList();
        for (SuiteLaunchElement suite : treeViewerInput) {
            if (suite.isChecked() && hasCheckedChildren(suite)) {
                if (hasCheckedAllChildren(suite)) {
                    testCasesNames.add(suite.getFullName() + ".*");
                } else {
                    for (TestCaseLaunchElement testCase : suite.getChildren()) {
                        if (testCase.isChecked()) {
                            testCasesNames.add(testCase.getFullName());
                        }
                    }
                }
            }
        }
        return testCasesNames;
    }
    
    private boolean hasAllTestCasesIncluded(final List<String> testCasesFromLaunchConfig,
            final List<RobotCase> testCasesFromSuite, final String suiteName) {
        for (RobotCase testCase : testCasesFromSuite) {
            final String testCaseFullName = suiteName + "." + testCase.getName();
            if (testCasesFromLaunchConfig.contains(testCaseFullName)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCheckedChildren(final SuiteLaunchElement suite) {
        for (TestCaseLaunchElement testCase : suite.getChildren()) {
            if(testCase.isChecked()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasCheckedAllChildren(final SuiteLaunchElement suite) {
        for (TestCaseLaunchElement testCase : suite.getChildren()) {
            if(!testCase.isChecked()) {
                return false;
            }
        }
        return true;
    }

    private class CheckboxTreeViewerContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        }

        @Override
        public Object[] getElements(final Object inputElement) {
            return (SuiteLaunchElement[]) inputElement;
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
            if (element instanceof SuiteLaunchElement) {
                return ((SuiteLaunchElement) element).getChildren().size() > 0;
            }
            return false;
        }

    }

    private class CheckboxTreeViewerLabelProvider extends StyledCellLabelProvider {

        @Override
        public void update(final ViewerCell cell) {
            final Object element = cell.getElement();
            if (element instanceof SuiteLaunchElement) {
                cell.setText(((SuiteLaunchElement) element).getPath());
                cell.setImage(ImagesManager.getImage(RedImages.getRobotFileImage()));
            } else if (element instanceof TestCaseLaunchElement) {
                cell.setText(((TestCaseLaunchElement) element).getName());
                cell.setImage(ImagesManager.getImage(RedImages.getTestCaseImage()));
            }
            super.update(cell);
        }
    }

    private class CheckStateProvider implements ICheckStateProvider {

        @Override
        public boolean isChecked(final Object element) {
            if (element instanceof SuiteLaunchElement) {
                return ((SuiteLaunchElement) element).isChecked();
            } else if (element instanceof TestCaseLaunchElement) {
                TestCaseLaunchElement testCase = (TestCaseLaunchElement) element;
                if (!testCase.getParent().isChecked) {
                    return false;
                }
                return testCase.isChecked();
            }
            return false;
        }

        @Override
        public boolean isGrayed(final Object element) {
            return false;
        }
    }

    public static class SuiteLaunchElement {

        private String path;
        
        private String fullName;

        private List<TestCaseLaunchElement> children;

        private boolean isChecked = true;
        
        private boolean isFolder;

        public SuiteLaunchElement(final String path, final String fullName, final List<TestCaseLaunchElement> children) {
            this.path = path;
            this.fullName = fullName;
            this.children = children;
        }

        public String getPath() {
            return path;
        }

        public void setPath(final String path) {
            this.path = path;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(final String fullName) {
            this.fullName = fullName;
        }

        public List<TestCaseLaunchElement> getChildren() {
            return children;
        }

        public void addChild(final TestCaseLaunchElement child) {
            this.children.add(child);
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(final boolean isChecked) {
            this.isChecked = isChecked;
        }

        public boolean isFolder() {
            return isFolder;
        }

        public void setIsFolder(final boolean isFolder) {
            this.isFolder = isFolder;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() == getClass()) {
                final SuiteLaunchElement other = (SuiteLaunchElement) obj;
                return Objects.equals(path, other.path) && Objects.equals(fullName, other.fullName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, fullName);
        }
    }

    public static class TestCaseLaunchElement {

        private String name;
        
        private String fullName;

        private SuiteLaunchElement parent;

        private boolean isChecked = true;

        public TestCaseLaunchElement(final String name, final String fullName, final SuiteLaunchElement parent) {
            this.name = name;
            this.fullName = fullName;
            this.parent = parent;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
        
        public String getFullName() {
            return fullName;
        }

        public void setFullName(final String fullName) {
            this.fullName = fullName;
        }

        public SuiteLaunchElement getParent() {
            return parent;
        }

        public void setParent(final SuiteLaunchElement parent) {
            this.parent = parent;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(final boolean isChecked) {
            this.isChecked = isChecked;
        }
    }
}
