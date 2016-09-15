/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

/**
 * @author wypych
 */
public class StyledTextCaretPositionProviderTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    private StyledText styledText;

    private Shell shell;

    @Test
    public void test_windowsEOLsHandling_whenUserClickOnEndOfLineAtFirstLine_shouldReturnCorrectPosition()
            throws InterruptedException {
        // given
        styledText.setText("\n\rfoobar\n\rpixel\n\r");
        Rectangle textBounds = styledText.getTextBounds(0, styledText.getCharCount() - 1);

        // when
        final int beginOfTheFirstLine = StyledTextCaretPositionProvider.getOffset(styledText, new Point(0, 0));
        final int justFewPointsAfterTheFirstLineBegin = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(10, 0));
        final int theEndOfTheLine = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(textBounds.width - 1, 0));

        // then
        assertThat(beginOfTheFirstLine).isEqualTo(0);
        assertThat(justFewPointsAfterTheFirstLineBegin).isEqualTo(0);
        assertThat(theEndOfTheLine).isEqualTo(0);
    }

    @Test
    public void test_windowsEOLsHandling_whenUserClickOnEndOfLineAtSecondLine_shouldReturnCorrectPosition()
            throws InterruptedException {
        // given
        styledText.setText("\n\rfoobar\n\rpixel\n\r");
        Rectangle textBounds = styledText.getTextBounds(0, styledText.getCharCount() - 1);

        // when
        final int offsetOfSecondLine = styledText.getOffsetAtLine(2);
        Point secondLineLocation = styledText.getLocationAtOffset(offsetOfSecondLine);
        final int beginOfTheSecondLine = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(0, secondLineLocation.y));
        final int justFewPointsAfterTheSecondLineBegin = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(10, secondLineLocation.y));
        final int theEndOfTheLine = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(textBounds.width - 1, secondLineLocation.y));

        // then
        assertThat(beginOfTheSecondLine).isEqualTo(offsetOfSecondLine);
        // characters are 10 pixels
        assertThat(justFewPointsAfterTheSecondLineBegin).isEqualTo(offsetOfSecondLine + 2);
        assertThat(theEndOfTheLine).isEqualTo(offsetOfSecondLine + styledText.getLine(2).length());
    }

    @Test
    public void test_windowsEOLsHandling_whenUserClickOnEndOfLineAtThridLine_shouldReturnCorrectPosition()
            throws InterruptedException {
        // given
        styledText.setText("\n\rfoobar\n\rpixel\n\r");
        Rectangle textBounds = styledText.getTextBounds(0, styledText.getCharCount() - 1);

        // when
        final int offsetOfThirdLine = styledText.getOffsetAtLine(3);
        Point thirdLineLocation = styledText.getLocationAtOffset(offsetOfThirdLine);
        final int beginOfTheThridLine = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(0, thirdLineLocation.y));
        final int justFewPointsAfterTheThirdLineBegin = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(10, thirdLineLocation.y));
        final int theEndOfTheLine = StyledTextCaretPositionProvider.getOffset(styledText,
                new Point(textBounds.width - 1, thirdLineLocation.y));

        // then
        assertThat(beginOfTheThridLine).isEqualTo(offsetOfThirdLine);
        // characters are 10 pixels
        assertThat(justFewPointsAfterTheThirdLineBegin).isEqualTo(offsetOfThirdLine);
        assertThat(theEndOfTheLine).isEqualTo(offsetOfThirdLine + styledText.getLine(3).length());
    }

    @Before
    public void setUp() {
        shell = shellProvider.getShell();
        shell.setText("StyledTextCaretPositionProviderTest - Test");
        shell.setLayout(new GridLayout());
        styledText = new StyledText(shell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
        Font font = new Font(shell.getDisplay(), "Arial", 10, SWT.NORMAL);
        styledText.setFont(font);

        shell.setSize(360, 120);
        shell.open();
    }

    @SuppressWarnings("unused")
    private void waitForWindowCloseForDebugPropose() {
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                // If no more entries in event queue
                shell.getDisplay().sleep();
            }
        }
    }
}
