package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;

import com.google.common.collect.Range;

class VariablesTableValidator implements ModelUnitValidator {

    private final IFile file;

    private final VariableTable variableTable;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    VariablesTableValidator(final IFile file, final VariableTable variableTable) {
        this.file = file;
        this.variableTable = variableTable;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!variableTable.isPresent()) {
            return;
        }
        reportDuplicatedVariables(variableTable);
    }

    private void reportDuplicatedVariables(final VariableTable variableTable) {
        final Set<String> duplicatedNames = newHashSet();

        for (final IVariableHolder var1 : variableTable.getVariables()) {
            for (final IVariableHolder var2 : variableTable.getVariables()) {
                if (var1 != var2 && var1.getName().equals(var2.getName())) {
                    duplicatedNames.add(var1.getName());
                }
            }
        }

        for (final IVariableHolder variable : variableTable.getVariables()) {
            if (duplicatedNames.contains(variable.getName())) {
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.DUPLICATED_VARIABLE)
                        .formatMessageWith(variable.getName());
                final ProblemPosition position = new ProblemPosition(variable.getDeclaration().getLineNumber(),
                        Range.closed(variable.getDeclaration().getStartOffset(),
                                variable.getDeclaration().getStartOffset()
                                        + variable.getDeclaration().getText().length()));
                reporter.handleProblem(problem, file, position);
            }
        }
    }
}
