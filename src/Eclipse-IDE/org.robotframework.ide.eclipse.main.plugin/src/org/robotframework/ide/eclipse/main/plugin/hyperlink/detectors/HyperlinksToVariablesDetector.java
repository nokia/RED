/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;

abstract class HyperlinksToVariablesDetector {

    protected final VariableDetector createDetector(final RobotSuiteFile suiteFile, final IRegion fromRegion,
            final String fullVariableName, final List<IHyperlink> hyperlinks) {
        final String hoveredVariableName = VariableNamesSupport
                .extractUnifiedVariableNameWithoutBrackets(fullVariableName);
        return new VariableDetector() {

            @Override
            public ContinueDecision variableDetected(final RobotVariable variable) {
                if (VariableNamesSupport.extractUnifiedVariableName(variable.getName()).equals(hoveredVariableName)) {
                    final DefinitionPosition position = variable.getDefinitionPosition();
                    final IRegion destination = new Region(position.getOffset(), position.getLength());

                    final RobotSuiteFile file = variable.getSuiteFile();
                    final IHyperlink definitionHyperlink = file == suiteFile
                            ? createLocalVariableHyperlink(variable, fullVariableName, fromRegion, file, destination)
                            : createResourceVariableHyperlink(variable, fullVariableName, fromRegion, file,
                                    destination);
                    hyperlinks.add(definitionHyperlink);
                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            }

            @Override
            public ContinueDecision localVariableDetected(final RobotFileInternalElement element,
                    final RobotToken variableToken) {
                if (VariableNamesSupport.extractUnifiedVariableNameWithoutBrackets(variableToken.getText().toString())
                        .equals(hoveredVariableName)) {
                    final IRegion destination = new Region(variableToken.getStartOffset(),
                            variableToken.getText().length());
                    hyperlinks.add(createLocalVariableHyperlink(element, fullVariableName, fromRegion,
                            element.getSuiteFile(), destination));
                    return ContinueDecision.STOP;
                } else {
                    return ContinueDecision.CONTINUE;
                }
            }

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                // we don't want to do anything if variable is global
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file,
                    final String variableName, final Object value) {
                // we don't want to do anything if variable is defined in Variable file
                return ContinueDecision.CONTINUE;
            }
        };
    }

    protected abstract IHyperlink createLocalVariableHyperlink(final RobotFileInternalElement element, String varName,
            final IRegion fromRegion, final RobotSuiteFile suiteFile, final IRegion destination);

    protected abstract IHyperlink createResourceVariableHyperlink(final RobotFileInternalElement element,
            String varName, final IRegion fromRegion, final RobotSuiteFile suiteFile, final IRegion destination);
}
