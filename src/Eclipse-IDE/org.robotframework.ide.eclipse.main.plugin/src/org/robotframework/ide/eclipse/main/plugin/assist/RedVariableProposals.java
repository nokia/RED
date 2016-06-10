/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;


public class RedVariableProposals {
    
    private final RobotSuiteFile suiteFile;

    public RedVariableProposals(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    public List<RedVariableProposal> getVariableProposals(final Comparator<RedVariableProposal> comparator,
            final int offset) {
        final Set<RedVariableProposal> proposals = newLinkedHashSet();

        final VariableDefinitionLocator locator = new VariableDefinitionLocator(suiteFile.getFile());
        final VariableDetector detector = createDetector(proposals);
        if (offset == -1) {
            locator.locateVariableDefinition(detector);
        } else {
            locator.locateVariableDefinitionWithLocalScope(detector, offset);
        }

        final List<RedVariableProposal> resultProposals = newArrayList(proposals);
        Collections.sort(resultProposals, comparator);
        return resultProposals;
    }
    
    public List<RedVariableProposal> getVariableProposals(final Comparator<RedVariableProposal> comparator) {
        return getVariableProposals(comparator, -1);
    }

    private VariableDetector createDetector(final Set<RedVariableProposal> proposals) {
        return new VariableDetector() {
            @Override
            public ContinueDecision variableDetected(final RobotSuiteFile file, final RobotVariable variable) {
                proposals.add(RedVariableProposal.create(variable));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision localVariableDetected(final RobotSuiteFile file, final RobotToken variable) {
                final String varName = variable.getText().trim();
                proposals.add(RedVariableProposal.createLocal(varName, file.getName()));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                proposals.add(RedVariableProposal.createBuiltIn(name, value.toString()));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file,
                    final String variableName, final Object value) {
                proposals.add(RedVariableProposal.create(variableName, value.toString(), file.getPath()));
                return ContinueDecision.CONTINUE;
            }
        };
    }
}
