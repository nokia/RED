/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class QualifiedKeywordNameTest {

    @Test
    public void testFromOccurrence() {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence("Log Many");
        assertThat(qualifiedName.getKeywordName()).isEqualTo("logmany");
        assertThat(qualifiedName.getKeywordSource()).isEmpty();
    }

    @Test
    public void testFromOccurrenceWithPrefix() {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence("BuiltIn.Log Many");
        assertThat(qualifiedName.getKeywordName()).isEqualTo("logmany");
        assertThat(qualifiedName.getKeywordSource()).isEqualTo("BuiltIn");
    }

    @Test
    public void testFromEmbeddedOccurrence() {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence("I _Execute \"ls\"");
        assertThat(qualifiedName.getEmbeddedKeywordName()).isEqualTo("i _execute \"ls\"");
        assertThat(qualifiedName.getKeywordSource()).isEmpty();
    }

    @Test
    public void testFrom_withDotsInSource() {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence("Bu.ilt.In.Log Many");
        assertThat(qualifiedName.getKeywordName()).isEqualTo("logmany");
        assertThat(qualifiedName.getKeywordSource()).isEqualTo("Bu.ilt.In");
    }

    @Test
    public void testFrom_withSpaces() {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence("Built In . Log Many");
        assertThat(qualifiedName.getKeywordName()).isEqualTo("logmany");
        assertThat(qualifiedName.getKeywordSource()).isEqualTo("Built In");
    }

    @Test
    public void testFrom_withUnderscores() {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence("Built In._Log_Many_");
        assertThat(qualifiedName.getKeywordName()).isEqualTo("logmany");
        assertThat(qualifiedName.getKeywordSource()).isEqualTo("Built In");
    }

    @Test
    public void testFrom_emptyKeyword() {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence("");
        assertThat(qualifiedName.getKeywordName()).isEmpty();
        assertThat(qualifiedName.getKeywordSource()).isEmpty();
    }

    @Test
    public void testUnifyDefinition() {
        assertThat(QualifiedKeywordName.unifyDefinition("_L og M_an y")).isEqualTo("logmany");
        assertThat(QualifiedKeywordName.unifyDefinition("I_Execute \"${cmd:(\\w+\\s*)+}\""))
                .isEqualTo("i_execute \"${cmd:(\\w+\\s*)+}\"");
    }

    @Test
    public void testIsOccurrenceEqualToDefinition() {
        assertThat(QualifiedKeywordName.isOccurrenceEqualToDefinition("BuiltIn.log many", "Log Many")).isTrue();
        assertThat(QualifiedKeywordName.isOccurrenceEqualToDefinition("log many", "Log Many")).isTrue();
        assertThat(QualifiedKeywordName.isOccurrenceEqualToDefinition("keyword.with.dots", "Keyword.With.Dots")).isTrue();
        assertThat(QualifiedKeywordName.isOccurrenceEqualToDefinition("BuiltIn.Log_Many", "Log Many")).isFalse();
        assertThat(QualifiedKeywordName.isOccurrenceEqualToDefinition("", "I_Execute \"${cmd:(\\w+\\s*)+}\"")).isTrue();
    }
}
