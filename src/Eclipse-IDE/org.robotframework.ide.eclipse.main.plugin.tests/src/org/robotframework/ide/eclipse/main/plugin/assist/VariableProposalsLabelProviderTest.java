/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.VariableOrigin;
import org.robotframework.red.graphics.ImagesManager;

public class VariableProposalsLabelProviderTest {

    @Test
    public void properImagesAreReturnedForDifferentTypesOfVariables() {
        testReturnedImageForType(VariableType.SCALAR, RedImages.getRobotScalarVariableImage());
        testReturnedImageForType(VariableType.SCALAR_AS_LIST, RedImages.getRobotScalarVariableImage());
        testReturnedImageForType(VariableType.LIST, RedImages.getRobotListVariableImage());
        testReturnedImageForType(VariableType.DICTIONARY, RedImages.getRobotDictionaryVariableImage());
        testReturnedImageForType(VariableType.INVALID, RedImages.getRobotScalarVariableImage());
    }

    private void testReturnedImageForType(final VariableType type, final ImageDescriptor expectedImage) {
        final VariableProposalsLabelProvider labelProvider = new VariableProposalsLabelProvider();
        final Image image = labelProvider
                .getImage(new VariableContentProposal(createProposalToWrap(type), ""));

        assertThat(image).isSameAs(ImagesManager.getImage(expectedImage));
    }

    @Test
    public void labelIsTheNameDecoratedWithDecoration() {
        final VariableProposalsLabelProvider labelProvider = new VariableProposalsLabelProvider();
        final String label = labelProvider
                .getText(new VariableContentProposal(createProposalToWrap(VariableType.SCALAR), ""));

        assertThat(label).isEqualTo("${name}");
    }

    @Test
    public void styledLabelHasNoStyles_whenThereIsNoMatchingPrefix() {
        final VariableProposalsLabelProvider labelProvider = new VariableProposalsLabelProvider();
        final StyledString label = labelProvider
                .getStyledText(new VariableContentProposal(createProposalToWrap(VariableType.SCALAR), ""));

        assertThat(label.getString()).isEqualTo("${name}");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void styledLabelHasPrefixHighlighted_whenThereIsAMatchingPrefix() {
        final VariableProposalsLabelProvider labelProvider = new VariableProposalsLabelProvider();
        final StyledString label = labelProvider
                .getStyledText(new VariableContentProposal(createProposalToWrap(VariableType.SCALAR), "${n"));

        final TextStyle expectedStyle = new TextStyle();
        Stylers.Common.MARKED_PREFIX_STYLER.applyStyles(expectedStyle);

        assertThat(label.getString()).isEqualTo("${name}");
        assertThat(label.getStyleRanges()).hasSize(1);
        assertThat(label.getStyleRanges()[0].background.getRGB()).isEqualTo(expectedStyle.background.getRGB());
        assertThat(label.getStyleRanges()[0].foreground.getRGB()).isEqualTo(expectedStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[0].borderColor.getRGB()).isEqualTo(expectedStyle.borderColor.getRGB());
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(expectedStyle.borderStyle);
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(3);
    }

    private RedVariableProposal createProposalToWrap(final VariableType type) {
        return new RedVariableProposal(type.getIdentificator() + "{name}", "<source>", "&{value}", "comment",
                VariableOrigin.BUILTIN);
    }
}
