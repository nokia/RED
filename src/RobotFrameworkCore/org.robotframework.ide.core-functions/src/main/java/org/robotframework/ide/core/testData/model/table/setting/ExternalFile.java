package org.robotframework.ide.core.testData.model.table.setting;

import java.io.File;


/**
 * Represents external file.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see LibraryReference
 * @see ResourceFileReference
 * @see VariablesFileReference
 */
public class ExternalFile {

    private String pathOrName;
    private File resolvedFile;
    private long fileLastModified = 0;


    /**
     * @param pathOrName
     */
    public ExternalFile(String pathOrName) {
        this.pathOrName = pathOrName;
    }


    /**
     * @return library name, path to library, resource or file with variables
     */
    public String getPathOrName() {
        return pathOrName;
    }


    /**
     * @param pathOrName
     *            library name, path to library, resource or file with variables
     */
    public void setPathOrName(String pathOrName) {
        this.pathOrName = pathOrName;
    }


    /**
     * @param fileLastModified
     *            timestamp of reference file last modification as EPOCH
     */
    public void setFileLastModified(long fileLastModified) {
        this.fileLastModified = fileLastModified;
    }


    /**
     * @return timestamp of reference file last modification as EPOCH
     */
    public long getFileLastModified() {
        return fileLastModified;
    }


    /**
     * @param resolvedFile
     *            path to reference file
     */
    public void setResolvedReferenceFile(File resolvedFile) {
        this.resolvedFile = resolvedFile;
    }


    /**
     * @return resolved reference file
     */
    public File getResolvedReferenceFile() {
        return this.resolvedFile;
    }
}
