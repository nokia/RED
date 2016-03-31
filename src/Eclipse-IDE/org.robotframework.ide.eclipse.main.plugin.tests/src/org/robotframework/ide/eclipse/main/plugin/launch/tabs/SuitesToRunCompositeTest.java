/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.CheckStateProvider;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.CheckboxTreeViewerContentProvider;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.CheckboxTreeViewerLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.SuiteLaunchElement;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.TestCaseLaunchElement;
import org.robotframework.red.graphics.ImagesManager;

public class SuitesToRunCompositeTest {

    @Test
    public void whenContentProviderIsAskedForElements_itReturnsArrayConvertedFromList() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final Object[] elements = provider.getElements(newArrayList("abc", "def", "ghi"));
        assertThat(elements).isEqualTo(new Object[] { "abc", "def", "ghi" });
    }

    @Test(expected = ClassCastException.class)
    public void whenContentProviderIsAskedForElementsOfNotAList_exceptionIsThrown() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        provider.getElements(new Object[] { "abc", "def", "ghi" });
    }

    @Test
    public void whenContentProviderIsAskedForChildrenOfSuite_arrayOfCasesIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
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

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);

        final Object[] children = provider.getChildren(test1);
        assertThat(children).isNull();
    }

    @Test
    public void whenContentProviderIsAskedForParentOfSuite_nullIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        assertThat(provider.getParent(suiteElement)).isNull();
    }

    @Test
    public void whenContextProviderIsAskedForParentOfTest_suiteElementIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);

        assertThat(provider.getParent(test1)).isSameAs(suiteElement);
    }

    @Test
    public void whenContextProviderIsAskedIfThereAreChildrenOfTest_falseIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);
        
        assertThat(provider.hasChildren(test1)).isFalse();
    }

    @Test
    public void whenContextProviderIsAskedIfThereAreChildrenOfSuiteContainingTest_trueIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test2", true, false);
        suiteElement.addChild(test1);

        assertThat(provider.hasChildren(suiteElement)).isTrue();
    }

    @Test
    public void whenContextProviderIsAskedIfThereAreChildrenOfSuiteWithoutTests_falseIsReturned() {
        final CheckboxTreeViewerContentProvider provider = new CheckboxTreeViewerContentProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));

        assertThat(provider.hasChildren(suiteElement)).isFalse();
    }

    @Test
    public void whenLabelProviderIsAskedForLabelOfASuite_pathIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getProjectRelativePath()).thenReturn(Path.fromPortableString("a/b/c/suite.robot"));
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource);

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
    public void whenLabelProviderIsAskedForImageOfExistingFolderSuite_folderImageIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FOLDER);
        when(resource.exists()).thenReturn(true);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource);

        assertThat(provider.getImage(suiteElement)).isEqualTo(ImagesManager.getImage(RedImages.getFolderImage()));
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfNonExistingFolderSuite_folderImageWithErrorIconIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FOLDER);
        when(resource.exists()).thenReturn(false);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource);

        final Image expectedImage = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getFolderImage()),
                        RedImages.getErrorImage(), IDecoration.BOTTOM_LEFT));

        assertThat(provider.getImage(suiteElement)).isEqualTo(expectedImage);
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfExistingFileSuite_fileImageIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FILE);
        when(resource.exists()).thenReturn(true);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource);

        assertThat(provider.getImage(suiteElement)).isEqualTo(ImagesManager.getImage(RedImages.getRobotFileImage()));
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfNonExistingFileSuite_fileImageWithErrorIconIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final IResource resource = mock(IResource.class);
        when(resource.getType()).thenReturn(IResource.FILE);
        when(resource.exists()).thenReturn(false);
        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(resource);

        final Image expectedImage = ImagesManager
                .getImage(new DecorationOverlayIcon(ImagesManager.getImage(RedImages.getRobotFileImage()),
                        RedImages.getErrorImage(), IDecoration.BOTTOM_LEFT));

        assertThat(provider.getImage(suiteElement)).isEqualTo(expectedImage);
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfExistingTestCase_testCaseImageIsReturned() {
        final CheckboxTreeViewerLabelProvider provider = new CheckboxTreeViewerLabelProvider();

        final TestCaseLaunchElement testElement = new TestCaseLaunchElement(null, "test", true, false);

        assertThat(provider.getImage(testElement)).isEqualTo(ImagesManager.getImage(RedImages.getTestCaseImage()));
    }

    @Test
    public void whenLabelProviderIsAskedForImageOfNonExistingTestCase_testCaseImageWithErrorIconIsReturned() {
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

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));

        assertThat(provider.isChecked(suiteElement)).isTrue();
        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForCheckStateOfSuite_theCheckStateIsReturned_2() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        suiteElement.setChecked(false);

        assertThat(provider.isChecked(suiteElement)).isFalse();
        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForGrayedStateOfSuiteWithAllTestsChecked_falseIsReturned() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test1", true, false);
        final TestCaseLaunchElement test2 = new TestCaseLaunchElement(suiteElement, "test1", true, false);
        suiteElement.addChild(test1);
        suiteElement.addChild(test2);

        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForGrayedStateOfSuiteWithAllTestsUnchecked_falseIsReturned() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
        final TestCaseLaunchElement test1 = new TestCaseLaunchElement(suiteElement, "test1", false, false);
        final TestCaseLaunchElement test2 = new TestCaseLaunchElement(suiteElement, "test1", false, false);
        suiteElement.addChild(test1);
        suiteElement.addChild(test2);

        assertThat(provider.isGrayed(suiteElement)).isFalse();
    }

    @Test
    public void whenCheckProviderIsAskedForGrayedStateOfSuiteWithSomeTestsChecked_trueIsReturned() {
        final CheckStateProvider provider = new CheckStateProvider();

        final SuiteLaunchElement suiteElement = new SuiteLaunchElement(mock(IResource.class));
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
}
