/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;

public class DocumentationServiceHandlerTest {

    @Test
    public void test_toShowConsolidated_singleLine() throws Exception {
        // prepare
        final String inFileName = ".." + File.separatorChar + ".." + File.separatorChar + "model" + File.separatorChar
                + "presenter" + File.separatorChar + "DocPresentationSingleLine.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());
        final SuiteDocumentation suiteDoc = modelFile.getSettingTable().documentation().get();

        // execute
        final String toShow = DocumentationServiceHandler.toShowConsolidated(suiteDoc);

        // verify
        assertThat(toShow).isEqualTo("text1 text2 text3");
    }

    @Test
    public void test_toShowConsolidated_threeLinesWithContinoue() throws Exception {
        // prepare
        final String inFileName = ".." + File.separatorChar + ".." + File.separatorChar + "model" + File.separatorChar
                + "presenter" + File.separatorChar + "DocPresentationThreeLinesWithContinue.robot";
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(inFileName);
        final RobotFile modelFile = RobotModelTestProvider.getModelFile(inputFile, RobotModelTestProvider.getParser());
        final SuiteDocumentation suiteDoc = modelFile.getSettingTable().documentation().get();

        // execute
        final String toShow = DocumentationServiceHandler.toShowConsolidated(suiteDoc);

        // verify
        assertThat(toShow).isEqualTo("text1 text2 text3" + "\n" + "text4 text5 text6" + "\n" + "text7 text8");
    }
}
