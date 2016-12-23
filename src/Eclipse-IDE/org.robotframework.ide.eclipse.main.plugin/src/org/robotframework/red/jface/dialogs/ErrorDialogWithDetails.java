package org.robotframework.red.jface.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

public class ErrorDialogWithDetails {

    public static void openErrorDialogWithDetails(final Shell shell, final String title, final String fullMessage,
            final String detailsDelimiter, final String toRemoveRegex, final String pluginId, final Throwable e) {
        final Scanner sc = new Scanner(fullMessage);
        StringBuilder sb = new StringBuilder("");
        boolean isDelimiterFound = false;
        String reason = "Check details";
        while (sc.hasNextLine() && !isDelimiterFound) {
            String line = sc.nextLine();
            if (line.matches(toRemoveRegex + ".*")) {
                reason = line.replaceFirst(toRemoveRegex, "");
            } else {
                isDelimiterFound = line.contains(detailsDelimiter);
                if (!isDelimiterFound) {
                    sb.append(line + "\n");
                }
            }
        }
        final String mainMessage = sb.toString();
        List<Status> childStatuses = new ArrayList<>();
        while (sc.hasNextLine()) {
            Status status = new Status(IStatus.ERROR, pluginId, sc.nextLine());
            childStatuses.add(status);
        }
        sc.close();
        MultiStatus ms = new MultiStatus(pluginId, IStatus.ERROR, childStatuses.toArray(new Status[] {}), reason, e);
        ErrorDialog.openError(shell, title, mainMessage, ms);
    }
}
