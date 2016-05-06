/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt;

import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.AKeywordsNotChangedDumperTest;

public class KeywordsNotChangedDumperTest extends AKeywordsNotChangedDumperTest {

    public KeywordsNotChangedDumperTest() {
        super("txt", FileFormat.TXT_OR_ROBOT);
    }
}
