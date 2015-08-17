package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.testData.importer.ResourceImportReference;


public class RobotProjectHolder {

    private RobotRuntimeEnvironment robotRuntime;
    private final List<RobotFileOutput> readableProjectFiles = new LinkedList<>();


    public RobotProjectHolder(final RobotRuntimeEnvironment robotRuntime) {
        this.robotRuntime = robotRuntime;
    }


    public RobotRuntimeEnvironment getRobotRuntime() {
        return robotRuntime;
    }


    public void addModelFile(final RobotFileOutput model) {
        readableProjectFiles.add(model);
    }


    public void addImportedResources(
            final List<ResourceImportReference> referenced) {
        for (ResourceImportReference ref : referenced) {
            readableProjectFiles.add(ref.getReference());
        }
    }


    public boolean shouldBeLoaded(final File file) {
        return !containsFile(file);
    }


    public boolean containsFile(final File file) {
        return (findFileByName(file) != null);
    }


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
