/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SingleParagraphInput;

import com.google.common.annotations.VisibleForTesting;

final class RedVariableProposal extends BaseAssistProposal {

    private final String source;

    private final String value;

    private final String comment;

    private final VariableOrigin origin;

    RedVariableProposal(final String name, final String source, final String value, final String comment,
            final VariableOrigin origin, final ProposalMatch match) {
        super(name, match);
        this.source = source;
        this.value = value;
        this.comment = comment;
        this.origin = origin;
    }

    VariableOrigin getOrigin() {
        return origin;
    }

    @Override
    public boolean isDocumented() {
        return true;
    }

    @Override
    public String getDescription() {
        final StringBuilder description = new StringBuilder();
        description.append("Source: " + source);
        if (!value.isEmpty()) {
            description.append("\nValue: " + value);
        }
        if (!comment.isEmpty()) {
            description.append("\nComment: " + comment);
        }
        return description.toString();
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new SingleParagraphInput(this::getHtmlDesc);
    }

    private String getHtmlDesc() {
        final StringBuilder description = new StringBuilder();
        description.append("*Source:* " + source);
        if (!value.isEmpty()) {
            description.append("\n\n*Value:* " + value);
        }
        if (!comment.isEmpty()) {
            description.append("\n\n*Comment:* " + comment);
        }
        return description.toString();
    }

    @Override
    public ImageDescriptor getImage() {
        if (content.startsWith("&")) {
            return RedImages.getRobotDictionaryVariableImage();
        } else if (content.startsWith("@")) {
            return RedImages.getRobotListVariableImage();
        } else {
            return RedImages.getRobotScalarVariableImage();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == RedVariableProposal.class) {
            final RedVariableProposal that = (RedVariableProposal) obj;
            return Objects.equals(this.content, that.content) && Objects.equals(this.source, that.source)
                    && this.origin == that.origin;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, source, origin);
    }

    @VisibleForTesting
    enum VariableOrigin {
        LOCAL,
        IMPORTED,
        BUILTIN
    }
}
