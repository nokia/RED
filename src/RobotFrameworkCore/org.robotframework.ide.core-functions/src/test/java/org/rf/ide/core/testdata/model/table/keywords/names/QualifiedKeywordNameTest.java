/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import org.junit.Assert;
import org.junit.Test;

public class QualifiedKeywordNameTest {

    @Test
    public void testFrom() {

        String keywordName = "Log Many";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.from(keywordName);
        Assert.assertEquals("Log Many", qualifiedName.getKeywordName());
        Assert.assertEquals("", qualifiedName.getKeywordSource());

        keywordName = "BuiltIn.Log Many";
        qualifiedName = QualifiedKeywordName.from(keywordName);
        Assert.assertEquals("Log Many", qualifiedName.getKeywordName());
        Assert.assertEquals("BuiltIn", qualifiedName.getKeywordSource());
    }

    @Test
    public void testFrom_withDotsInSource() {

        String keywordName = "Bu.ilt.In.Log Many";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.from(keywordName);
        Assert.assertEquals("Log Many", qualifiedName.getKeywordName());
        Assert.assertEquals("Bu.ilt.In", qualifiedName.getKeywordSource());
    }

    @Test
    public void testFrom_withSpaces() {

        String keywordName = "Built In . Log Many";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.from(keywordName);
        Assert.assertEquals("Log Many", qualifiedName.getKeywordName());
        Assert.assertEquals("Built In", qualifiedName.getKeywordSource());
    }

    @Test
    public void testFrom_emptyKeyword() {

        String keywordName = "";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.from(keywordName);
        Assert.assertEquals("", qualifiedName.getKeywordName());
        Assert.assertEquals("", qualifiedName.getKeywordSource());
    }

}
