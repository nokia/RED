/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.eclipse.jface.text.Position;

public interface MatchingEngine {

    public void searchForMatches(String toMatch, MatchAccess matchAccess);

    public interface MatchAccess {

        public void onMatch(String matchingContent, final Position matchPosition);
    }
}