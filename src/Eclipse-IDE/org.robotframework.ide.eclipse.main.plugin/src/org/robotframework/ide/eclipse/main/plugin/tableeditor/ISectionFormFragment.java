package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

public interface ISectionFormFragment {

    public void initialize(final Composite parent);

    public void setFocus();

    public MatchesCollection collectMatches(String filter);

    public interface MatcherProvider {

        MatchesCollection getMatches();
    }

    public static class MatchesCollection {

        private final Multimap<String, Range<Integer>> matches = ArrayListMultimap.create();
        protected int allMatches = 0;
        protected int rowsMatching = 0;

        @SuppressWarnings("unused")
        public void collect(final RobotElement element, final String filter) {
            // nothing to collect here, override this method
        }
        
        public Collection<Range<Integer>> getRanges(final String label) {
            return matches.get(label);
        }

        public boolean contains(final String label) {
            return matches.containsKey(label);
        }

        public void addAll(final MatchesCollection from) {
            if (from != null) {
                matches.putAll(from.matches);
            }
            allMatches += from.allMatches;
            rowsMatching += from.rowsMatching;
        }

        protected final void put(final String key, final Range<Integer> range) {
            matches.put(key, range);
        }

        public int getNumberOfAllMatches() {
            return allMatches;
        }

        public int getNumberOfMatchingElement() {
            return rowsMatching;
        }
    }
}
