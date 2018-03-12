/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;


class SuiteFileMarkersContainerMock implements SuiteFileMarkersContainer {

    private final Map<RobotFileInternalElement, Severity> severityMarkers = new HashMap<>();

    private final Set<RobotFileInternalElement> taskMarkers = new HashSet<>();

    @Override
    public Optional<Severity> getHighestSeverityMarkerFor(final Optional<? extends RobotFileInternalElement> element) {
        if (element.isPresent() && severityMarkers.containsKey(element.get())) {
            return Optional.of(severityMarkers.get(element.get()));
        }
        return Optional.empty();
    }

    @Override
    public boolean hasTaskMarkerFor(final Optional<RobotFileInternalElement> element) {
        return element.isPresent() && taskMarkers.contains(element.get());
    }

    @Override
    public List<String> getMarkersMessagesFor(final Optional<? extends RobotFileInternalElement> element) {
        return null;
    }

    public void registerMarkerSeverity(final RobotFileInternalElement element, final Severity highestSeverity) {
        severityMarkers.put(element, highestSeverity);
    }

    public void registerMarkerTask(final RobotFileInternalElement element) {
        taskMarkers.add(element);
    }
}
