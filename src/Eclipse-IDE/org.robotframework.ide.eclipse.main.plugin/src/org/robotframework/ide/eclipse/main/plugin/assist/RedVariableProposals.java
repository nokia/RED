package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.importer.AVariableImported;
import org.robotframework.ide.core.testData.robotImported.ARobotInternalVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;


public class RedVariableProposals {
    
    private List<RedVariableProposal> builtInVariableProposals;
    
    private final RobotSuiteFile suiteFile;

    public RedVariableProposals(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
        
        builtInVariableProposals = new ArrayList<>();
        for (ARobotInternalVariable<?> robotInternalVariable : suiteFile.getGlobalVariables()) {
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

        for (RobotVariable variable : suiteFile.getUserDefinedVariables()) {
            proposals.add(RedVariableProposal.create(variable));
        }

        Map<AVariableImported, String> variablesMap = suiteFile.getVariablesFromImportedFiles();
        for (AVariableImported variable : variablesMap.keySet()) {
            proposals.add(RedVariableProposal.create(variable, variablesMap.get(variable)));
        }

        for (ReferencedVariableFile referencedVariableFile : suiteFile.getVariablesFromReferencedFiles()) {
            final List<String> refVariablesList = referencedVariableFile.getVariables();
            if (variablesMap != null && !variablesMap.isEmpty()) {
                for (String variable : refVariablesList) {
                    proposals.add(RedVariableProposal.create(variable, referencedVariableFile.getPath()));
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
