/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.BrowseSuitesViewerFilter;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.BrowseSuitesViewerSorter;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.CheckStateProvider;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.CheckboxTreeViewerContentProvider;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.CheckboxTreeViewerLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.CheckboxTreeViewerSorter;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.SuiteLaunchElement;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.TestCaseLaunchElement;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.ImmutableMap;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class SuitesToRunCompositeTest {

    @Project(dirs = { "nested" })
    static IProject project;

    @FreshShell
    Shell shell;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "tests1.robot", "*** Test Cases ***", "test1", "test2", "test3");
        createFile(project, "tests2.robot", "*** Test Cases ***", "test4", "test5", "test6");
        createFile(project, "tasks.robot", "*** Tasks ***", "task1", "task2", "task3");
        createFile(project, "res.robot", "*** Keywords ***", "kw1", "kw2", "kw3");

        RedPlugin.getModelManager().createProject(project).setRobotParserComplianceVersion(new RobotVersion(3, 1));
    }

    @Test
    public void emptyResultIsReturned_whenNoInputIsSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        assertThat(composite.extractSuitesToRun()).isEmpty();
    }

    @Test
    public void singleSuiteWithNoTestsIsReturned_whenAllTestsAreSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("tests1.robot", newArrayList("test1", "test2", "test3")),
                newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1).containsEntry("tests1.robot", newArrayList());
    }

    @Test
    public void singleSuiteWithSelectedTestsIsReturned_whenNotAllTestsAreSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("tests1.robot", newArrayList("test1", "test2")),
                newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1)
                .containsEntry("tests1.robot", newArrayList("test1", "test2"));
    }

    @Test
    public void singleSuiteWithNoTasksIsReturned_whenAllTasksAreSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("tasks.robot", newArrayList("task1", "task2", "task3")),
                newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1).containsEntry("tasks.robot", newArrayList());
    }

    @Test
    public void singleSuiteWithSelectedTasksIsReturned_whenNotAllTasksAreSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("tasks.robot", newArrayList("task1", "task2")),
                newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1)
                .containsEntry("tasks.robot", newArrayList("task1", "task2"));
    }

    @Test
    public void singleResourceFileIsReturned_whenResourceIsSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("res.robot", newArrayList()), newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1).containsEntry("res.robot", newArrayList());
    }

    @Test
    public void suiteFolderIsReturned_whenFolderIsSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("nested", newArrayList()), newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1).containsEntry("nested", newArrayList());
    }

    @Test
    public void suiteFolderAndSuiteWithNoTestsAreReturned_whenFolderAndSuiteWithAllTestsAreSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(),
                ImmutableMap.of("nested", newArrayList(), "tests1.robot", newArrayList("test1", "test2", "test3")),
                newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(2)
                .containsEntry("nested", newArrayList())
                .containsEntry("tests1.robot", newArrayList());
    }

    @Test
    public void emptyResultIsReturned_whenEmptyProjectNameIsSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput("", ImmutableMap.of("tests1.robot", newArrayList()), newHashSet());
        assertThat(composite.extractSuitesToRun()).isEmpty();
    }

    @Test
    public void singleSuiteWithNotExistingTestIsReturned_whenNotExistingTestIsSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("tests1.robot", newArrayList("other")), newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1).containsEntry("tests1.robot", newArrayList("other"));
    }

    @Test
    public void singleNotExistingSuiteIsReturned_whenNotExistingSuiteIsSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("other.robot", newArrayList()), newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(1).containsEntry("other.robot", newArrayList());
    }

    @Test
    public void twoSuitesWithSelectedTestsAreReturned_whenNotAllTestsAreSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(),
                ImmutableMap.of("tests1.robot", newArrayList("test1", "test2"), "tests2.robot", newArrayList("test3")),
                newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(2)
                .containsEntry("tests1.robot", newArrayList("test1", "test2"))
                .containsEntry("tests2.robot", newArrayList("test3"));
    }

    @Test
    public void twoSuitesWithNoTestsAreReturned_whenAllTestsAreSelected() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(), ImmutableMap.of("tests1.robot", newArrayList("test1", "test2", "test3"),
                "tests2.robot", newArrayList("test4", "test5", "test6")), newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(2)
                .containsEntry("tests1.robot", newArrayList())
                .containsEntry("tests2.robot", newArrayList());
    }

    @Test
    public void allSuitesAndTestsAreSelectedAndDeselectedCorrectly() throws Exception {
        final SuitesToRunComposite composite = new SuitesToRunComposite(shell, () -> {});
        composite.setInput(project.getName(),
                ImmutableMap.of("tests1.robot", newArrayList("test1"), "tests2.robot", newArrayList("test4", "test5")),
                newHashSet());
        assertThat(composite.extractSuitesToRun()).hasSize(2)
                .containsEntry("tests1.robot", newArrayList("test1"))
                .containsEntry("tests2.robot", newArrayList("test4", "test5"));
        assertThat(composite.extractUnselectedSuites()).isEmpty();

        composite.setLaunchElementsChecked(true);
        assertThat(composite.extractSuitesToRun()).hasSize(2)
                .containsEntry("tests1.robot", newArrayList())
                .containsEntry("tests2.robot", newArrayList());
        assertThat(composite.extractUnselectedSuites()).isEmpty();

        composite.setLaunchElementsChecked(false);
        assertThat(composite.extractSuitesToRun()).hasSize(2)
                .containsEntry("tests1.robot", newArrayList())
                .containsEntry("tests2.robot", newArrayList());
        assertThat(composite.extractUnselectedSuites()).containsOnly("tests1.robot", "tests2.robot");
    }

    @Test
    public void whenContentProviderIsAskedForElements_itReturnsArrayConvertedFromList() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final Object[] elements = provider.getElements(newArrayList("abc", "def", "ghi"));
        assertThat(elements).isEqualTo(new Object[] { "abc", "def", "ghi" });
    }

    @Test
    public void whenContentProviderIsAskedForElementsOfNotAList_exceptionIsThrown() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> provider.getElements(new Object[] { "abc", "def", "ghi" }));
    }

    @Test
    public void whenContentProviderIsAskedForChildrenOfSuite_arrayOfCasesIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test1", true, false);
        final TestCaseLaunchElement test2 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);
        suiteElement.addChild(test2);

        final Object[] children = provider.getChildren(suiteElement);
        assertThat(children).isEqualTo(new Object[] { test1, test2 });
    }

    @Test
    public void whenContextProviderIsAskedForChildrenOfTest_nullIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);

        final Object[] children = provider.getChildren(test1);
        assertThat(children).isNull();
    }

    @Test
    public void whenContentProviderIsAskedForParentOfSuite_nullIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        assertThat(provider.getParent(suiteElement)).isNull();
    }

    @Test
    public void whenContextProviderIsAskedForParentOfTest_suiteElementIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);

        assertThat(provider.getParent(test1)).isSameAs(suiteElement);
    }

    @Test
    public void whenContextProviderIsAskedIfThereAreChildrenOfTest_falseIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);

        assertThat(provider.hasChildren(test1)).isFalse();
    }

    @Test
    public void whenContextProviderIsAskedIfThereAreChildrenOfSuiteContainingTest_trueIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);

        assertThat(provider.hasChildren(suiteElement)).isTrue();
    }

    @Test
    public void whenContextProviderIsAskedIfThereAreChildrenOfSuiteWithoutTests_falseIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);

        assertThat(provider.hasChildren(suiteElement)).isFalse();
    }

    @Test
    public void whenLabelProviderIsAskedForLabelOfASuite_pathIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getProjectRelativePath()).thenReturn(Path.fromPortableString("a/b/c/suite.robot"));
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource, true, false);

        final StyledString styledText = provider.getStyledText(suiteElement);
        assertThat(styledText.getString()).isEqualTo("a/b/c/suite.robot");
    }

    @Test
    public void whenLabelProviderIsAskedForLabelOfATest_testNamesIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final TestCaseLaunchElement test = new TestCaseLaunchElement(null, "test from suite", true, false);

        final StyledString styledText = provider.getStyledText(test);
        assertThat(styledText.getString()).isEqualTo("test from suite");
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfFolderSuiteWithoutErrors_folderImageIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FOLDER);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource, true, false);

        assertThat(provider.getImage(suiteElement)).isEqualTo(ImagesManager.getImage(RedImages.getFolderImage()));
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfFolderSuiteWithErrors_folderImageWithErrorIconIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FOLDER);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource, true, true);

        final Image expectedImage = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getFolderImage()),
                        RedImages.getErrorImage(), IDecoration.BOTTOM_LEFT));

        assertThat(provider.getImage(suiteElement)).isEqualTo(expectedImage);
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfFileSuiteWithoutErrors_fileImageIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FILE);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource, true, false);

        assertThat(provider.getImage(suiteElement)).isEqualTo(ImagesManager.getImage(RedImages.getRobotFileImage()));
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfFileSuiteWithErrors_fileImageWithErrorIconIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FILE);
        when(resource.exists()).thenReturn(false);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource, true, true);

        final Image expectedImage = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getRobotFileImage()),
                        RedImages.getErrorImage(), IDecoration.BOTTOM_LEFT));

        assertThat(provider.getImage(suiteElement)).isEqualTo(expectedImage);
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfCaseWithoutErrors_testCaseImageIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final TestCaseLaunchElement testElement = new TestCaseLaunchElement(null, "test", true, false);

        assertThat(provider.getImage(testElement)).isEqualTo(ImagesManager.getImage(RedImages.getTestCaseImage()));
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfCaseWithErrors_testCaseImageWithErrorIconIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final TestCaseLaunchElement testElement = new TestCaseLaunchElement(null, "test", true, true);

        final Image expectedImage = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getTestCaseImage()),
                        RedImages.getErrorImage(), IDecoration.BOTTOM_LEFT));

        assertThat(provider.getImage(testElement)).isEqualTo(expectedImage);
    }

    @Test
    public void whenCheckProviderIsAskedForCheckStateOfSuite_theCheckStateIsReturned_1() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);

        assertThat(provider.isChecked(suiteElement)).isTrue();
        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForCheckStateOfSuite_theCheckStateIsReturned_2() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), false, false);

        assertThat(provider.isChecked(suiteElement)).isFalse();
        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForGrayedStateOfSuiteWithAllTestsChecked_falseIsReturned() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test1", true, false);
        final TestCaseLaunchElement test2 = new TestCaseLaunchElement(suiteElement, "test1", true, false);
        suiteElement.addChild(test1);
        suiteElement.addChild(test2);

        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForGrayedStateOfSuiteWithAllTestsUnchecked_falseIsReturned() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test1", false, false);
        final TestCaseLaunchElement test2 = new TestCaseLaunchElement(suiteElement, "test1", false, false);
        suiteElement.addChild(test1);
        suiteElement.addChild(test2);

        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForGrayedStateOfSuiteWithSomeTestsChecked_trueIsReturned() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test1", false, false);
        final TestCaseLaunchElement test2 = new TestCaseLaunchElement(suiteElement, "test1", true, false);
        suiteElement.addChild(test1);
        suiteElement.addChild(test2);

        assertThat(provider.isGrayed(suiteElement)).isTrue();
    }

    @Test
    public void whenCheckProviderIsAskedForCheckStateOfTest_theCheckStateIsReturned_1() {
        final CheckStateProvider provider = new CheckStateProvider();

        final TestCaseLaunchElement testElement = new TestCaseLaunchElement(null, "test", true, false);

        assertThat(provider.isChecked(testElement)).isTrue();
        assertThat(provider.isGrayed(testElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForCheckStateOfTest_theCheckStateIsReturned_2() {
        final CheckStateProvider provider = new CheckStateProvider();

        final TestCaseLaunchElement testElement = new TestCaseLaunchElement(null, "test", false, false);

        assertThat(provider.isChecked(testElement)).isFalse();
        assertThat(provider.isGrayed(testElement)).isFalse();
    }

    @Test
    public void whenCheckSorterIsAskedForCategory_ZeroIsReturnedForFolders() throws Exception {
        final CheckboxTreeViewerSorter sorter = new CheckboxTreeViewerSorter();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FOLDER);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource, true, false);

        assertThat(sorter.category(suiteElement)).isEqualTo(0);
    }

    @Test
    public void whenCheckSorterIsAskedForCategory_OneIsReturnedForFiles() throws Exception {
        final CheckboxTreeViewerSorter sorter = new CheckboxTreeViewerSorter();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FILE);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource, true, false);

        assertThat(sorter.category(suiteElement)).isEqualTo(1);
    }

    @Test
    public void whenCheckSorterIsAskedForCategory_ZeroIsReturnedForCases() throws Exception {
        final CheckboxTreeViewerSorter sorter = new CheckboxTreeViewerSorter();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement test = new TestCaseLaunchElement(suiteElement, "test", true, false);

        assertThat(sorter.category(test)).isEqualTo(0);
    }

    @Test
    public void whenCheckSorterIsAskedForCompare_folderIsBeforeFile() throws Exception {
        final CheckboxTreeViewerSorter sorter = new CheckboxTreeViewerSorter();

        final IResource folder = mock(IResource.class);
        when(folder.getType()).thenReturn(IResource.FOLDER);
        final IResource file = mock(IResource.class);
        when(file.getType()).thenReturn(IResource.FILE);
        final SuiteLaunchElement folderElement = new SuiteLaunchElement(folder, true, false);
        final SuiteLaunchElement fileElement = new SuiteLaunchElement(file, true, false);

        assertThat(sorter.compare(mock(Viewer.class), folderElement, fileElement)).isNegative();
        assertThat(sorter.compare(mock(Viewer.class), fileElement, folderElement)).isPositive();
    }

    @Test
    public void whenCheckSorterIsAskedForCompare_filesAreInNaturalOrder() throws Exception {
        final CheckboxTreeViewerSorter sorter = new CheckboxTreeViewerSorter();

        final IResource file1 = mock(IResource.class);
        when(file1.getType()).thenReturn(IResource.FILE);
        when(file1.getFullPath()).thenReturn(Path.fromPortableString("abc.robot"));
        final IResource file2 = mock(IResource.class);
        when(file2.getType()).thenReturn(IResource.FILE);
        when(file2.getFullPath()).thenReturn(Path.fromPortableString("def.robot"));
        final SuiteLaunchElement fileElement1 = new SuiteLaunchElement(file1, true, false);
        final SuiteLaunchElement fileElement2 = new SuiteLaunchElement(file2, true, false);

        assertThat(sorter.compare(mock(Viewer.class), fileElement1, fileElement2)).isNegative();
        assertThat(sorter.compare(mock(Viewer.class), fileElement2, fileElement1)).isPositive();
    }

    @Test
    public void whenCheckSorterIsAskedForCompare_testCasesAreInNaturalOrder() throws Exception {
        final CheckboxTreeViewerSorter sorter = new CheckboxTreeViewerSorter();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class), true, false);
        final TestCaseLaunchElement testElement1 = new TestCaseLaunchElement(suiteElement, "abc", true, false);
        final TestCaseLaunchElement testElement2 = new TestCaseLaunchElement(suiteElement, "def", true, false);

        assertThat(sorter.compare(mock(Viewer.class), testElement1, testElement2)).isNegative();
        assertThat(sorter.compare(mock(Viewer.class), testElement2, testElement1)).isPositive();
    }

    @Test
    public void whenBrowseSuitesSorterIsAskedForCategory_ZeroIsReturnedForFolders() throws Exception {
        final BrowseSuitesViewerSorter sorter = new BrowseSuitesViewerSorter();

        final IFolder folder = getDir(project, "nested");

        assertThat(sorter.category(folder)).isEqualTo(0);
    }

    @Test
    public void whenBrowseSuitesSorterIsAskedForCategory_OneIsReturnedForFiles() throws Exception {
        final BrowseSuitesViewerSorter sorter = new BrowseSuitesViewerSorter();

        final IFile file = getFile(project, "tasks.robot");

        assertThat(sorter.category(file)).isEqualTo(1);
    }

    @Test
    public void whenBrowseSuitesFilterIsAskedForSelect_TrueIsReturnedForProjectWithSameName() throws Exception {
        final BrowseSuitesViewerFilter filter = new BrowseSuitesViewerFilter(project.getName());

        assertThat(filter.select(mock(Viewer.class), null, project)).isTrue();
    }

    @Test
    public void whenBrowseSuitesFilterIsAskedForSelect_FalseIsReturnedForProjectWithDifferentName() throws Exception {
        final BrowseSuitesViewerFilter filter = new BrowseSuitesViewerFilter("OTHER_PROJECT");

        assertThat(filter.select(mock(Viewer.class), null, project)).isFalse();
    }

    @Test
    public void whenBrowseSuitesFilterIsAskedForSelect_TrueIsReturnedForFoldersOtherThanLibspecFolder()
            throws Exception {
        final BrowseSuitesViewerFilter filter = new BrowseSuitesViewerFilter(project.getName());

        final IFolder folder = getDir(project, "nested");

        assertThat(filter.select(mock(Viewer.class), null, folder)).isTrue();
    }

    @Test
    public void whenBrowseSuitesFilterIsAskedForSelect_FalseIsReturnedForLibspecFolder() throws Exception {
        final BrowseSuitesViewerFilter filter = new BrowseSuitesViewerFilter(project.getName());

        final IFolder folder = LibspecsFolder.get(project).getResource();

        assertThat(filter.select(mock(Viewer.class), null, folder)).isFalse();
    }

    @Test
    public void whenBrowseSuitesFilterIsAskedForSelect_TrueIsReturnedForFileWithTests() throws Exception {
        final BrowseSuitesViewerFilter filter = new BrowseSuitesViewerFilter(project.getName());

        final IFile file = getFile(project, "tests1.robot");

        assertThat(filter.select(mock(Viewer.class), null, file)).isTrue();
    }

    @Test
    public void whenBrowseSuitesFilterIsAskedForSelect_TrueIsReturnedForFileWithTasks() throws Exception {
        final BrowseSuitesViewerFilter filter = new BrowseSuitesViewerFilter(project.getName());

        final IFile file = getFile(project, "tasks.robot");

        assertThat(filter.select(mock(Viewer.class), null, file)).isTrue();
    }

    @Test
    public void whenBrowseSuitesFilterIsAskedForSelect_FalseIsReturnedForFileWithoutCases() throws Exception {
        final BrowseSuitesViewerFilter filter = new BrowseSuitesViewerFilter(project.getName());

        final IFile file = getFile(project, "res.robot");

        assertThat(filter.select(mock(Viewer.class), null, file)).isFalse();
    }

}
