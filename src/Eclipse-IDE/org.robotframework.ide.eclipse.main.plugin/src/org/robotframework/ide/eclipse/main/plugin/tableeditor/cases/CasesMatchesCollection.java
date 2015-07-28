package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesCollection;

import com.google.common.collect.Range;

public class CasesMatchesCollection extends MatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotCasesSection) {
            collectMatches((RobotCasesSection) element, filter);
        }
    }

    private void collectMatches(final RobotCasesSection section, final String filter) {
        for (final RobotCase testCase : section.getChildren()) {
            collectMatches(testCase, filter);
        }
    }

    private void collectMatches(final RobotCase testCase, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, testCase.getName());
        isMatching |= collectMatches(filter, testCase.getComment());
        if (isMatching) {
            rowsMatching++;
        }

        for (final RobotKeywordCall call : testCase.getChildren()) {
            collectMatches(call, filter);
        }
    }

    private void collectMatches(final RobotKeywordCall call, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, call.getName());
        for (final String arg : call.getArguments()) {
            isMatching |= collectMatches(filter, arg);
        }
        isMatching |= collectMatches(filter, call.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }

    private boolean collectMatches(final String filter, final String label) {
        int index = label.indexOf(filter);
        final boolean result = index >= 0;
        while (index >= 0) {
            put(label, Range.closed(index, index + filter.length()));
            allMatches++;
            index = label.indexOf(filter, index + 1);
        }
        return result;
    }
}
