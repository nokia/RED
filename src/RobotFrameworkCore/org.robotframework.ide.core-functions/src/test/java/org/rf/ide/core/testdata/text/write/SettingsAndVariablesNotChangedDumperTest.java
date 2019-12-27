/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class SettingsAndVariablesNotChangedDumperTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void dumpAsItIsInFile(final FileFormat format) throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile("remainNotChanged/SettingsAndVariables." + format.getExtension());
        final String fileContent = DumperTestHelper.getINSTANCE().readWithLineSeparatorPresave(inputFile);

        final RobotFile modelFile = RobotModelTestProvider.getModelFile(fileContent, format,
                RobotModelTestProvider.getParser());
        final RobotFileDumper dumper = new RobotFileDumper();
        final DumpContext ctx = new DumpContext(null, false);

        // execute
        final String dumpResult = dumper.dump(ctx, modelFile.getParent()).newContent();

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }
}
