/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.custom.StyleRange;
import org.junit.Test;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;
import org.robotframework.red.graphics.ImagesManager;

public class ExecutionViewLabelProviderTest {

    private final ExecutionViewLabelProvider labelProvider = new ExecutionViewLabelProvider();

    @Test
    public void nodeWithoutElapsedTime_hasOnlyNameInLabel() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "Suite");

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo("Suite");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void nodeWithElapsedTime_hasNameFollowedByTheTimeInSeconds() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "Suite");
        node.setElapsedTime(1234);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("Suite (%.3f s)", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();
        
        assertThat(ranges).hasSize(1);
        assertThat(ranges[0].start).isEqualTo(5);
        assertThat(ranges[0].length).isEqualTo(10);
        assertThat(ranges[0].foreground).isEqualTo(RedTheme.getEclipseDecorationColor());
    }

    @Test
    public void nodeWithoutAKind_hasNoImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, null, "a");
        assertThat(labelProvider.getImage(node)).isNull();
    }

    @Test
    public void suiteNodeWithoutStatus_hasSimpleSuiteImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "a");
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuiteImage()));
    }

    @Test
    public void suiteNodeUnderExecution_hasRunningSuiteImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "a");
        node.setStatus(Status.RUNNING);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuiteInProgressImage()));
    }

    @Test
    public void suiteNodeWhichPassed_hasPassedSuiteImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "a");
        node.setStatus(Status.PASS);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuitePassImage()));
    }

    @Test
    public void suiteNodeWhichFailed_hasFailedSuiteImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "a");
        node.setStatus(Status.FAIL);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuiteFailImage()));
    }

    @Test
    public void testNodeWithoutStatus_hasSimpleTestImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.TEST, "a");
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestImage()));
    }

    @Test
    public void testNodeUnderExecution_hasRunningTestImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.TEST, "a");
        node.setStatus(Status.RUNNING);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestInProgressImage()));
    }

    @Test
    public void testNodeWhichPassed_hasPassedTestImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.TEST, "a");
        node.setStatus(Status.PASS);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestPassImage()));
    }

    @Test
    public void testNodeWhichFailed_hasFailedTestImage() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.TEST, "a");
        node.setStatus(Status.FAIL);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestFailImage()));
    }

}
