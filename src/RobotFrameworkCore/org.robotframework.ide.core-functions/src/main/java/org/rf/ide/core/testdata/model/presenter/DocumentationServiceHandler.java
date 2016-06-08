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

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class DocumentationServiceHandler {

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
        final StringBuilder text = new StringBuilder();

        int currentLineNr = -1;

        final List<RobotToken> docText = documentation.getDocumentationText();
        final int nrOfDocTokens = docText.size();
        for (int tokId = 0; tokId < nrOfDocTokens; tokId++) {
            final RobotToken docPart = docText.get(tokId);

            final int tokenLineNr = docPart.getLineNumber();
            final String tokenText = docPart.getText();

            if (tokId == 0) {
                if (tokenText.trim().equals("...")) {
                    currentLineNr = tokenLineNr;
                    continue;
                } else if (tokenLineNr == documentation.getBeginPosition().getLine()) {
                    currentLineNr = tokenLineNr;
                }
            }

            if (tokenText.equals("\n...")) {
                text.append("\n");
                currentLineNr = tokenLineNr;
                continue;
            } else if (currentLineNr != tokenLineNr && tokenLineNr != FilePosition.NOT_SET) {
                text.append("\n");
                currentLineNr = tokenLineNr;
            } else if (tokId > 0 && text.length() > 0) {
                text.append(" ");
            }

            if (shouldUnescapeTokens) {
                text.append(unescape(tokenText));
            } else {
                text.append(tokenText);
            }
        }

        return text.toString();
    }

    private static String unescape(final String text) {
        return text;
    }

    /**
     * Updates documentation in model.
     * 
     * @param documentation
     * @param newDocumentation
     */
    public static void update(final IDocumentationHolder documentation, final String newDocumentation) {
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
                tok.setText(lines.get(0));
                documentation.addDocumentationText(tok);

                for (int lineNr = 1; lineNr < numberOfLines; lineNr++) {
                    RobotToken newLine = new RobotToken();
                    newLine.setText("\n...");
                    documentation.addDocumentationText(newLine);

                    RobotToken docPart = new RobotToken();
                    docPart.setText(lines.get(lineNr));
                    documentation.addDocumentationText(docPart);
                }
            }
        }
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
