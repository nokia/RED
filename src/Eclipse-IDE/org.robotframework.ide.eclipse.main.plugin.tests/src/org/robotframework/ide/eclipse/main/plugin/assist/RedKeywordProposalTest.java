/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.RedLibraryKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.RedNotAccessibleLibraryKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.RedUserKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;

import com.google.common.collect.Range;

public class RedKeywordProposalTest {

    @Test
    public void whenNameShouldNotBeQualified_contentIsOnlyKeywordName() {
        final Predicate<RedKeywordProposal> shouldBeQualified = AssistProposalPredicates.alwaysFalse();
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(), "", false,
                new Path("file.robot"), shouldBeQualified, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("Given keyword");
    }

    @Test
    public void whenNameShouldBeQualified_contentIsComposedOfSourceNameAndKeywordName() {
        final Predicate<RedKeywordProposal> shouldBeQualified = AssistProposalPredicates.alwaysTrue();
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(), "", false,
                new Path("file.robot"), shouldBeQualified, ProposalMatch.EMPTY);

        assertThat(proposal.getContent()).isEqualTo("Given alias.keyword");
    }

    @Test
    public void qualificationPredicateIsCalledOnlyOnce_evenWhenContentIsCalledMultipleTimes() {
        // this because it may be visible for user when we're calling predicate multiple times

        @SuppressWarnings("unchecked")
        final Predicate<RedKeywordProposal> shouldBeQualified = mock(Predicate.class);
        when(shouldBeQualified.test(any(RedKeywordProposal.class))).thenReturn(false);

        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(), "", false,
                new Path("file.robot"), shouldBeQualified, ProposalMatch.EMPTY);

        for (int i = 0; i < 5; i++) {
            assertThat(proposal.getContent()).isEqualTo("Given keyword");
        }

        // verify it was called only once
        verify(shouldBeQualified).test(any(RedKeywordProposal.class));
        verifyNoMoreInteractions(shouldBeQualified);
    }

    @Test
    public void thereAreNoArguments_whenKeywordUsesEmbeddedSyntax() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword ${a} and ${b}", ArgumentsDescriptor.createDescriptor(), "",
                false, new Path("file.robot"), AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getArguments()).isEmpty();
    }

    @Test
    public void thereAreRequiredArgumentsProvided_whenKeywordHasOnlyRequiredArguments() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b", "c");

        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword", descriptor, "", false, new Path("file.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getArguments()).containsExactly("a", "b", "c");
    }

    @Test
    public void thereAreRequiredArgumentsAndEmptyPlaceProvided_whenKeywordHasAdditionallyDefaultArguments() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b", "c=10");

        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword", descriptor, "", false, new Path("file.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getArguments()).containsExactly("a", "b", "");
    }

    @Test
    public void thereAreRequiredArgumentsAndEmptyPlaceProvided_whenKeywordHasVargs() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b", "*c");

        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword", descriptor, "", false, new Path("file.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getArguments()).containsExactly("a", "b", "");
    }

    @Test
    public void thereAreRequiredArgumentsAndEmptyPlaceProvided_whenKeywordHasKwargs() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b", "**c");

        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.LOCAL, "Given ", "keyword", descriptor, "", false, new Path("file.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getArguments()).containsExactly("a", "b", "");
    }

    @Test
    public void userKeywordImageIsUsed_whenUserKeywordIsProposed() {
        final RedKeywordProposal proposal = new RedUserKeywordProposal("source", KeywordScope.LOCAL, "Given ",
                "keyword", ArgumentsDescriptor.createDescriptor(), "", false,
                new Path("file.robot"), AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getImage()).isEqualTo(RedImages.getUserKeywordImage());
    }

    @Test
    public void libraryKeywordImageIsUsed_whenLibraryKeywordIsProposed() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(), "", false,
                new Path("file.robot"), AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getImage()).isEqualTo(RedImages.getKeywordImage());
    }

    @Test
    public void labelIsComposedOfKeywordNameAndSourceSeparatedWithHyphen_1() {
        final RedKeywordProposal proposal = new RedUserKeywordProposal("source", KeywordScope.LOCAL, "Given ",
                "keyword", ArgumentsDescriptor.createDescriptor(), "", false, new Path("file.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getLabel()).isEqualTo("keyword - file.robot");
    }

    @Test
    public void labelIsComposedOfKeywordNameAndSourceSeparatedWithHyphen_2() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(), "", false,
                new Path("file.robot"), AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.getLabel()).isEqualTo("keyword - alias");
    }

    @Test
    public void styledLabelIsComposedOfKeywordNameAndSource_theSourceIsDecorated() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(), "", false,
                new Path("file.robot"), AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        final StyledString label = proposal.getStyledLabel();

        final TextStyle decorationStyle = new TextStyle();
        Stylers.Common.ECLIPSE_DECORATION_STYLER.applyStyles(decorationStyle);

        assertThat(label.getString()).isEqualTo("keyword - alias");
        assertThat(label.getStyleRanges()).hasSize(2);

        assertThat(label.getStyleRanges()[0].background).isNull();
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].strikeout).isFalse();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(7);

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(decorationStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].strikeout).isFalse();
        assertThat(label.getStyleRanges()[1].start).isEqualTo(7);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(8);
    }

    @Test
    public void styledLabelOfDeprecatedKeywordIsComposedOfKeywordNameAndSource_theKeywordIsStrikeoutAndSourceIsDecorated() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(), "", true,
                new Path("file.robot"), AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        final StyledString label = proposal.getStyledLabel();

        final TextStyle decorationStyle = new TextStyle();
        Stylers.Common.ECLIPSE_DECORATION_STYLER.applyStyles(decorationStyle);

        assertThat(label.getString()).isEqualTo("keyword - alias");
        assertThat(label.getStyleRanges()).hasSize(2);

        assertThat(label.getStyleRanges()[0].background).isNull();
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].strikeout).isTrue();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(7);

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(decorationStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[1].strikeout).isFalse();
        assertThat(label.getStyleRanges()[1].start).isEqualTo(7);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(8);
    }

    @Test
    public void styledLabelIsComposedOfKeywordNameAndSource_theMatchIsHighlighted() {
        final RedKeywordProposal proposal = new RedUserKeywordProposal("source", KeywordScope.LOCAL, "Given ",
                "keyword", ArgumentsDescriptor.createDescriptor(), "", false, new Path("file.robot"),
                AssistProposalPredicates.alwaysFalse(), new ProposalMatch(Range.openClosed(0, 3)));

        final StyledString label = proposal.getStyledLabel();

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(matchStyle);

        final TextStyle decorationStyle = new TextStyle();
        Stylers.Common.ECLIPSE_DECORATION_STYLER.applyStyles(decorationStyle);

        assertThat(label.getString()).isEqualTo("keyword - file.robot");
        assertThat(label.getStyleRanges()).hasSize(3);
        assertThat(label.getStyleRanges()[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[0].foreground.getRGB()).isEqualTo(matchStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[0].borderColor.getRGB()).isEqualTo(matchStyle.borderColor.getRGB());
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[0].strikeout).isFalse();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(3);

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground).isNull();
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[1].strikeout).isFalse();
        assertThat(label.getStyleRanges()[1].start).isEqualTo(3);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(4);

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground.getRGB()).isEqualTo(decorationStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[2].strikeout).isFalse();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(7);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(13);
    }

    @Test
    public void descriptionIsComposedOfGeneralHeaderAndKeywordDocumentation_forUserKeyword() {
        final RedKeywordProposal proposal = new RedUserKeywordProposal("source", KeywordScope.LOCAL, "Given ",
                "keyword", ArgumentsDescriptor.createDescriptor("a", "*b"), "the documentation", false,
                new Path("file.robot"), AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Name: keyword\n" + "Source: User defined (file.robot)\n"
                + "Arguments: [a, *b]\n\n" + "the documentation");
    }

    @Test
    public void descriptionIsComposedOfGeneralHeaderAndKeywordDocumentation_forLibrary() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.empty(),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor("a", "*b"),
                "the documentation", false, new Path("file.robot"), AssistProposalPredicates.alwaysFalse(),
                ProposalMatch.EMPTY);

        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo(
                "Name: keyword\n" + "Source: Library (source)\n" + "Arguments: [a, *b]\n\n" + "the documentation");
    }

    @Test
    public void descriptionIsComposedOfGeneralHeaderAndKeywordDocumentation_forAliasedLibrary() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor("a", "*b"),
                "the documentation", false, new Path("file.robot"), AssistProposalPredicates.alwaysFalse(),
                ProposalMatch.EMPTY);

        assertThat(proposal.hasDescription()).isTrue();
        assertThat(proposal.getDescription()).isEqualTo("Name: keyword\n"
                + "Source: Library (alias - alias for source)\n" + "Arguments: [a, *b]\n\n" + "the documentation");
    }

    @Test
    public void keywordIsAccessible_forUserKeyword() {
        final RedKeywordProposal proposal = new RedUserKeywordProposal("source", KeywordScope.LOCAL, "Given ",
                "keyword", ArgumentsDescriptor.createDescriptor(), "the documentation", false, new Path("file.robot"),
                AssistProposalPredicates.alwaysFalse(), ProposalMatch.EMPTY);

        assertThat(proposal.isAccessible()).isTrue();
    }

    @Test
    public void keywordIsAccessible_forLibraryKeyword() {
        final RedKeywordProposal proposal = new RedLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(),
                "the documentation", false, new Path("file.robot"), AssistProposalPredicates.alwaysFalse(),
                ProposalMatch.EMPTY);

        assertThat(proposal.isAccessible()).isTrue();
    }

    @Test
    public void keywordIsNotAccessible_forNotAccessibleLibraryKeyword() {
        final RedKeywordProposal proposal = new RedNotAccessibleLibraryKeywordProposal("source", Optional.of("alias"),
                KeywordScope.STD_LIBRARY, "Given ", "keyword", ArgumentsDescriptor.createDescriptor(),
                "the documentation", false, new Path("file.robot"), AssistProposalPredicates.alwaysFalse(),
                ProposalMatch.EMPTY);

        assertThat(proposal.isAccessible()).isFalse();
    }
}
