package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.robotframework.ide.core.executor.IRobotLogOutputStreamListener;

public class ExecutorOutputStreamListener implements IRobotLogOutputStreamListener {

    private MessageConsoleStream out;
    private MessageConsole console;

    public ExecutorOutputStreamListener(String consoleName) {
        console = findConsole(consoleName);
        ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
        out = console.newMessageStream();
    }

    @Override
    public void handleLine(String line) {
        out.println(line);
    }

    private MessageConsole findConsole(String consoleName) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager consoleManager = plugin.getConsoleManager();
        IConsole[] existingConsoles = consoleManager.getConsoles();
        for (int i = 0; i < existingConsoles.length; i++) {
            if (consoleName.equals(existingConsoles[i].getName())) {
                return (MessageConsole) existingConsoles[i];
            }
        }

        MessageConsole newConsole = new MessageConsole(consoleName, null);
        consoleManager.addConsoles(new IConsole[] { newConsole });
        return newConsole;
    }

}
