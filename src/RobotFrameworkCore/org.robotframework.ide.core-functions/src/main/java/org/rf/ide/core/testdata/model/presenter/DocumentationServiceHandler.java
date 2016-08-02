/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class DocumentationServiceHandler {

    private final static Pattern LINE_CONTINUE = Pattern.compile("^\\n\\s*[...]\\s*$");

    /**
     * Consolidate documentation text to one String for edit. The escaped characters will be
     * transformed to what will be shown in RF report output.
     * 
     * @param documentation
     * @return
     */
    public static String toShowConsolidated(final IDocumentationHolder documentation) {
        return consolidate(documentation, true);
    }

    /**
     * Consolidate documentation text to one String for edit. The escaped characters will be leave
     * as they are.
     * 
     * @param documentation
     * @return
     */
    public static String toEditConsolidated(final IDocumentationHolder documentation) {
        return consolidate(documentation, false);
    }

    private static String consolidate(final IDocumentationHolder documentation, final boolean shouldUnescapeTokens) {
        synchronized (documentation) {
            final StringBuilder text = new StringBuilder();

            int currentLineNr = -1;
            boolean prevNewLine = false;

            final List<RobotToken> docText = documentation.getDocumentationText();
            final int nrOfDocTokens = docText.size();
            for (int tokId = 0; tokId < nrOfDocTokens; tokId++) {
                final RobotToken docPart = docText.get(tokId);

                final int tokenLineNr = docPart.getLineNumber();
                final String tokenText = docPart.getText();

                if (tokId == 0) {
                    if (tokenText.trim().equals("...") || tokenText.trim().equals("\n...")) {
                        currentLineNr = tokenLineNr;
                        prevNewLine = true;
                        continue;
                    } else if (tokenLineNr == documentation.getBeginPosition().getLine()) {
                        currentLineNr = tokenLineNr;
                    } else {
                        currentLineNr = documentation.getBeginPosition().getLine();
                    }
                }

                if (currentLineNr < tokenLineNr && tokenText.trim().isEmpty()) {
                    continue;
                } else if (LINE_CONTINUE.matcher(tokenText).find() || tokenText.equals("\n...")
                        || (currentLineNr < tokenLineNr && tokenText.trim().equals("..."))) {
                    text.append("\n");
                    currentLineNr = tokenLineNr;
                    prevNewLine = true;
                    continue;
                } else if (currentLineNr != tokenLineNr && tokenLineNr != FilePosition.NOT_SET) {
                    text.append("\n");
                    currentLineNr = tokenLineNr;
                } else if (tokId > 0 && text.length() > 0) {
                    if (!prevNewLine && !tokenText.isEmpty()) {
                        text.append(" ");
                    }
                }

                prevNewLine = false;
                if (shouldUnescapeTokens) {
                    text.append(unescape(tokenText));
                } else {
                    text.append(tokenText);
                }
            }

            return text.toString();
        }
    }

    private static String unescape(final String text) {
        String newText = text;
        if (text != null) {
            StringBuilder unescape = new StringBuilder("");

            int nrOfEscape = 0;
            char[] chars = text.toCharArray();
            for (char c : chars) {
                if (c == '\\') {
                    unescape.append(c);
                    nrOfEscape++;
                } else {
                    if (nrOfEscape > 0) {
                        if (nrOfEscape == 1) {
                            if (c == 'n') {
                                c = '\n';
                            } else if (c == 't') {
                                c = '\t';
                            } else if (c == 'r') {
                                c = '\r';
                            }
                        }

                        unescape.setCharAt(unescape.length() - 1, c);
                    } else {
                        unescape.append(c);
                    }

                    nrOfEscape = 0;
                }
            }

            if (nrOfEscape > 0) {
                newText = unescape.substring(0, unescape.length() - 1);
            } else {
                newText = unescape.toString();
            }
        }

        return newText;
    }

    /**
     * Updates documentation in model.
     * 
     * @param documentation
     * @param newDocumentation
     */
    public static void update(final IDocumentationHolder documentation, final String newDocumentation) {
        synchronized (documentation) {
            if (newDocumentation == null || newDocumentation.isEmpty()) {
                documentation.clearDocumentation();
                return;
            }

            if (!toEditConsolidated(documentation).equals(newDocumentation)) {
                documentation.clearDocumentation();
                final List<String> lines = splitToLines(newDocumentation);
                final int numberOfLines = lines.size();

                if (numberOfLines > 0) {
                    RobotToken tok = new RobotToken();
                    tok.setText(smartTrimmedRight(lines.get(0)));
                    documentation.addDocumentationText(tok);

                    for (int lineNr = 1; lineNr < numberOfLines; lineNr++) {
                        RobotToken newLine = new RobotToken();
                        newLine.setText("\n...");
                        documentation.addDocumentationText(newLine);

                        final String lineText = lines.get(lineNr);

                        if (!lineText.isEmpty()) {
                            RobotToken docPart = new RobotToken();
                            docPart.setText(smartTrimmedRight(lineText));
                            documentation.addDocumentationText(docPart);
                        }
                    }
                }
            }
        }
    }

    private static String smartTrimmedRight(final String current) {
        String t = current;

        final char[] cArray = current.toCharArray();
        final int size = cArray.length;
        int toCut = 0;
        int escapeChars = 0;
        for (int i = size - 1; i >= 0; i--) {
            final char theChar = cArray[i];
            if (theChar == ' ') {
                toCut++;
            } else if (theChar == '\\') {
                escapeChars++;
            } else {
                break;
            }
        }

        if (toCut > 0) {
            if (escapeChars == 1) {
                t = current.substring(0, size - (toCut - 1));
            } else {
                t = current.substring(0, size - toCut);
            }
        }

        return t;
    }

    private static List<String> splitToLines(final String newDocumentation) {
        final List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new StringReader(newDocumentation))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            // shouldn't happen is not i/o but string
        }

        return lines;
    }
}
