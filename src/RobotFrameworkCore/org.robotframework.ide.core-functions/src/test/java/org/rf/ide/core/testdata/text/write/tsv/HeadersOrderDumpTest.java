/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tsv;

import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.AHeadersOrderDumpTest;

/**
 * @author wypych
 */
public class HeadersOrderDumpTest extends AHeadersOrderDumpTest {

    public HeadersOrderDumpTest() {
        super("tsv", FileFormat.TSV);
    }
}
