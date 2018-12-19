/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class RedSourceDoubleClickStrategy extends DefaultTextDoubleClickStrategy {

    private final boolean isTsv;

    public RedSourceDoubleClickStrategy(final boolean isTsv) {
        this.isTsv = isTsv;
    }

    @Override
    protected IRegion findWord(final IDocument document, final int offset) {
        try {
            if (containsVariableInCurrentCell(document, offset)) {
                return super.findWord(document, offset);
            }

            final Optional<IRegion> cellRegion = DocumentUtilities.findCellRegion(document, isTsv, offset);
            if (cellRegion.isPresent()) {
                return cellRegion.get();
            }
            return new Region(offset - 1, 1);

        } catch (final BadLocationException | InterruptedException e) {
            return super.findWord(document, offset);
        }
    }

    private boolean containsVariableInCurrentCell(final IDocument document, final int offset)
            throws InterruptedException {
        final RobotDocument robotDocument = (RobotDocument) document;

        return robotDocument.getNewestModel().getRobotLineBy(offset)
                .map(RobotLine::elementsStream)
                .orElseGet(() -> Stream.empty())
                .filter(elem -> elem.getStartOffset() <= offset && offset <= elem.getEndOffset())
                .filter(elem -> elem.getTypes().contains(RobotTokenType.VARIABLE_USAGE))
                .findFirst()
                .isPresent();
    }
}
