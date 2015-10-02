package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    
    public static Comparator<RedVariableProposal> variablesSortedByTypesAndNames() {
        return new Comparator<RedVariableProposal>() {
            @Override
            public int compare(final RedVariableProposal proposal1, final RedVariableProposal proposal2) {
                if (proposal1.getType() == proposal2.getType()) {
                    return proposal1.getName().compareToIgnoreCase(proposal2.getName());
                } else {
                    return proposal1.getType().compareTo(proposal2.getType());
                }
            }
        };
    }
    
    public List<RedVariableProposal> getVariableProposals(final Comparator<RedVariableProposal> comparator) {
        final List<RedVariableProposal> proposals = newArrayList();

        new VariableDefinitionLocator(suiteFile).locateVariableDefinition(new VariableDetector() {
            @Override
            public ContinueDecision variableDetected(final RobotSuiteFile file, final RobotVariable variable) {
                proposals.add(RedVariableProposal.create(variable));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                proposals.add(RedVariableProposal.createBuiltIn(name, value.toString()));
                return ContinueDecision.CONTINUE;
            }
        });

        for (final ReferencedVariableFile referencedVariableFile : suiteFile.getVariablesFromReferencedFiles()) {
            final Map<String, Object> refVariableMap = referencedVariableFile.getVariables();
            if (refVariableMap != null && !refVariableMap.isEmpty()) {
                for (final String variableName : refVariableMap.keySet()) {
                    proposals.add(RedVariableProposal.create(variableName, String.valueOf(refVariableMap.get(variableName)), referencedVariableFile.getPath()));
                }
            }
        }
        
        Collections.sort(proposals, comparator);
        return proposals;
    }
}
