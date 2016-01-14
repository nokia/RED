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
    public void testFromOccurrence() {

        String keywordName = "Log Many";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(keywordName);
        Assert.assertEquals("logmany", qualifiedName.getKeywordName());
        Assert.assertEquals("", qualifiedName.getKeywordSource());

        keywordName = "BuiltIn.Log Many";
        qualifiedName = QualifiedKeywordName.fromOccurrence(keywordName);
        Assert.assertEquals("logmany", qualifiedName.getKeywordName());
        Assert.assertEquals("BuiltIn", qualifiedName.getKeywordSource());
    }
    
    @Test
    public void testFromEmbeddedOccurrence() {

        String keywordName = "I _Execute \"ls\"";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(keywordName);
        Assert.assertEquals("i _execute \"ls\"", qualifiedName.getEmbeddedKeywordName());
        Assert.assertEquals("", qualifiedName.getKeywordSource());
    }
    
    @Test
    public void testFrom_withDotsInSource() {

        String keywordName = "Bu.ilt.In.Log Many";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(keywordName);
        Assert.assertEquals("logmany", qualifiedName.getKeywordName());
        Assert.assertEquals("Bu.ilt.In", qualifiedName.getKeywordSource());
    }

    @Test
    public void testFrom_withSpaces() {

        String keywordName = "Built In . Log Many";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(keywordName);
        Assert.assertEquals("logmany", qualifiedName.getKeywordName());
        Assert.assertEquals("Built In", qualifiedName.getKeywordSource());
    }
    
    @Test
    public void testFrom_withUnderscores() {

        String keywordName = "Built In._Log_Many_";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(keywordName);
        Assert.assertEquals("logmany", qualifiedName.getKeywordName());
        Assert.assertEquals("Built In", qualifiedName.getKeywordSource());
    }

    @Test
    public void testFrom_emptyKeyword() {

        String keywordName = "";
        QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(keywordName);
        Assert.assertEquals("", qualifiedName.getKeywordName());
        Assert.assertEquals("", qualifiedName.getKeywordSource());
    }

    @Test
    public void testUnifyDefinition() {

        String keywordName = "_L og M_an y";
        Assert.assertEquals("logmany", QualifiedKeywordName.unifyDefinition(keywordName));

        keywordName = "I_Execute \"${cmd:(\\w+\\s*)+}\"";
        Assert.assertEquals("i_execute \"${cmd:(\\w+\\s*)+}\"", QualifiedKeywordName.unifyDefinition(keywordName));
    }
    
    @Test
    public void testIsOccurrenceEqualToDefinition() {
        Assert.assertTrue(QualifiedKeywordName.isOccurrenceEqualToDefinition("BuiltIn.log many", "Log Many"));
        Assert.assertTrue(QualifiedKeywordName.isOccurrenceEqualToDefinition("log many", "Log Many"));
        Assert.assertFalse(QualifiedKeywordName.isOccurrenceEqualToDefinition("BuiltIn.Log_Many", "Log Many"));
        
        Assert.assertTrue(QualifiedKeywordName.isOccurrenceEqualToDefinition("", "I_Execute \"${cmd:(\\w+\\s*)+}\""));
    }
}
