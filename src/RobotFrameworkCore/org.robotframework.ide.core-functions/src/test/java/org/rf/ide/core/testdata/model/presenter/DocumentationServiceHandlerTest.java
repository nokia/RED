/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;

import com.google.common.base.Joiner;

public class DocumentationServiceHandlerTest {

    private final static String DIR_PATH = ".." + File.separatorChar + ".." + File.separatorChar + "model"
            + File.separatorChar + "presenter" + File.separatorChar;

    @Test
    public void test_toShowConsolidated_singleLine() throws Exception {
        final String inFileName = DIR_PATH + "DocPresentationSingleLine.robot";
        final String expectedText = "text1 text2 text3";
        assertThatViewIsTheSameAsItShouldBe(inFileName, expectedText);
    }

    @Test
    public void test_toShowConsolidated_threeLinesWithContinoue() throws Exception {
        final String inFileName = DIR_PATH + "DocPresentationThreeLinesWithContinue.robot";
        final String expectedText = "text1 text2 text3" + "\n" + "text4 text5 text6" + "\n" + "text7 text8";
        assertThatViewIsTheSameAsItShouldBe(inFileName, expectedText);
    }

    @Test
    public void test_toShowConsolidated_singleLineWithTheFirstAndMiddleAsPreviousLine() throws Exception {
        final String inFileName = DIR_PATH + "DocPresentationSingleLineWithPreviousLineAtFirstAndMiddlePosition.robot";
        final String expectedText = "text2 ... text3";
        assertThatViewIsTheSameAsItShouldBe(inFileName, expectedText);
    }

    @Test
    public void test_readAndUpdateIntegration_singleLine() throws Exception {
        final String inFileName = DIR_PATH + "DocPresentationSingleLine.robot";
        final String textToUpdate = "text1 text2 text3a";
        final String textFromTokens = textToUpdate;
        assertThatViewAfterUpdate(inFileName, textToUpdate, textFromTokens);
    }

    @Test
    public void test_readAndUpdateIntegration_threeLinesWithContinoue() throws Exception {
        final String inFileName = DIR_PATH + "DocPresentationThreeLinesWithContinue.robot";
        final String textToUpdate = "text1 text2 text3" + "\n" + "text4 text5 text6" + "\n" + "text7 text8a";
        final String textFromTokens = "text1 text2 text3" + "\n..." + "text4 text5 text6" + "\n..." + "text7 text8a";
        assertThatViewAfterUpdate(inFileName, textToUpdate, textFromTokens);
    }

    @Test
    public void test_readAndUpdateIntegration_singleLineWithTheFirstAndMiddleAsPreviousLine() throws Exception {
        final String inFileName = DIR_PATH + "DocPresentationSingleLineWithPreviousLineAtFirstAndMiddlePosition.robot";
        final String textToUpdate = "text2 ... text3a";
        final String textFromTokens = textToUpdate;
        assertThatViewAfterUpdate(inFileName, textToUpdate, textFromTokens);
    }

    private void assertThatViewIsTheSameAsItShouldBe(final String inFileName, final String expectedText)
            throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());
        final SuiteDocumentation suiteDoc = modelFile.getSettingTable().documentation().get();

        // execute
        final String toShow = DocumentationServiceHandler.toShowConsolidated(suiteDoc);

        // verify
        assertThat(toShow).isEqualTo(expectedText);

    }

    private void assertThatViewAfterUpdate(final String inFileName, final String newDocumentation,
            final String expectedText) throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());
        final SuiteDocumentation suiteDoc = modelFile.getSettingTable().documentation().get();

        // execute
        DocumentationServiceHandler.update(suiteDoc, newDocumentation);

        // verify
        assertThat(toText(text(suiteDoc.getDocumentationText()))).isEqualTo(expectedText);
    }

    private String toText(final List<String> text) {
        return Joiner.on("").join(text);
    }

    private List<String> text(final List<RobotToken> toks) {
        final List<String> wholeText = new ArrayList<>();
        for (RobotToken r : toks) {
            wholeText.add(r.getText());
        }
        return wholeText;
    }
}
