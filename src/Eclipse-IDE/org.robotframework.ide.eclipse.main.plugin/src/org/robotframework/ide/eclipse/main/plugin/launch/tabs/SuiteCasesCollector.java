/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;

class SuiteCasesCollector {

    static List<String> collectCaseNames(final RobotSuiteFile suiteModel) {
        return collectCases(suiteModel).map(RobotCodeHoldingElement::getName).collect(Collectors.toList());
    }

    static Stream<RobotCodeHoldingElement<?>> collectCases(final RobotSuiteFile suiteModel) {
        return Stream
                .of(suiteModel.findSection(RobotCasesSection.class), suiteModel.findSection(RobotTasksSection.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(section -> section.getChildren().stream())
                .map(RobotCodeHoldingElement.class::cast);
    }

}
