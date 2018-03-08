/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;

public class ChangeToFixerTest {

    @Test
    public void documentIsProperlyChangedByChangeToFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(5);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(8);

        final Document document = new Document("abc  def  ghi", "jkl  mno  pqr");
        final ChangeToFixer fixer = new ChangeToFixer("xyz");
        final Stream<IDocument> changedDocuments = Stream.of(fixer)
                .map(Fixers.byApplyingToDocument(marker, document, null));

        assertThat(fixer.getLabel()).isEqualTo("Change to 'xyz'");
        assertThat(changedDocuments).containsExactly(new Document("abc  xyz  ghi", "jkl  mno  pqr"));
    }

}
