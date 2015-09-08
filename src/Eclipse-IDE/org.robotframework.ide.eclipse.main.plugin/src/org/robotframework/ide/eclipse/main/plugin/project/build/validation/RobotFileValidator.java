package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.core.testData.model.IRobotFile;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;

public abstract class RobotFileValidator implements ModelUnitValidator {

    protected final IFile file;

    protected final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    public RobotFileValidator(final IFile file) {
        this.file = file;
    }

    @Override
    public final void validate(final IProgressMonitor monitor) throws CoreException {
        final RobotProject project = RedPlugin.getModelManager().getModel().createRobotProject(file.getProject());
        final RobotParser parser = new RobotParser(new RobotProjectHolder(project.getRuntimeEnvironment()));

        final List<IRobotFileOutput> parserOut = parser.parse(file.getLocation().toFile());
        if (!parserOut.isEmpty()) {
            validate(parserOut.get(0), monitor);
        }
    }

    private void validate(final IRobotFileOutput fileOutput, final IProgressMonitor monitor) throws CoreException {
        // TODO : check output status and parsing messages
        validate(fileOutput.getFileModel(), monitor);
    }

    /**
     * This method does common validation for different file types (resources, inits, suites).
     * It should be overridden and called by subclasses
     * 
     * @param fileModel
     * @param monitor
     * @throws CoreException
     */
    protected void validate(final IRobotFile fileModel, final IProgressMonitor monitor) throws CoreException {
        new TestCasesTableValidator(file, fileModel.getTestCaseTable()).validate(monitor);
        new GeneralSettingsTableValidator(file, fileModel.getSettingTable()).validate(monitor);
        new KeywordTableValidator(file, fileModel.getKeywordTable()).validate(monitor);
        new VariablesTableValidator(file, fileModel.getVariableTable()).validate(monitor);
    }
}