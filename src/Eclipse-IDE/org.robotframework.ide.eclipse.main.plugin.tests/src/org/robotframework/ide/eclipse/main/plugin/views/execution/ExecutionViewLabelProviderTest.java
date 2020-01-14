/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.DynamicFlag;
import org.robotframework.red.graphics.ImagesManager;

public class ExecutionViewLabelProviderTest {

    private final ExecutionViewLabelProvider labelProvider = new ExecutionViewLabelProvider();

    @Test
    public void nodeWithoutElapsedTime_hasOnlyNameInLabel() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo("Suite");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void nodeWithResolvedName_hasResolvedNameInLabel() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        final ExecutionTreeNode testNode = node.getTestOrCreateIfMissing("test ${x}", "test 1");

        final StyledString label = labelProvider.getStyledText(testNode);
        assertThat(label.getString()).isEqualTo("[+] test 1");
    }

    @Test
    public void nodeWithElapsedTime_hasNameFollowedByTheTimeInSeconds() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        node.setElapsedTime(1234);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("Suite (%.3f s)", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();
        
        assertThat(ranges).hasSize(1);
        assertThat(ranges[0].start).isEqualTo(5);
        assertThat(ranges[0].length).isEqualTo(10);
        assertThat(ranges[0].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
    }

    @Test
    public void addedNodeWithoutElapsedTime_hasPlusSignAndName() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        node.setDynamic(DynamicFlag.ADDED);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("[+] Suite", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();

        assertThat(ranges).hasSize(3);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(1);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(0, 150, 0));
        assertThat(ranges[1].start).isEqualTo(1);
        assertThat(ranges[1].length).isEqualTo(1);
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(new RGB(0, 150, 0));
        assertThat(ranges[2].start).isEqualTo(2);
        assertThat(ranges[2].length).isEqualTo(1);
        assertThat(ranges[2].foreground.getRGB()).isEqualTo(new RGB(0, 150, 0));

    }

    @Test
    public void addedNodeWithElapsedTime_hasPlusSignAndNameFollowedByTheTimeInSeconds() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        node.setDynamic(DynamicFlag.ADDED);
        node.setElapsedTime(1234);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("[+] Suite (%.3f s)", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();

        assertThat(ranges).hasSize(4);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(1);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(0, 150, 0));
        assertThat(ranges[1].start).isEqualTo(1);
        assertThat(ranges[1].length).isEqualTo(1);
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(new RGB(0, 150, 0));
        assertThat(ranges[2].start).isEqualTo(2);
        assertThat(ranges[2].length).isEqualTo(1);
        assertThat(ranges[2].foreground.getRGB()).isEqualTo(new RGB(0, 150, 0));
        assertThat(ranges[3].start).isEqualTo(9);
        assertThat(ranges[3].length).isEqualTo(10);
        assertThat(ranges[3].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
    }

    @Test
    public void removedNodeWithoutElapsedTime_hasMinusSignAndName() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        node.setDynamic(DynamicFlag.REMOVED);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("[-] Suite", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();

        assertThat(ranges).hasSize(3);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(1);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(200, 0, 0));
        assertThat(ranges[1].start).isEqualTo(1);
        assertThat(ranges[1].length).isEqualTo(1);
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(new RGB(200, 0, 0));
        assertThat(ranges[2].start).isEqualTo(2);
        assertThat(ranges[2].length).isEqualTo(1);
        assertThat(ranges[2].foreground.getRGB()).isEqualTo(new RGB(200, 0, 0));
    }

    @Test
    public void removedNodeWithElapsedTime_hasMinusSignAndNameFollowedByTheTimeInSeconds() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        node.setDynamic(DynamicFlag.REMOVED);
        node.setElapsedTime(1234);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("[-] Suite (%.3f s)", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();

        assertThat(ranges).hasSize(4);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(1);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(200, 0, 0));
        assertThat(ranges[1].start).isEqualTo(1);
        assertThat(ranges[1].length).isEqualTo(1);
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(new RGB(200, 0, 0));
        assertThat(ranges[2].start).isEqualTo(2);
        assertThat(ranges[2].length).isEqualTo(1);
        assertThat(ranges[2].foreground.getRGB()).isEqualTo(new RGB(200, 0, 0));
        assertThat(ranges[3].start).isEqualTo(9);
        assertThat(ranges[3].length).isEqualTo(10);
        assertThat(ranges[3].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
    }

    @Test
    public void modifiedNodeWithoutElapsedTime_hasStarSignAndName() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        node.setDynamic(DynamicFlag.OTHER);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("[*] Suite", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();

        assertThat(ranges).hasSize(3);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(1);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(0, 0, 200));
        assertThat(ranges[1].start).isEqualTo(1);
        assertThat(ranges[1].length).isEqualTo(1);
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(new RGB(0, 0, 200));
        assertThat(ranges[2].start).isEqualTo(2);
        assertThat(ranges[2].length).isEqualTo(1);
        assertThat(ranges[2].foreground.getRGB()).isEqualTo(new RGB(0, 0, 200));
    }

    @Test
    public void modifiedNodeWithElapsedTime_hasStarSignAndNameFollowedByTheTimeInSeconds() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "Suite", null);
        node.setDynamic(DynamicFlag.OTHER);
        node.setElapsedTime(1234);

        final StyledString label = labelProvider.getStyledText(node);
        assertThat(label.getString()).isEqualTo(String.format("[*] Suite (%.3f s)", 1.234));
        final StyleRange[] ranges = label.getStyleRanges();

        assertThat(ranges).hasSize(4);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(1);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(0, 0, 200));
        assertThat(ranges[1].start).isEqualTo(1);
        assertThat(ranges[1].length).isEqualTo(1);
        assertThat(ranges[1].foreground.getRGB()).isEqualTo(new RGB(0, 0, 200));
        assertThat(ranges[2].start).isEqualTo(2);
        assertThat(ranges[2].length).isEqualTo(1);
        assertThat(ranges[2].foreground.getRGB()).isEqualTo(new RGB(0, 0, 200));
        assertThat(ranges[3].start).isEqualTo(9);
        assertThat(ranges[3].length).isEqualTo(10);
        assertThat(ranges[3].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
    }

    @Test
    public void suiteNodeWithoutStatus_hasSimpleSuiteImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "a", null);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuiteImage()));
    }

    @Test
    public void suiteNodeUnderExecution_hasRunningSuiteImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "a", null);
        node.setStatus(Status.RUNNING);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuiteInProgressImage()));
    }

    @Test
    public void suiteNodeWhichPassed_hasPassedSuiteImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "a", null);
        node.setStatus(Status.PASS);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuitePassImage()));
    }

    @Test
    public void suiteNodeWhichFailed_hasFailedSuiteImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "a", null);
        node.setStatus(Status.FAIL);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getSuiteFailImage()));
    }

    @Test
    public void testNodeWithoutStatus_hasSimpleTestImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newTestNode(null, "a", null);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestImage()));
    }

    @Test
    public void testNodeUnderExecution_hasRunningTestImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newTestNode(null, "a", null);
        node.setStatus(Status.RUNNING);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestInProgressImage()));
    }

    @Test
    public void testNodeWhichPassed_hasPassedTestImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newTestNode(null, "a", null);
        node.setStatus(Status.PASS);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestPassImage()));
    }

    @Test
    public void testNodeWhichFailed_hasFailedTestImage() {
        final ExecutionTreeNode node = ExecutionTreeNode.newTestNode(null, "a", null);
        node.setStatus(Status.FAIL);
        assertThat(labelProvider.getImage(node)).isSameAs(ImagesManager.getImage(RedImages.getTestFailImage()));
    }

}
