package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
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

    public List<RedVariableProposal> getVariableProposals(final Comparator<RedVariableProposal> comparator,
            final int offset) {
        final Set<RedVariableProposal> proposals = newLinkedHashSet();

        final VariableDefinitionLocator locator = new VariableDefinitionLocator(suiteFile);
        final VariableDetector detector = createDetector(proposals);
        if (offset == -1) {
            locator.locateVariableDefinition(detector);
        } else {
            locator.locateVariableDefinitionWithLocalScope(detector, offset);
        }

        for (final ReferencedVariableFile referencedVariableFile : suiteFile.getVariablesFromReferencedFiles()) {
            final Map<String, Object> refVariableMap = referencedVariableFile.getVariables();
            if (refVariableMap != null && !refVariableMap.isEmpty()) {
                for (final String variableName : refVariableMap.keySet()) {
                    proposals.add(RedVariableProposal.create(variableName,
                            String.valueOf(refVariableMap.get(variableName)), referencedVariableFile.getPath()));
                }
            }
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
                String varName = variable.getText().toString().trim();
                if (varName.endsWith("=")) {
                    varName = varName.substring(0, varName.length() - 1).trim();
                }
                proposals.add(RedVariableProposal.createLocal(varName, file.getName()));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                proposals.add(RedVariableProposal.createBuiltIn(name, value.toString()));
                return ContinueDecision.CONTINUE;
            }
        };
    }
}
