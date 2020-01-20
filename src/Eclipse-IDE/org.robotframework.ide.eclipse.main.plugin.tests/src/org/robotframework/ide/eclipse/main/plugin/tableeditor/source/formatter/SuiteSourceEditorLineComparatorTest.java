/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;

public class SuiteSourceEditorLineComparatorTest {

    @Test
    public void getRangeCountReturnsNumberOfLines() throws Exception {
        final Document document = new Document("line1", "line2", "line3");

        final SuiteSourceEditorLineComparator comparator = new SuiteSourceEditorLineComparator(document);

        assertThat(comparator.getRangeCount()).isEqualTo(3);
    }

    @Test
    public void skipRangeComparisonReturnsFalse() throws Exception {
        final Document document = new Document("line1", "line2", "line3");

        final SuiteSourceEditorLineComparator comparator = new SuiteSourceEditorLineComparator(document);

        assertThat(comparator.skipRangeComparison(0, 0, null)).isFalse();
    }

    @Test
    public void rangesEqualReturnsTrue_whenComparedLinesAreEqual() throws Exception {
        final Document document1 = new Document("line1", "line2", "line3", "other");
        final Document document2 = new Document("lineA", "line2", "other");

        final SuiteSourceEditorLineComparator comparator1 = new SuiteSourceEditorLineComparator(document1);
        final SuiteSourceEditorLineComparator comparator2 = new SuiteSourceEditorLineComparator(document2);

        assertThat(comparator1.rangesEqual(1, comparator2, 1)).isTrue();
        assertThat(comparator1.rangesEqual(3, comparator2, 2)).isTrue();
    }

    @Test
    public void rangesEqualReturnsFalse_whenComparedLinesAreNotEqual() throws Exception {
        final Document document1 = new Document("line1", "line2", "line3", "other");
        final Document document2 = new Document("lineA", "line2", "other");

        final SuiteSourceEditorLineComparator comparator1 = new SuiteSourceEditorLineComparator(document1);
        final SuiteSourceEditorLineComparator comparator2 = new SuiteSourceEditorLineComparator(document2);

        assertThat(comparator1.rangesEqual(0, comparator2, 0)).isFalse();
        assertThat(comparator1.rangesEqual(0, comparator2, 1)).isFalse();
        assertThat(comparator1.rangesEqual(0, comparator2, 2)).isFalse();
        assertThat(comparator1.rangesEqual(1, comparator2, 0)).isFalse();
        assertThat(comparator1.rangesEqual(1, comparator2, 2)).isFalse();
        assertThat(comparator1.rangesEqual(2, comparator2, 0)).isFalse();
        assertThat(comparator1.rangesEqual(2, comparator2, 1)).isFalse();
        assertThat(comparator1.rangesEqual(2, comparator2, 2)).isFalse();
        assertThat(comparator1.rangesEqual(3, comparator2, 0)).isFalse();
        assertThat(comparator1.rangesEqual(3, comparator2, 1)).isFalse();
    }
}
