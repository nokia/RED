package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author mmarzec
 *
 */
public class KeywordFinder {

    public boolean isKeywordInBreakpointLine(IBreakpoint breakpoint, int breakpointLine, String keyword,
            List<String> args, int currentExecutionLine) {
        FileEditorInput fileEditorInput = new FileEditorInput((IFile) ((ILineBreakpoint) breakpoint).getMarker()
                .getResource());

        IFile editedFile = fileEditorInput.getFile();

        String[] s = keyword.split("\\.");
        keyword = s[s.length - 1];

        boolean result = false;
        int lineNum = 1;
        Scanner scanner = null;
        try (InputStream is = editedFile.getContents()) {
            scanner = new Scanner(is).useDelimiter("\\n");
            while (scanner.hasNext()) {
                String line = scanner.next();
                if (line.contains(keyword)) {
                    int count = 0;
                    for (String arg : args) {
                        if (line.contains(arg)) {
                            count++;
                        }
                    }
                    if (count == args.size() && breakpointLine == lineNum && lineNum == currentExecutionLine) {
                        result = true;
                        break;
                    }
                }
                lineNum++;
            }
        } catch (CoreException | IOException e) {
            e.printStackTrace();
        }

        if (scanner != null) {
            scanner.close();
        }

        return result;
    }

    public int getKeywordLine(IFile editedFile, String keyword, List<String> args,
            List<Integer> executedLines) {

        String[] s = keyword.split("\\.");
        keyword = s[s.length - 1];

        int result = -1;
        int lineNum = 1;
        Scanner scanner = null;
        try (InputStream is = editedFile.getContents()) {
            scanner = new Scanner(is).useDelimiter("\\n");
            while (scanner.hasNext()) {
                String line = scanner.next();
                if (line.contains(keyword)) {
                    int count = 0;
                    for (String arg : args) {
                        if (line.contains(arg)) {
                            count++;
                        }
                    }
                    if (count == args.size() && !executedLines.contains(lineNum)) {
                        result = lineNum;
                        break;
                    }
                }
                lineNum++;
            }
        } catch (CoreException | IOException e) {
            e.printStackTrace();
        }

        if (scanner != null) {
            scanner.close();
        }

        return result;
    }
}
