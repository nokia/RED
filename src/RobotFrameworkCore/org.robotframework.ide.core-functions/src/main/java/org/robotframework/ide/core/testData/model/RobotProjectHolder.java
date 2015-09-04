package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.listener.IRobotProjectHolder;


public class RobotProjectHolder implements IRobotProjectHolder {

    private final RobotRuntimeEnvironment robotRuntime;
    private final List<RobotFileOutput> readableProjectFiles = new LinkedList<>();


    public static RobotProjectHolder create(
            final RobotRuntimeEnvironment robotRuntime) {
        RobotProjectHolder rph = new RobotProjectHolder(robotRuntime);

        return rph;
    }


    public RobotProjectHolder(final RobotRuntimeEnvironment robotRuntime) {
        this.robotRuntime = robotRuntime;
    }


    @Override
    public RobotRuntimeEnvironment getRobotRuntime() {
        return robotRuntime;
    }


    @Override
    public void addModelFile(final RobotFileOutput robotOutput) {
        if (robotOutput != null) {
            File processedFile = robotOutput.getProcessedFile();
            if (processedFile != null) {
                RobotFileOutput file = findFileByName(processedFile);
                removeModelFile(file);
            }

            readableProjectFiles.add(robotOutput);
        }
    }


    @Override
    public void removeModelFile(final RobotFileOutput robotOutput) {
        readableProjectFiles.remove(robotOutput);
    }


    @Override
    public void addImportedResources(
            final List<ResourceImportReference> referenced) {
        for (ResourceImportReference ref : referenced) {
            addImportedResource(ref);
        }
    }


    @Override
    public void addImportedResource(final ResourceImportReference referenced) {
        readableProjectFiles.add(referenced.getReference());
    }


    @Override
    public boolean shouldBeLoaded(final RobotFileOutput robotOutput) {
        return (robotOutput != null && shouldBeLoaded(robotOutput
                .getProcessedFile()));
    }


    @Override
    public boolean shouldBeLoaded(final File file) {
        RobotFileOutput foundFile = findFileByName(file);
        return (foundFile == null)
                || (file.lastModified() != foundFile
                        .getLastModificationEpochTime());
    }


    @Override
    public RobotFileOutput findFileByName(final File file) {
        RobotFileOutput found = null;
        List<Integer> findFile = findFile(new SearchByName(file));
        if (!findFile.isEmpty()) {
            found = readableProjectFiles.get(findFile.get(0));
        }

        return found;
    }


    protected List<Integer> findFile(final ISearchCriteria criteria) {
        List<Integer> foundFiles = new LinkedList<>();
        int size = readableProjectFiles.size();
        for (int i = 0; i < size; i++) {
            RobotFileOutput robotFile = readableProjectFiles.get(i);
            if (criteria.matchCriteria(robotFile)) {
                foundFiles.add(i);
                break;
            }
        }

        return foundFiles;
    }

    private class SearchByName implements ISearchCriteria {

        private final File toFound;


        public SearchByName(final File toFound) {
            this.toFound = toFound;
        }


        @Override
        public boolean matchCriteria(RobotFileOutput robotFile) {
            return (robotFile.getProcessedFile().getAbsolutePath()
                    .equals(toFound.getAbsolutePath()));
        }
    }

    protected interface ISearchCriteria {

        boolean matchCriteria(final RobotFileOutput robotFile);
    }
}
