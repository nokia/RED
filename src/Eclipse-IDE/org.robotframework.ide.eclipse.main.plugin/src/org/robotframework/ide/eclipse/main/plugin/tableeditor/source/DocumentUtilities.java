/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class DocumentUtilities {

    /**
     * Returns region around offset which constitutes a single robot variable.
     * 
     * @param document
     *            Document in which variable should be find
     * @param offset
     *            Current offset at which search should start
     * @return The region describing location of variable or absent if offset lies outside variable
     * @throws BadLocationException
     */
    public static Optional<IRegion> findVariable(final IDocument document, final int offset)
            throws BadLocationException {
        final Optional<IRegion> cellRegion = findCellRegion(document, offset);
        if (cellRegion.isPresent()) {
            final String cellContent = document.get(cellRegion.get().getOffset(), cellRegion.get().getLength());

            final Matcher matcher = Pattern.compile("[@$&%]\\{[^\\}]+\\}").matcher(cellContent);
            while (matcher.find()) {
                final int start = matcher.start() + cellRegion.get().getOffset();
                final int end = matcher.end() + cellRegion.get().getOffset();
                if (Range.closed(start, end).contains(offset)) {
                    return Optional.<IRegion> of(new Region(start, end - start));
                }
            }
            return Optional.absent();
        }
        return Optional.absent();
    }

    /**
     * Returns region around offset which constitutes a cell in robot file table. The region
     * is surrounded with file begin or cells separator on the left and by the file end or another
     * cells separator on right.
     * Cell separator is at least 2 spaces, tabulator or newline character
     * 
     * @param document
     *            Document in which cell should be find
     * @param offset
     *            Current offset at which search should start
     * @return The region describing whole cell or absent if offset is inside cell separator. If returned region 
     * is present then it is always true that:
     *     region.getOffset() <= offset <= region.getOffset() + region.getLength() 
     * @throws BadLocationException
     */
    public static Optional<IRegion> findCellRegion(final IDocument document, final int offset)
            throws BadLocationException {
        final String prev = offset > 0 ? document.get(offset - 1, 1) : "";
        final String next = offset < document.getLength() ? document.get(offset, 1) : "";
        if (isInsideSeparator(prev, next)) {
            return Optional.absent();
        }

        final int beginOffset = offset - calculateCellRegionBegin(document, offset);
        final int endOffset = offset + calculateCellRegionEnd(document, offset);
        return Optional.<IRegion> of(new Region(beginOffset, endOffset - beginOffset));
    }

    private static boolean isInsideSeparator(final String prev, final String next) {
        return !prev.isEmpty() && (Character.isWhitespace(prev.charAt(0)) || prev.charAt(0) == '|') && !next.isEmpty()
                && (Character.isWhitespace(next.charAt(0)) || next.charAt(0) == '|');
    }

    private static int calculateCellRegionBegin(final IDocument document, final int caretOffset)
            throws BadLocationException {
        int j = 1;
        while (true) {
            if (caretOffset - j < 0) {
                break;
            }
            final String prev = document.get(caretOffset - j, 1);
            if (prev.equals("\t") || prev.equals("\r") || prev.equals("\n")) {
                break;
            }

            if (caretOffset - j - 1 < 0) {
                if (!prev.equals(" ")) {
                    j++;
                }
                break;
            }
            if (prev.equals(" ")) {
                final char lookBack = document.get(caretOffset - j - 1, 1).charAt(0);
                if (Character.isWhitespace(lookBack) || lookBack == '|') {
                    break;
                }
            }
            j++;
        }
        return j - 1;
    }

    private static int calculateCellRegionEnd(final IDocument document, final int caretOffset)
            throws BadLocationException {
        int i = 0;
        while (true) {
            if (caretOffset + i >= document.getLength()) {
                break;
            }
            final String next = document.get(caretOffset + i, 1);
            if (next.equals("\t") || next.equals("\r") || next.equals("\n")) {
                break;
            }
            if (caretOffset + i + 1 >= document.getLength()) {
                if (!next.equals(" ")) {
                    i++;
                }
                break;
            }
            if (next.equals(" ")) {
                final char lookAhead = document.get(caretOffset + i + 1, 1).charAt(0);
                if (Character.isWhitespace(lookAhead) || lookAhead == '|') {
                    break;
                }
            }
            i++;
        }
        return i;
    }

    public static String getDelimiter(final IDocument document) {
        try {
            final String delimiter = document.getLineDelimiter(0);
            if (delimiter != null) {
                return delimiter;
            }
        } catch (final BadLocationException e) {
            // ok just get it from preferences
        }

        final IScopeContext[] context = new IScopeContext[] { InstanceScope.INSTANCE, ConfigurationScope.INSTANCE,
                DefaultScope.INSTANCE };
        final String delimiter = Platform.getPreferencesService().getString(Platform.PI_RUNTIME,
                Platform.PREF_LINE_SEPARATOR, null, context);
        return delimiter != null ? delimiter : "\n";
    }
}
