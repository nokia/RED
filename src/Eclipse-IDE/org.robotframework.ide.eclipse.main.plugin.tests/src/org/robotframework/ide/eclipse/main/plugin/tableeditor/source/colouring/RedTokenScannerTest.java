/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.line;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.lines;

import java.io.File;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.LineElement;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Supplier;

public class RedTokenScannerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedTokenScannerTest.class);

    private static RobotDocument document;

    @BeforeClass
    public static void beforeSuite() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotParser parser = RobotParser.create(robotProject.getRobotProjectHolder(),
                RobotParserConfig.allImportsLazy(new RobotVersion(3, 0)));
        final File file = new File("file.robot");

        document = new RobotDocument(parser, file);
    }

    @Test
    public void onlyDefaultTokensAreReturned_whenThereAreNoRules() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final Supplier<Deque<IRobotLineElement>> supplier = new Supplier<Deque<IRobotLineElement>>() {
            @Override
            public Deque<IRobotLineElement> get() {
                return new RedTokensQueueBuilder().buildQueue(0, 38, lines, 0);
            }
        };

        final RedTokenScanner scanner = createScanner();
        scanner.setRange(document, 0, 38);

        IToken nextToken = scanner.nextToken(supplier);
        while (!nextToken.isEOF()) {
            assertThat(nextToken).isSameAs(ISyntaxColouringRule.DEFAULT_TOKEN);
            nextToken = scanner.nextToken(supplier);
        }
    }

    @Test
    public void onlyDefaultTokensAreReturned_whenThereAreNoApplicableRules() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final Supplier<Deque<IRobotLineElement>> supplier = new Supplier<Deque<IRobotLineElement>>() {
            @Override
            public Deque<IRobotLineElement> get() {
                return new RedTokensQueueBuilder().buildQueue(0, 38, lines, 0);
            }
        };

        final RedTokenScanner scanner = createScanner(nonApplicableRule(new Token("token")));
        scanner.setRange(document, 0, 38);

        IToken nextToken = scanner.nextToken(supplier);
        while (!nextToken.isEOF()) {
            assertThat(nextToken).isSameAs(ISyntaxColouringRule.DEFAULT_TOKEN);
            nextToken = scanner.nextToken(supplier);
        }
    }

    @Test
    public void onlyDefaultTokensAreReturned_whenRulesDoesNotMatchAnything() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));
        final Supplier<Deque<IRobotLineElement>> supplier = new Supplier<Deque<IRobotLineElement>>() {
            @Override
            public Deque<IRobotLineElement> get() {
                return new RedTokensQueueBuilder().buildQueue(0, 38, lines, 0);
            }
        };

        final RedTokenScanner scanner = createScanner(nonMatchingRule());
        scanner.setRange(document, 0, 38);

        IToken nextToken = scanner.nextToken(supplier);
        while (!nextToken.isEOF()) {
            assertThat(nextToken).isSameAs(ISyntaxColouringRule.DEFAULT_TOKEN);
            nextToken = scanner.nextToken(supplier);
        }
    }

    @Test
    public void tokenAreProperlyReturned_whenRulesMatchesSomeOfThem() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 13, "dddd"), new LineElement(1, 4, 17, "eeee"), new LineElement(1, 8, 21, "ffff")),
                line(2, new LineElement(2, 0, 26, "gggg"), new LineElement(2, 4, 30, "hhhh"), new LineElement(2, 8, 34, "iiii")));

        final Supplier<Deque<IRobotLineElement>> supplier = new Supplier<Deque<IRobotLineElement>>() {
            @Override
            public Deque<IRobotLineElement> get() {
                return new RedTokensQueueBuilder().buildQueue(0, 38, lines, 0);
            }
        };

        final RedTokenScanner scanner = createScanner(textMatchingRule("bbbb", new Token("b_token")),
                textMatchingRule("eeee", new Token("e_token")));
        scanner.setRange(document, 0, 38);

        int matchingTokens = 0;
        IToken nextToken = scanner.nextToken(supplier);
        while (!nextToken.isEOF()) {
            if (nextToken != ISyntaxColouringRule.DEFAULT_TOKEN) {
                matchingTokens++;

                assertThat(nextToken.getData()).isIn("b_token", "e_token");
                if (nextToken.getData().equals("b_token")) {
                    assertThat(scanner.getTokenOffset()).isEqualTo(4);
                } else if (nextToken.getData().equals("e_token")) {
                    assertThat(scanner.getTokenOffset()).isEqualTo(17);
                }
                assertThat(scanner.getTokenLength()).isEqualTo(4);
            }
            nextToken = scanner.nextToken(supplier);
        }
        assertThat(matchingTokens).isEqualTo(2);
    }

    @Test
    public void tokenAreProperlyReturned_whenRulesMatchesOnlyPartsOfTokens() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 13, "dddd"), new LineElement(1, 4, 17, "eeee"), new LineElement(1, 8, 21, "ffff")),
                line(2, new LineElement(2, 0, 26, "gggg"), new LineElement(2, 4, 30, "hhhh"), new LineElement(2, 8, 34, "iiii")));

        final Supplier<Deque<IRobotLineElement>> supplier = new Supplier<Deque<IRobotLineElement>>() {
            @Override
            public Deque<IRobotLineElement> get() {
                return new RedTokensQueueBuilder().buildQueue(0, 38, lines, 0);
            }
        };

        final RedTokenScanner scanner = createScanner(partiallyMatchingRule("gggg", new Token("g_token")));
        scanner.setRange(document, 0, 38);

        int matchingTokens = 0;
        IToken nextToken = scanner.nextToken(supplier);
        while (!nextToken.isEOF()) {
            if (nextToken != ISyntaxColouringRule.DEFAULT_TOKEN) {
                matchingTokens++;

                assertThat(nextToken.getData()).isEqualTo("g_token");
                assertThat(scanner.getTokenOffset()).isIn(27, 28);
                assertThat(scanner.getTokenLength()).isEqualTo(1);
            }
            nextToken = scanner.nextToken(supplier);
        }
        assertThat(matchingTokens).isEqualTo(2);
    }

    private static ISyntaxColouringRule textMatchingRule(final String text, final IToken token) {
        return new ISyntaxColouringRule() {

            @Override
            public boolean isApplicable(final IRobotLineElement nextToken) {
                return true;
            }

            @Override
            public Optional<PositionedTextToken> evaluate(final IRobotLineElement robotToken, final int offsetInToken,
                    final List<RobotLine> context) {
                if (robotToken.getText().equals(text)) {
                    return Optional.of(
                            new PositionedTextToken(token, robotToken.getStartOffset(), robotToken.getText().length()));
                }
                return Optional.empty();
            }
        };
    }

    private static ISyntaxColouringRule partiallyMatchingRule(final String text, final IToken token) {
        return new ISyntaxColouringRule() {

            @Override
            public boolean isApplicable(final IRobotLineElement nextToken) {
                return true;
            }

            @Override
            public Optional<PositionedTextToken> evaluate(final IRobotLineElement robotToken,
                    final int offsetInToken, final List<RobotLine> context) {
                if (robotToken.getText().equals(text)) {
                    // matches only second and third character inside token
                    if (offsetInToken >= 1 && offsetInToken <= 2) {
                        return Optional.of(
                                new PositionedTextToken(token, robotToken.getStartOffset() + offsetInToken, 1));
                    } else {
                        return Optional.of(new PositionedTextToken(ISyntaxColouringRule.DEFAULT_TOKEN,
                                robotToken.getStartOffset() + offsetInToken, 1));
                    }
                }
                return Optional.empty();
            }
        };
    }

    private static ISyntaxColouringRule nonMatchingRule() {
        return new ISyntaxColouringRule() {

            @Override
            public boolean isApplicable(final IRobotLineElement nextToken) {
                return true;
            }

            @Override
            public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
                    final List<RobotLine> context) {
                return Optional.empty();
            }
        };
    }

    private static ISyntaxColouringRule nonApplicableRule(final IToken token) {
        return new ISyntaxColouringRule() {

            @Override
            public boolean isApplicable(final IRobotLineElement nextToken) {
                return false;
            }

            @Override
            public Optional<PositionedTextToken> evaluate(final IRobotLineElement robotToken, final int offsetInToken,
                    final List<RobotLine> context) {
                return Optional
                        .of(new PositionedTextToken(token, robotToken.getStartOffset(), robotToken.getText().length()));
            }
        };
    }

    private static RedTokenScanner createScanner(final ISyntaxColouringRule... rules) {
        return new RedTokenScanner(rules);
    }
}
