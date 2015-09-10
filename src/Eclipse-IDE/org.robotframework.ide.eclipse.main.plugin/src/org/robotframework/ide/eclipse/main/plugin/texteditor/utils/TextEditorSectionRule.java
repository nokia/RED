package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

public class TextEditorSectionRule extends MultiLineRule {

    private static class DecreasingCharArrayLengthComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return ((char[]) o2).length - ((char[]) o1).length;
        }
    }

    private Comparator fLineDelimiterComparator = new DecreasingCharArrayLengthComparator();

    private char[][] fLineDelimiters;

    private char[][] fSortedLineDelimiters;

    private List<String> headersList;

    public TextEditorSectionRule(String startSequence, String endSequence, IToken token, char escapeCharacter,
            boolean breaksOnEOL, boolean breaksOnEOF, List<String> headersList) {
        super(startSequence, endSequence, token, escapeCharacter, breaksOnEOL);
        fBreaksOnEOF = breaksOnEOF;
        this.headersList = headersList;
    }

    protected boolean endSequenceDetected(ICharacterScanner scanner) {

        char[][] originalDelimiters = scanner.getLegalLineDelimiters();
        int count = originalDelimiters.length;
        if (fLineDelimiters == null || fLineDelimiters.length != count) {
            fSortedLineDelimiters = new char[count][];
        } else {
            while (count > 0 && Arrays.equals(fLineDelimiters[count - 1], originalDelimiters[count - 1]))
                count--;
        }
        if (count != 0) {
            fLineDelimiters = originalDelimiters;
            System.arraycopy(fLineDelimiters, 0, fSortedLineDelimiters, 0, fLineDelimiters.length);
            Arrays.sort(fSortedLineDelimiters, fLineDelimiterComparator);
        }

        int readCount = 1;
        int c;
        while ((c = scanner.read()) != ICharacterScanner.EOF) {
            if (c == fEscapeCharacter) {
                // Skip escaped character(s)
                if (fEscapeContinuesLine) {
                    c = scanner.read();
                    for (int i = 0; i < fSortedLineDelimiters.length; i++) {
                        if (c == fSortedLineDelimiters[i][0]
                                && sequenceDetected(scanner, fSortedLineDelimiters[i], fBreaksOnEOF))
                            break;
                    }
                } else
                    scanner.read();

            } else if (fEndSequence.length > 0 && c == fEndSequence[0]) {
                // Check if the specified end sequence has been found.
                if (sequenceDetected(scanner, fEndSequence, fBreaksOnEOF)) {

                    // Find header and go back to beginning
                    for (String header : headersList) {
                        if (sequenceDetected(scanner, header.toCharArray(), fBreaksOnEOF)) {
                            for (int j = header.length(); j > 0; j--)
                                scanner.unread();
                            return true;
                        }
                    }

                }
            } else if (fBreaksOnEOL) {
                // Check for end of line since it can be used to terminate the pattern.
                for (int i = 0; i < fSortedLineDelimiters.length; i++) {
                    if (c == fSortedLineDelimiters[i][0]
                            && sequenceDetected(scanner, fSortedLineDelimiters[i], fBreaksOnEOF))
                        return true;
                }
            }
            readCount++;
        }

        if (fBreaksOnEOF)
            return true;

        for (; readCount > 0; readCount--)
            scanner.unread();

        return false;
    }
}
