/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;

/**
 * @author Michal Anglart
 *
 */
public class CasesDefinitionLocator {

    private final IFile file;

    private final RobotModel model;

    public CasesDefinitionLocator(final IFile file) {
        this(file, RedPlugin.getModelManager().getModel());
    }

    public CasesDefinitionLocator(final IFile file, final RobotModel model) {
        this.file = file;
        this.model = model;
    }

    public void locateCaseDefinition(final CaseDetector detector) {
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        
        final List<RobotFileInternalElement> cases = Stream
                .of(suiteFile.findSection(RobotCasesSection.class), suiteFile.findSection(RobotTasksSection.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RobotSuiteFileSection::getChildren)
                .flatMap(List::stream)
                .collect(toList());
        
        for (final RobotFileInternalElement theCase : cases) {
            final ContinueDecision shouldContinue = detector.caseDetected(suiteFile,
                    (RobotCodeHoldingElement<?>) theCase);
            if (shouldContinue == ContinueDecision.STOP) {
                break;
            }
        }
    }

    @FunctionalInterface
    public interface CaseDetector {

        ContinueDecision caseDetected(RobotSuiteFile file, RobotCodeHoldingElement<?> taskOrTest);

    }
}
