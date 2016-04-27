/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class TestCaseNotChangedDumperTest {

    @Test
    public void dumpAsItIsInFile() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile("remainNotChanged//TestCase.txt");
        final String fileContent = DumperTestHelper.getINSTANCE().readWithLineSeparatorPresave(inputFile);

        final RobotFile modelFile = RobotModelTestProvider.getModelFile(fileContent, FileFormat.TXT_OR_ROBOT,
                RobotModelTestProvider.getParser());
        final RobotFileDumper dumper = new RobotFileDumper();
        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());
        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }
}
