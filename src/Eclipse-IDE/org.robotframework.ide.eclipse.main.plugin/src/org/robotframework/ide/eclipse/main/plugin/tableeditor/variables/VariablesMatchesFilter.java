package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesCollection;

public class VariablesMatchesFilter extends ViewerFilter {

    private final MatchesCollection matches;

    public VariablesMatchesFilter(final MatchesCollection matches) {
        this.matches = matches;
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof RobotVariable) {
            return variableMatches((RobotVariable) element);
        }
        return true;
    }

    private boolean variableMatches(final RobotVariable variable) {
        return matches.contains(variable.getPrefix() + variable.getName() + variable.getSuffix())
                || matches.contains(variable.getValue())
                || matches.contains(variable.getComment());
    }
}
