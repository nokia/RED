package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.assist.IDecoratedContentProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Strings;

class KeywordContentProposal implements IDecoratedContentProposal {

    private final RedKeywordProposal wrappedProposal;

    KeywordContentProposal(final RedKeywordProposal proposedKeyword) {
        this.wrappedProposal = proposedKeyword;
    }

    @Override
    public String getContent() {
        return wrappedProposal.getContent();
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    Image getImage() {
        final ImageDescriptor descriptor = wrappedProposal.getImage();
        return descriptor != null ? descriptor.createImage() : null;
    }

    @Override
    public String getLabel() {
        return wrappedProposal.getLabel();
    }

    @Override
    public String getLabelDecoration() {
        return wrappedProposal.getLabelDecoration();
    }

    @Override
    public boolean hasDescription() {
        return wrappedProposal.hasDescription();
    }

    @Override
    public String getDescription() {
        final String nameLabel = Strings.padEnd("Name:", 15, ' ');
        final String sourceLabel = Strings.padEnd("Source:", 15, ' ');
        final String argsLabel = Strings.padEnd("Arguments:", 15, ' ');

        final StringBuilder builder = new StringBuilder();
        builder.append("<form>");
        builder.append("<p><span font=\"monospace\">" + nameLabel + wrappedProposal.getLabel() + "</span></p>");
        builder.append("<p><span font=\"monospace\">" + sourceLabel + wrappedProposal.getSourceName() + "</span></p>");
        builder.append("<p><span font=\"monospace\">" + argsLabel + wrappedProposal.getArgumentsLabel() + "</span></p>");
        builder.append("<p></p>");
        builder.append(wrappedProposal.getHtmlDocumentation());
        builder.append("</form>");

        return builder.toString();
    }
}
