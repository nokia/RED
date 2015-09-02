package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;
import org.robotframework.ide.core.testData.text.read.recognizer.header.TestCasesTableHeaderRecognizer;


public class RobotSuiteFileDescriber implements ITextContentDescriber {

    public static final String SUITE_FILE_CONTENT_ID = "org.robotframework.red.robotsuitefile";

    public static final String RESOURCE_FILE_CONTENT_ID = "org.robotframework.red.robotfile";

    public static final String INIT_FILE_CONTENT_ID = "org.robotframework.red.robotsuiteinitfile";

    public static boolean isSuiteFile(final IFile resource) {
        return hasContentType(resource, SUITE_FILE_CONTENT_ID);
    }

    public static boolean isResourceFile(final IFile resource) {
        return hasContentType(resource, RESOURCE_FILE_CONTENT_ID);
    }

    public static boolean isInitializationFile(final IFile resource) {
        return hasContentType(resource, INIT_FILE_CONTENT_ID);
    }

    private static boolean hasContentType(final IFile resource, final String id) {
        try {
            final IContentDescription contentDescription = resource.getContentDescription();
            if (contentDescription != null) {
                return id.equals(contentDescription.getContentType().getId());
            }
            return false;
        } catch (final CoreException e) {
            return false;
        }
    }

    @Override
    public int describe(final InputStream contents, final IContentDescription description) throws IOException {
        return describe(new InputStreamReader(contents), description);
    }

    @Override
    public QualifiedName[] getSupportedOptions() {
        return new QualifiedName[0];
    }

    @Override
    public int describe(final Reader contents, final IContentDescription description) throws IOException {
        final BufferedReader br = new BufferedReader(contents);
        String line;
        while ((line = br.readLine()) != null) {
            if (TestCasesTableHeaderRecognizer.EXPECTED.matcher(line).find()) {
               return VALID;
           }
        }
        return INDETERMINATE;
    }

}
