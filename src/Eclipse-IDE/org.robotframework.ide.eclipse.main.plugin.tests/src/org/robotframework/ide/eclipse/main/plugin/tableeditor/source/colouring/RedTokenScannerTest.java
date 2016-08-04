package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.line;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.lines;

import java.io.File;
import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokensSource.LineElement;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Optional;

public class RedTokenScannerTest {

    private static final String PROJECT_NAME = RedTokenScannerTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    private static RobotParser parser;

    private static File file;

    @BeforeClass
    public static void beforeSuite() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotParserConfig cfg = new RobotParserConfig();
        cfg.setEagerImport(false);
        cfg.setIncludeImportVariables(false);
        parser = RobotParser.create(robotProject.getRobotProjectHolder(), cfg);
        file = new File("file.robot");
    }

    @Test
    public void onlyDefaultTokensAreReturned_whenThereAreNoRules() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final RedTokenScanner scanner = createScanner();
        scanner.setRange(0, 0, 36, lines);

        IToken nextToken = scanner.nextToken();
        while (!nextToken.isEOF()) {
            assertThat(nextToken).isSameAs(ISyntaxColouringRule.DEFAULT_TOKEN);
            nextToken = scanner.nextToken();
        }
    }

    @Test
    public void onlyDefaultTokensAreReturned_whenRulesDoesNotMatchAnything() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final RedTokenScanner scanner = createScanner(nonMatchingRule());
        scanner.setRange(0, 0, 36, lines);

        IToken nextToken = scanner.nextToken();
        while (!nextToken.isEOF()) {
            assertThat(nextToken).isSameAs(ISyntaxColouringRule.DEFAULT_TOKEN);
            nextToken = scanner.nextToken();
        }
    }

    @Test
    public void tokenAreProperlyReturned_whenRulesMatchesSomeOfThem() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final RedTokenScanner scanner = createScanner(textMatchingRule("bbbb", new Token("b_token")),
                textMatchingRule("eeee", new Token("e_token")));
        scanner.setRange(0, 0, 36, lines);

        int matchingTokens = 0;
        IToken nextToken = scanner.nextToken();
        while (!nextToken.isEOF()) {
            if (nextToken != ISyntaxColouringRule.DEFAULT_TOKEN) {
                matchingTokens++;

                assertThat(nextToken.getData()).isIn("b_token", "e_token");
                if (nextToken.getData().equals("b_token")) {
                    assertThat(scanner.getTokenOffset()).isEqualTo(4);
                } else if (nextToken.getData().equals("e_token")) {
                    assertThat(scanner.getTokenOffset()).isEqualTo(16);
                }
                assertThat(scanner.getTokenLength()).isEqualTo(4);
            }
            nextToken = scanner.nextToken();
        }
        assertThat(matchingTokens).isEqualTo(2);
    }

    @Test
    public void tokenAreProperlyReturned_whenRulesMatchesOnlyPartsOfTokens() throws Exception {
        final List<RobotLine> lines = lines(
                line(0, new LineElement(0, 0, 0, "aaaa"), new LineElement(0, 4, 4, "bbbb"), new LineElement(0, 8, 8, "cccc")),
                line(1, new LineElement(1, 0, 12, "dddd"), new LineElement(1, 4, 16, "eeee"), new LineElement(1, 8, 20, "ffff")),
                line(2, new LineElement(2, 0, 24, "gggg"), new LineElement(2, 4, 28, "hhhh"), new LineElement(2, 8, 32, "iiii")));

        final RedTokenScanner scanner = createScanner(partiallyMatchingRule("gggg", new Token("g_token")));
        scanner.setRange(0, 0, 36, lines);

        int matchingTokens = 0;
        IToken nextToken = scanner.nextToken();
        while (!nextToken.isEOF()) {
            if (nextToken != ISyntaxColouringRule.DEFAULT_TOKEN) {
                matchingTokens++;

                assertThat(nextToken.getData()).isEqualTo("g_token");
                assertThat(scanner.getTokenOffset()).isIn(25, 26);
                assertThat(scanner.getTokenLength()).isEqualTo(1);
            }
            nextToken = scanner.nextToken();
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
                    final List<IRobotLineElement> analyzedTokens) {
                if (robotToken.getText().equals(text)) {
                    return Optional.of(
                            new PositionedTextToken(token, robotToken.getStartOffset(), robotToken.getText().length()));
                }
                return Optional.absent();
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
            public Optional<PositionedTextToken> evaluate(final IRobotLineElement robotToken, final int offsetInToken,
                    final List<IRobotLineElement> analyzedTokens) {
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
                return Optional.absent();
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
                    final List<IRobotLineElement> analyzedTokens) {
                return Optional.absent();
            }
        };
    }

    private static RedTokenScanner createScanner(final ISyntaxColouringRule... rules) {
        return new RedTokenScanner(parser, file, rules);
    }
}
