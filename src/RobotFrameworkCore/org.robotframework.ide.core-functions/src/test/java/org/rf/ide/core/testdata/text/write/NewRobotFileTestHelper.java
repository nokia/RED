/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public class NewRobotFileTestHelper {

    public static RobotFile getModelFileToModify(final String version) {
        return new RobotFileOutput(RobotVersion.from("2.9")).getFileModel();
    }

    public static void assertNewModelTheSameAsInFile(final String fileName, final RobotFile modelFile)
            throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE().getFile(fileName);
        final String fileContent = DumperTestHelper.getINSTANCE()
                .readWithLineSeparatorPresave(inputFile)
                .replaceAll("\r\n", System.lineSeparator());
        final RobotFileDumper dumper = new RobotFileDumper();

        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();

    }
}
