/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule.PositionedTextToken;

public class ExecutableCallInShellRuleTest {

    @Test
    public void simpleKeywordGetsProperlyColored() {
        final ShellDocumentSession session = new ShellDocumentSession("\n");
        final int offset = session.get().getLength();
        final ShellDocument document = session.type("keyword_call  arg1  arg2")
                .continueExpr()
                .type("arg3  arg${x}")
                .execute("keyword passed!")
                .get();

        final ExecutableCallInShellRule rule = new ExecutableCallInShellRule(new Token("kw"), new Token("gherkin"),
                new Token("libPrefix"), new Token("quote"), new Token("var"));

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);

        final List<PositionedTextToken> evaluated = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .map(lineElem -> rule.evaluate(lineElem, 0, lines))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        assertThat(evaluated).hasSize(1);
        assertThat(evaluated.get(0).getToken().getData()).isEqualTo("kw");
        assertThat(evaluated.get(0).getOffset()).isEqualTo(offset);
        assertThat(evaluated.get(0).getLength()).isEqualTo("keyword_call".length());
    }

    @Test
    public void simpleKeywordWithGherkinGetsProperlyColored() {
        final ShellDocumentSession session = new ShellDocumentSession("\n");
        final int offset = session.get().getLength();
        final ShellDocument document = session.type("given keyword_call").get();

        final ExecutableCallInShellRule rule = new ExecutableCallInShellRule(new Token("kw"), new Token("gherkin"),
                new Token("lib_prefix"), new Token("quote"), new Token("var"));

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);

        final List<PositionedTextToken> evaluated1 = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .map(lineElem -> rule.evaluate(lineElem, 0, lines))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        assertThat(evaluated1).hasSize(1);

        final IRobotLineElement matchingToken = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .filter(e -> e.getStartOffset() == offset)
                .findFirst()
                .get();

        assertThat(evaluated1.get(0).getToken().getData()).isEqualTo("gherkin");
        assertThat(evaluated1.get(0).getOffset()).isEqualTo(offset);
        assertThat(evaluated1.get(0).getLength()).isEqualTo("given ".length());

        final PositionedTextToken evaluated2 = rule.evaluate(matchingToken, "given ".length(), lines).get();
        assertThat(evaluated2.getToken().getData()).isEqualTo("kw");
        assertThat(evaluated2.getOffset()).isEqualTo(offset + "given ".length());
        assertThat(evaluated2.getLength()).isEqualTo("keyword_call".length());
    }

    @Test
    public void simpleKeywordWithLibraryPrefixGetsProperlyColored() {
        final ShellDocumentSession session = new ShellDocumentSession("\n");
        final int offset = session.get().getLength();
        final ShellDocument document = session.type("library.keyword_call").get();

        final ExecutableCallInShellRule rule = new ExecutableCallInShellRule(new Token("kw"), new Token("gherkin"),
                new Token("lib_prefix"), new Token("quote"), new Token("var"));

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);

        final List<PositionedTextToken> evaluated1 = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .map(lineElem -> rule.evaluate(lineElem, 0, lines))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        assertThat(evaluated1).hasSize(1);

        final IRobotLineElement matchingToken = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .filter(e -> e.getStartOffset() == offset)
                .findFirst()
                .get();

        assertThat(evaluated1.get(0).getToken().getData()).isEqualTo("lib_prefix");
        assertThat(evaluated1.get(0).getOffset()).isEqualTo(offset);
        assertThat(evaluated1.get(0).getLength()).isEqualTo("library.".length());

        final PositionedTextToken evaluated2 = rule.evaluate(matchingToken, "library.".length(), lines).get();
        assertThat(evaluated2.getToken().getData()).isEqualTo("kw");
        assertThat(evaluated2.getOffset()).isEqualTo(offset + "library.".length());
        assertThat(evaluated2.getLength()).isEqualTo("keyword_call".length());
    }

    @Test
    public void simpleKeywordWithQuoteGetsProperlyColored() {
        final ShellDocumentSession session = new ShellDocumentSession("\n");
        final int offset = session.get().getLength();
        final ShellDocument document = session.type("keyword \"quoted\" call").get();

        final ExecutableCallInShellRule rule = new ExecutableCallInShellRule(new Token("kw"), new Token("gherkin"),
                new Token("lib_prefix"), new Token("quote"), new Token("var"));

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);

        final List<PositionedTextToken> evaluated1 = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .map(lineElem -> rule.evaluate(lineElem, 0, lines))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        assertThat(evaluated1).hasSize(1);

        final IRobotLineElement matchingToken = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .filter(e -> e.getStartOffset() == offset)
                .findFirst()
                .get();

        assertThat(evaluated1.get(0).getToken().getData()).isEqualTo("kw");
        assertThat(evaluated1.get(0).getOffset()).isEqualTo(offset);
        assertThat(evaluated1.get(0).getLength()).isEqualTo("keyword ".length());

        final PositionedTextToken evaluated2 = rule.evaluate(matchingToken, "keyword ".length(), lines).get();
        assertThat(evaluated2.getToken().getData()).isEqualTo("quote");
        assertThat(evaluated2.getOffset()).isEqualTo(offset + "keyword ".length());
        assertThat(evaluated2.getLength()).isEqualTo("\"quoted\"".length());

        final PositionedTextToken evaluated3 = rule.evaluate(matchingToken, "keyword \"quoted\"".length(), lines).get();
        assertThat(evaluated3.getToken().getData()).isEqualTo("kw");
        assertThat(evaluated3.getOffset()).isEqualTo(offset + "keyword \"quoted\"".length());
        assertThat(evaluated3.getLength()).isEqualTo(" call".length());
    }

    @Test
    public void simpleKeywordWithVariableGetsProperlyColored() {
        final ShellDocumentSession session = new ShellDocumentSession("\n");
        final int offset = session.get().getLength();
        final ShellDocument document = session.type("keyword ${x} call").get();

        final ExecutableCallInShellRule rule = new ExecutableCallInShellRule(new Token("kw"), new Token("gherkin"),
                new Token("lib_prefix"), new Token("quote"), new Token("var"));

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);

        final List<PositionedTextToken> evaluated1 = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .map(lineElem -> rule.evaluate(lineElem, 0, lines))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        assertThat(evaluated1).hasSize(1);

        final IRobotLineElement matchingToken = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .filter(e -> e.getStartOffset() == offset)
                .findFirst()
                .get();

        assertThat(evaluated1.get(0).getToken().getData()).isEqualTo("kw");
        assertThat(evaluated1.get(0).getOffset()).isEqualTo(offset);
        assertThat(evaluated1.get(0).getLength()).isEqualTo("keyword ".length());

        final PositionedTextToken evaluated2 = rule.evaluate(matchingToken, "keyword ".length(), lines).get();
        assertThat(evaluated2.getToken().getData()).isEqualTo("var");
        assertThat(evaluated2.getOffset()).isEqualTo(offset + "keyword ".length());
        assertThat(evaluated2.getLength()).isEqualTo("${x}".length());

        final PositionedTextToken evaluated3 = rule.evaluate(matchingToken, "keyword ${x}".length(), lines).get();
        assertThat(evaluated3.getToken().getData()).isEqualTo("kw");
        assertThat(evaluated3.getOffset()).isEqualTo(offset + "keyword ${x}".length());
        assertThat(evaluated3.getLength()).isEqualTo(" call".length());
    }

    @Test
    public void nestedKeywordsAreProperlyColored() {
        final ShellDocumentSession session = new ShellDocumentSession("\n");
        final int offset1 = session.get().getLength();
        session.type("Run Keyword If  1==1  ");
        final int offset2 = session.get().getLength();
        session.type("kw1  5");
        session.continueExpr();
        session.type("ELSE  ");
        final int offset3 = session.get().getLength();
        session.type("kw2  10");

        final ShellDocument document = session.get();

        final ExecutableCallInShellRule rule = new ExecutableCallInShellRule(new Token("kw"), new Token("gherkin"),
                new Token("libPrefix"), new Token("quote"), new Token("var"));

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);

        final List<PositionedTextToken> evaluated = lines.stream()
                .flatMap(RobotLine::elementsStream)
                .map(lineElem -> rule.evaluate(lineElem, 0, lines))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        assertThat(evaluated).hasSize(3);
        assertThat(evaluated.get(0).getToken().getData()).isEqualTo("kw");
        assertThat(evaluated.get(0).getOffset()).isEqualTo(offset1);
        assertThat(evaluated.get(0).getLength()).isEqualTo("Run Keyword If".length());

        assertThat(evaluated.get(1).getToken().getData()).isEqualTo("kw");
        assertThat(evaluated.get(1).getOffset()).isEqualTo(offset2);
        assertThat(evaluated.get(1).getLength()).isEqualTo("kw1".length());

        assertThat(evaluated.get(2).getToken().getData()).isEqualTo("kw");
        assertThat(evaluated.get(2).getOffset()).isEqualTo(offset3);
        assertThat(evaluated.get(2).getLength()).isEqualTo("kw2".length());
    }
}
