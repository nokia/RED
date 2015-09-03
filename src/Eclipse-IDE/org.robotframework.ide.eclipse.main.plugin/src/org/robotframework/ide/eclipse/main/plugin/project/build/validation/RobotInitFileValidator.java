package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.listener.IRobotFile;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.setting.DefaultTags;
import org.robotframework.ide.core.testData.model.table.setting.TestTemplate;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.collect.Range;

public class RobotInitFileValidator extends RobotFileValidator {

    public RobotInitFileValidator(final IFile file) {
        super(file);
    }

    @Override
    protected void validate(final IRobotFile fileModel, final IProgressMonitor monitor) throws CoreException {
        validateIfThereAreNoForbiddenSettings(fileModel.getSettingTable());

        super.validate(fileModel, monitor);
    }

    private void validateIfThereAreNoForbiddenSettings(final SettingTable settingTable) {
        if (!settingTable.isPresent()) {
            return;
        }
        for (final TestTemplate template : settingTable.getTestTemplates()) {
            reportProblem(template.getDeclaration().getText().toString(), template);
        }
        for (final DefaultTags defaultTag : settingTable.getDefaultTags()) {
            reportProblem(defaultTag.getDeclaration().getText().toString(), defaultTag);
        }
    }

    private void reportProblem(final String declarationName, final AModelElement element) {
        final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.UNSUPPORTED_SETTING)
                .formatMessageWith(declarationName, "initialization");
        final ProblemPosition position = new ProblemPosition(element.getBeginPosition().getLine(),
                Range.closed(element.getBeginPosition().getOffset(), element.getEndPosition().getOffset()));
        reporter.handleProblem(problem, file, position);
    }
}