/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.KeywordType;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Supplier;

public class KeywordProposalsLabelProviderTest {

    @Test
    public void normalKeywordImageIsReturnedForLibraryKeyword() {
        final KeywordProposalsLabelProvider labelProvider = new KeywordProposalsLabelProvider();
        final Image image = labelProvider
                .getImage(new KeywordContentProposal(createProposalToWrap(KeywordType.LIBRARY), ""));

        assertThat(image).isSameAs(ImagesManager.getImage(RedImages.getKeywordImage()));
    }

    @Test
    public void userKeywordImageIsReturnedForUserDefinedKeyword() {
        final KeywordProposalsLabelProvider labelProvider = new KeywordProposalsLabelProvider();
        final Image image = labelProvider
                .getImage(new KeywordContentProposal(createProposalToWrap(KeywordType.USER_DEFINED), ""));

        assertThat(image).isSameAs(ImagesManager.getImage(RedImages.getUserKeywordImage()));
    }

    @Test
    public void labelIsTheNameDecoratedWithDecoration() {
        final KeywordProposalsLabelProvider labelProvider = new KeywordProposalsLabelProvider();
        final String label = labelProvider
                .getText(new KeywordContentProposal(createProposalToWrap(KeywordType.USER_DEFINED), ""));
        assertThat(label).isEqualTo("&name decoration");
    }

    @Test
    public void styledLabelIsTheNameDecoratedWithDecoration_usingEclipseDecorationStyler() {
        final KeywordProposalsLabelProvider labelProvider = new KeywordProposalsLabelProvider();
        final StyledString label = labelProvider
                .getStyledText(new KeywordContentProposal(createProposalToWrap(KeywordType.USER_DEFINED), ""));

        assertThat(label.getString()).isEqualTo("&name decoration");
        assertThat(label.getStyleRanges()).hasSize(1);
        assertThat(label.getStyleRanges()[0].foreground.getRGB())
                .isEqualTo(RedTheme.getEclipseDecorationColor().getRGB());
        assertThat(label.getStyleRanges()[0].start).isEqualTo(5);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(11);
    }

    @Test
    public void styledLabelHasMatchingPrefixDecoratedWithBackground() {
        final KeywordProposalsLabelProvider labelProvider = new KeywordProposalsLabelProvider();
        final StyledString label = labelProvider
                .getStyledText(new KeywordContentProposal(createProposalToWrap(KeywordType.USER_DEFINED), "$NAM"));

        final TextStyle expectedStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(expectedStyle);

        assertThat(label.getString()).isEqualTo("&name decoration");
        assertThat(label.getStyleRanges()).hasSize(2);
        assertThat(label.getStyleRanges()[0].background.getRGB()).isEqualTo(expectedStyle.background.getRGB());
        assertThat(label.getStyleRanges()[0].foreground.getRGB()).isEqualTo(expectedStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[0].borderColor.getRGB()).isEqualTo(expectedStyle.borderColor.getRGB());
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(expectedStyle.borderStyle);
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(4);
        assertThat(label.getStyleRanges()[1].foreground.getRGB())
                .isEqualTo(RedTheme.getEclipseDecorationColor().getRGB());
        assertThat(label.getStyleRanges()[1].start).isEqualTo(5);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(11);
    }

    private RedKeywordProposal createProposalToWrap(final KeywordType kwType) {
        final Supplier<String> docSupplier = new Supplier<String>() {

            @Override
            public String get() {
                return "<p>doc</p>";
            }
        };
        return new RedKeywordProposal("<source>", "source-alias", KeywordScope.LOCAL, kwType, "&name", "decoration",
                true, ArgumentsDescriptor.createDescriptor("arg<>"), docSupplier, "documentation", false,
                new Path("path"));
    }
}
