package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class RedTokenScanner implements ITokenScanner, IDocumentListener {

    private final RobotParser parser;
    private final File file;

    private final List<ISyntaxColouringRule> rules;

    private Position lastTokenPosition;

    private int offsetInsideToken;

    private List<IRobotLineElement> analyzedTokens;
    private Deque<IRobotLineElement> tokensToAnalyze;

    private List<RobotLine> lines;

    private boolean listenerInstalled;

    public RedTokenScanner(final RobotParser parser, final File file, final ISyntaxColouringRule... rules) {
        this.parser = parser;
        this.file = file;
        this.rules = newArrayList(rules);
        this.listenerInstalled = false;
    }

    @Override
    public void documentAboutToBeChanged(final DocumentEvent event) {
        // we will need to reparse the file in this case
        lines = null;
    }

    @Override
    public void documentChanged(final DocumentEvent event) {
        // nothing to do
    }

    @Override
    public void setRange(final IDocument document, final int offset, final int length) {
        if (!listenerInstalled) {
            document.addDocumentListener(this);
            listenerInstalled = true;
        }

        if (lines == null) {
            final RobotFileOutput content = parser.parseEditorContent(document.get(), file);
            lines = content.getFileModel().getFileContent();
        }

        final int startingLine = DocumentUtilities.getLine(document, offset);
        setRange(startingLine, offset, length, lines);
    }

    @VisibleForTesting
    void setRange(final int startingLine, final int offset, final int length, final List<RobotLine> lines) {
        lastTokenPosition = null;
        analyzedTokens = new ArrayList<>();
        tokensToAnalyze = new RedTokensQueueBuilder().buildQueue(offset, length, lines, startingLine);

        final IRobotLineElement firstToken = tokensToAnalyze.peekFirst();
        if (firstToken != null) {
            offsetInsideToken = offset - firstToken.getStartOffset();
        }
    }

    @Override
    public IToken nextToken() {
        while (!tokensToAnalyze.isEmpty()) {
            final IRobotLineElement nextToken = tokensToAnalyze.poll();

            for (final ISyntaxColouringRule rule : rules) {
                if (!rule.isApplicable(nextToken)) {
                    continue;
                }
                final Optional<PositionedTextToken> tok = rule.evaluate(nextToken, offsetInsideToken, analyzedTokens);
                if (tok.isPresent()) {
                    final PositionedTextToken textToken = tok.get();
                    lastTokenPosition = textToken.getPosition();

                    if (lastTokenPosition.offset + lastTokenPosition.length >= nextToken.getStartOffset()
                            + nextToken.getText().length()) {
                        // rule have consumed whole Robot Token
                        offsetInsideToken = 0;
                        analyzedTokens.add(nextToken);
                    } else {
                        // the token needs more coloring, so return it to queue and shift the
                        // offset
                        offsetInsideToken = lastTokenPosition.getOffset() + lastTokenPosition.getLength()
                                - nextToken.getStartOffset();
                        tokensToAnalyze.addFirst(nextToken);
                    }

                    return textToken.getToken();
                }
            }
            lastTokenPosition = new Position(nextToken.getStartOffset(), nextToken.getText().length());
            offsetInsideToken = 0;
            analyzedTokens.add(nextToken);
            return ISyntaxColouringRule.DEFAULT_TOKEN;
        }
        return Token.EOF;
    }

    @Override
    public int getTokenOffset() {
        return lastTokenPosition.getOffset();
    }

    @Override
    public int getTokenLength() {
        return lastTokenPosition.getLength();
    }
}
