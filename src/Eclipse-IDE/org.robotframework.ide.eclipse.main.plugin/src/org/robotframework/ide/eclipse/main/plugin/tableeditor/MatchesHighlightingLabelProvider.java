package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Collection;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesCollection;

import com.google.common.collect.Range;

public abstract class MatchesHighlightingLabelProvider extends StylersDisposingLabelProvider {

    private final MatchesProvider matchesProvider;
    private final DisposeNeededStyler styler;

    public MatchesHighlightingLabelProvider(final MatchesProvider matchesProvider) {
        this.matchesProvider = matchesProvider;
        this.styler = addDisposeNeededStyler(new DisposeNeededStyler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.background = new Color(Display.getCurrent(), 255, 255, 175);
                textStyle.borderStyle = SWT.BORDER_DOT;
                markForDisposal(textStyle.background);
            }
        });
    }

    protected final StyledString highlightMatches(final StyledString label) {
        if (label == null) {
            return new StyledString();
        }
        return highlightMatches(label, 0, label.getString());
    }

    protected final StyledString highlightMatches(final StyledString label, final int shift, final String modelContent) {
        if (label == null) {
            return new StyledString();
        }
        final MatchesCollection matches = matchesProvider.getMatches();
        if (matches == null) {
            return label;
        }
        final Collection<Range<Integer>> ranges = matches.getRanges(modelContent);
        if (ranges == null) {
            return label;
        }
        for (final Range<Integer> range : ranges) {
            label.setStyle(range.lowerEndpoint() + shift, range.upperEndpoint() - range.lowerEndpoint(), styler);
        }
        return label;
    }
}
