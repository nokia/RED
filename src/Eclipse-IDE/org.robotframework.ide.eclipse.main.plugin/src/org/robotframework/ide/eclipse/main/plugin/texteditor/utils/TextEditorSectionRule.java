package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

public class TextEditorSectionRule extends MultiLineRule {

    private static class DecreasingCharArrayLengthComparator implements Comparator<char[]> {

        @Override
        public int compare(final char[] o1, final char[] o2) {
            return o2.length - o1.length;
        }
    }

    private final Comparator<char[]> lineDelimiterComparator = new DecreasingCharArrayLengthComparator();

    private char[][] lineDelimiters;

    private char[][] sortedLineDelimiters;

    private final List<String> headersList;

    public TextEditorSectionRule(final String startSequence, final String endSequence, final IToken token, final char escapeCharacter,
            final boolean breaksOnEOL, final boolean breaksOnEOF, final List<String> headersList) {
        super(startSequence, endSequence, token, escapeCharacter, breaksOnEOL);
        this.fBreaksOnEOF = breaksOnEOF;
        this.headersList = headersList;
    }

    @Override
    protected boolean endSequenceDetected(final ICharacterScanner scanner) {

        final char[][] originalDelimiters = scanner.getLegalLineDelimiters();
        int count = originalDelimiters.length;
        if (lineDelimiters == null || lineDelimiters.length != count) {
            sortedLineDelimiters = new char[count][];
        } else {
            while (count > 0 && Arrays.equals(lineDelimiters[count - 1], originalDelimiters[count - 1])) {
                count--;
            }
        }
        if (count != 0) {
            lineDelimiters = originalDelimiters;
            System.arraycopy(lineDelimiters, 0, sortedLineDelimiters, 0, lineDelimiters.length);
            Arrays.sort(sortedLineDelimiters, lineDelimiterComparator);
        }

        int readCount = 1;
        int c;
        while ((c = scanner.read()) != ICharacterScanner.EOF) {
            if (c == fEscapeCharacter) {
                // Skip escaped character(s)
                if (fEscapeContinuesLine) {
                    c = scanner.read();
                    for (int i = 0; i < sortedLineDelimiters.length; i++) {
                        if (c == sortedLineDelimiters[i][0]
                                && sequenceDetected(scanner, sortedLineDelimiters[i], fBreaksOnEOF)) {
                            break;
                        }
                    }
                } else {
                    scanner.read();
                }

            } else if (fEndSequence.length > 0 && c == fEndSequence[0]) {
                // Check if the specified end sequence has been found.
                if (sequenceDetected(scanner, fEndSequence, fBreaksOnEOF)) {

                    // Find header and go back to beginning
                    for (final String header : headersList) {
                        if (sequenceDetected(scanner, header.toCharArray(), fBreaksOnEOF)) {
                            for (int j = header.length(); j > 0; j--) {
                                scanner.unread();
                            }
                            return true;
                        }
                    }

                }
            } else if (fBreaksOnEOL) {
                // Check for end of line since it can be used to terminate the pattern.
                for (int i = 0; i < sortedLineDelimiters.length; i++) {
                    if (c == sortedLineDelimiters[i][0]
                            && sequenceDetected(scanner, sortedLineDelimiters[i], fBreaksOnEOF)) {
                        return true;
                    }
                }
            }
            readCount++;
        }

        if (fBreaksOnEOF) {
            return true;
        }

        for (; readCount > 0; readCount--) {
            scanner.unread();
        }

        return false;
    }
}
