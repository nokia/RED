package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.core.testData.model.listener.IRobotFile;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;

public class RobotSuiteFileValidator {

    private final IFile file;

    public RobotSuiteFileValidator(final IFile file) {
        this.file = file;
    }

    public void validate(final IProgressMonitor monitor) throws CoreException, IOException {
        RobotProject project = RedPlugin.getModelManager().getModel().createRobotProject(file.getProject());
        final RobotRuntimeEnvironment runtimeEnvironment = project.getRuntimeEnvironment();
        //TODO: Handle null runtime Environment
        final RobotParser parser = new RobotParser(new RobotProjectHolder(runtimeEnvironment));
        final List<RobotFileOutput> parserOut = parser.parse(file.getLocation().toFile());
        if (!parserOut.isEmpty()) {
            validate(parserOut.get(0), monitor);
        }
    }

    private void validate(final RobotFileOutput fileOutput, final IProgressMonitor monitor) throws CoreException {
        // TODO : check output status and paring messages

        validate(fileOutput.getFileModel(), monitor);
    }

    private void validate(final IRobotFile fileModel, final IProgressMonitor monitor) throws CoreException {
        validateKeywordTable(fileModel.getKeywordTable(), monitor);
    }

    private void validateKeywordTable(final KeywordTable keywordTable, final IProgressMonitor monitor)
            throws CoreException {
        
        for (final UserKeyword kw1 : keywordTable.getKeywords()) {
            for (final UserKeyword kw2 : keywordTable.getKeywords()) {
                if (kw1 != kw2) {
                    final RobotToken kw1Token = kw1.getKeywordName();
                    final RobotToken kw2Token = kw2.getKeywordName();
                    final String kw1Name = kw1Token.getText().toString();
                    final String kw2Name = kw2Token.getText().toString();

                    if (kw1Name.equals(kw2Name)) {
                        final IMarker marker = file.createMarker(RobotProblem.TYPE_ID);

                        marker.setAttribute(IMarker.MESSAGE, "Duplicated keyword definition '" + kw1Name + "'");
                        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                        marker.setAttribute(IMarker.LOCATION, "line " + kw1Token.getLineNumber());
                        marker.setAttribute(IMarker.LINE_NUMBER, kw1Token.getLineNumber());
                    }
                }
            }
        }
    }
}