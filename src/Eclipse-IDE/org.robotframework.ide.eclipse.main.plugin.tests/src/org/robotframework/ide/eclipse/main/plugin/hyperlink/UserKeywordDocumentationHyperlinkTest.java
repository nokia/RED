/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.hyperlink.Conditions.shellWithText;

import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;

public class UserKeywordDocumentationHyperlinkTest {

    @Test
    public void testFileHyperlinkProperties() {
        final RobotSuiteFile suiteFile = createModel();
        final RobotKeywordDefinition userKeyword = suiteFile.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0);

        final UserKeywordDocumentationHyperlink link = new UserKeywordDocumentationHyperlink(new Region(20, 50),
                suiteFile, userKeyword, "decoration");
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Documentation");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("file.robot");
        assertThat(link.additionalLabelDecoration()).isEqualTo("decoration");
        assertThat(link.getImage()).isEqualTo(RedImages.getImageForFileWithExtension("robot"));
    }

    @Test
    public void testIfPopupOpensCorrectly() {
        final RobotSuiteFile suiteFile = createModel();
        final RobotKeywordDefinition userKeyword = suiteFile.findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0);


        final Display display = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
        assertThat(display.getShells()).doesNotHave(shellWithText(KeywordDocumentationPopup.POPUP_TEXT));

        final UserKeywordDocumentationHyperlink link = new UserKeywordDocumentationHyperlink(new Region(20, 50),
                suiteFile, userKeyword, "decoration");
        link.open();
        assertThat(display.getShells()).has(shellWithText(KeywordDocumentationPopup.POPUP_TEXT));

        for (final Shell shell : display.getShells()) {
            if (shell.getText().equals(KeywordDocumentationPopup.POPUP_TEXT)) {
                shell.close();
            }
        }
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Keywords ***")
                .appendLine("myKeyword")
                .appendLine("  [Documentation]  keyword doc")
                .appendLine("  Log  10")
                .build();
    }
}
