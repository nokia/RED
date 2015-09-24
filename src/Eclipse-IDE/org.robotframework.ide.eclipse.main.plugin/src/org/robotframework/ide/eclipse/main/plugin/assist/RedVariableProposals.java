package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.importer.AVariableImported;
import org.robotframework.ide.core.testData.robotImported.ARobotInternalVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;


public class RedVariableProposals {
    
    private final List<RedVariableProposal> builtInVariableProposals;
    
    private final RobotSuiteFile suiteFile;

    public RedVariableProposals(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
        
        builtInVariableProposals = new ArrayList<>();
        for (final ARobotInternalVariable<?> robotInternalVariable : suiteFile.getGlobalVariables()) {
            builtInVariableProposals.add(RedVariableProposal.createBuiltIn(robotInternalVariable.getName(),
                    robotInternalVariable.getValue().toString()));
        }
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

        for (final RobotVariable variable : suiteFile.getUserDefinedVariables()) {
            proposals.add(RedVariableProposal.create(variable));
        }

        final Map<AVariableImported<?>, String> variablesMap = suiteFile.getVariablesFromImportedFiles();
        for (final AVariableImported<?> variable : variablesMap.keySet()) {
            proposals.add(RedVariableProposal.create(variable, variablesMap.get(variable)));
        }

        for (final ReferencedVariableFile referencedVariableFile : suiteFile.getVariablesFromReferencedFiles()) {
            final Map<String, Object> refVariableMap = referencedVariableFile.getVariables();
            if (refVariableMap != null && !refVariableMap.isEmpty()) {
                for (final String variableName : refVariableMap.keySet()) {
                    proposals.add(RedVariableProposal.create(variableName, String.valueOf(refVariableMap.get(variableName)), referencedVariableFile.getPath()));
                }
            }
        }
        
        proposals.addAll(builtInVariableProposals);

        if (comparator != null) {
            Collections.sort(proposals, comparator);
        }
        return proposals;
    }
}
