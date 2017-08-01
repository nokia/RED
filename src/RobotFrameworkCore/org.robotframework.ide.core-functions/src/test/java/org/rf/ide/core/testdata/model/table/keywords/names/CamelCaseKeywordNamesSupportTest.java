/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.Range;

public class CamelCaseKeywordNamesSupportTest {

    @Test
    public void toCamelCasePartsTest() {
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("")).isEmpty();

        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("a")).isEmpty();
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("ab")).isEmpty();
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("abc")).isEmpty();

        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("aB")).isEmpty();
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("aBc")).isEmpty();

        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("A")).containsExactly("A");
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("Ab")).containsExactly("Ab");
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("Abc")).containsExactly("Abc");

        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("AbcD")).containsExactly("Abc", "D");
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("AbcDe")).containsExactly("Abc", "De");
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("AbcDef")).containsExactly("Abc", "Def");
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("AbCdEf")).containsExactly("Ab", "Cd", "Ef");

        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("AB")).containsExactly("A", "B");
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("ABC")).containsExactly("A", "B", "C");
        assertThat(CamelCaseKeywordNamesSupport.toCamelCaseParts("ABCD")).containsExactly("A", "B", "C", "D");
    }

    @Test
    public void matchesTest() {
        assertThat(CamelCaseKeywordNamesSupport.matches("abcAbC", "")).isEmpty();
        assertThat(CamelCaseKeywordNamesSupport.matches("A", "a")).isEmpty();
        assertThat(CamelCaseKeywordNamesSupport.matches("a", "A")).isEmpty();
        assertThat(CamelCaseKeywordNamesSupport.matches("Ab cD Ef gH", "ACEG")).isEmpty();
        assertThat(CamelCaseKeywordNamesSupport.matches("Cd Ef", "AB")).isEmpty();

        assertThat(CamelCaseKeywordNamesSupport.matches("A", "A")).containsExactly(Range.closedOpen(0, 1));
        assertThat(CamelCaseKeywordNamesSupport.matches("Ab", "A")).containsExactly(Range.closedOpen(0, 1));
        assertThat(CamelCaseKeywordNamesSupport.matches("Ab T", "A")).containsExactly(Range.closedOpen(0, 1));

        assertThat(CamelCaseKeywordNamesSupport.matches("A D", "AD")).containsExactly(Range.closedOpen(0, 1),
                Range.closedOpen(2, 3));
        assertThat(CamelCaseKeywordNamesSupport.matches("A D T", "AD")).containsExactly(Range.closedOpen(0, 1),
                Range.closedOpen(2, 3));
        assertThat(CamelCaseKeywordNamesSupport.matches("Ab De", "AD")).containsExactly(Range.closedOpen(0, 1),
                Range.closedOpen(3, 4));

        assertThat(CamelCaseKeywordNamesSupport.matches("A D X", "ADX")).containsExactly(Range.closedOpen(0, 1),
                Range.closedOpen(2, 3), Range.closedOpen(4, 5));
        assertThat(CamelCaseKeywordNamesSupport.matches("A D X T", "ADX")).containsExactly(Range.closedOpen(0, 1),
                Range.closedOpen(2, 3), Range.closedOpen(4, 5));
        assertThat(CamelCaseKeywordNamesSupport.matches("Ab De Xy", "ADX")).containsExactly(Range.closedOpen(0, 1),
                Range.closedOpen(3, 4), Range.closedOpen(6, 7));

        assertThat(CamelCaseKeywordNamesSupport.matches("Ab", "Ab")).containsExactly(Range.closedOpen(0, 2));
        assertThat(CamelCaseKeywordNamesSupport.matches("Ab T", "Ab")).containsExactly(Range.closedOpen(0, 2));

        assertThat(CamelCaseKeywordNamesSupport.matches("Ab D", "AbD")).containsExactly(Range.closedOpen(0, 2),
                Range.closedOpen(3, 4));
        assertThat(CamelCaseKeywordNamesSupport.matches("Ab D T", "AbD")).containsExactly(Range.closedOpen(0, 2),
                Range.closedOpen(3, 4));
        assertThat(CamelCaseKeywordNamesSupport.matches("Abc De", "AbD")).containsExactly(Range.closedOpen(0, 2),
                Range.closedOpen(4, 5));

        assertThat(CamelCaseKeywordNamesSupport.matches("Abc Def Xyz", "AbDeX")).containsExactly(Range.closedOpen(0, 2),
                Range.closedOpen(4, 6), Range.closedOpen(8, 9));
        assertThat(CamelCaseKeywordNamesSupport.matches("Abc Def Xyz T", "AbDeX"))
                .containsExactly(Range.closedOpen(0, 2), Range.closedOpen(4, 6), Range.closedOpen(8, 9));
        assertThat(CamelCaseKeywordNamesSupport.matches("Abcde Defgh Xyz12", "AbcDefXyz"))
                .containsExactly(Range.closedOpen(0, 3), Range.closedOpen(6, 9), Range.closedOpen(12, 15));

        assertThat(CamelCaseKeywordNamesSupport.matches("Abc   Def  Xyz", "AbDeXy"))
                .containsExactly(Range.closedOpen(0, 2), Range.closedOpen(6, 8), Range.closedOpen(11, 13));
        assertThat(CamelCaseKeywordNamesSupport.matches("Abc.Def.Xyz", "AbDeXy"))
                .containsExactly(Range.closedOpen(0, 2), Range.closedOpen(4, 6), Range.closedOpen(8, 10));
    }
}
