/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tsv;

import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.ATestCaseNotChangedDumperTest;

public class TestCaseNotChangedDumperTest extends ATestCaseNotChangedDumperTest {

    public TestCaseNotChangedDumperTest() {
        super("tsv", FileFormat.TSV);
    }
}
