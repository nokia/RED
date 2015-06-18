package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.assist.IRedContentProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

class KeywordProposal implements Comparable<KeywordProposal>, IRedContentProposal {

    private static final Image IMAGE = RobotImages.getKeywordImage().createImage();

    protected final String libName;

    private final KeywordSpecification keywordSpec;

    KeywordProposal(final LibrarySpecification spec, final KeywordSpecification keyword) {
        this.libName = spec.getName();
        this.keywordSpec = keyword;
    }

    @Override
    public String getContent() {
        return keywordSpec.getName();
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    @Override
    public String getLabel() {
        return keywordSpec.getName();
    }

    @Override
    public String getDescription() {
        final String nameLabel = Strings.padEnd("Name:", 15, ' ');
        final String sourceLabel = Strings.padEnd("Source:", 15, ' ');
        final String argsLabel = Strings.padEnd("Arguments:", 15, ' ');
        final List<String> arguments = keywordSpec.getArguments() == null ? new ArrayList<String>() : keywordSpec
                .getArguments();
        final String args = "[" + Joiner.on(" | ").join(arguments) + "]";

        final StringBuilder builder = new StringBuilder();
        builder.append("<form>");
        builder.append("<p><span font=\"monospace\">" + nameLabel + keywordSpec.getName() + "</span></p>");
        builder.append("<p><span font=\"monospace\">" + sourceLabel + libName + "</span></p>");
        builder.append("<p><span font=\"monospace\">" + argsLabel + args + "</span></p>");
        builder.append("<p></p>");
        builder.append(keywordSpec.getDocumentationAsHtml());
        builder.append("</form>");

        return builder.toString();
    }

    public Image getImage() {
        return IMAGE;
    }

    protected final String getName() {
        return keywordSpec.getName();
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public String getLabelDecoration() {
        return "- " + libName;
    }

    @Override
    public int compareTo(final KeywordProposal other) {
        if (other instanceof ReservedKeywordProposal) {
            return 1;
        }
        if (libName.equals(other.libName)) {
            return getName().compareTo(other.getName());
        }
        return libName.compareTo(other.libName);
    }
}
