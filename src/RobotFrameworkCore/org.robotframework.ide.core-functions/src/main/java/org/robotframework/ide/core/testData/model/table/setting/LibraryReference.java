package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.SettingTable;


/**
 * Mapping used for taking test library into use.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see SettingTable
 */
public class LibraryReference extends AbstractImportable {

    private final List<Argument> arguments = new LinkedList<Argument>();
    private final Alias alias = new Alias();


    /**
     * @param pathOrName
     *            path to library file or library name
     * @see AbstractImportable
     */
    public LibraryReference(String pathOrName) {
        super(pathOrName);
    }


    /**
     * @param arg
     *            library argument
     */
    public void addArgument(Argument arg) {
        this.arguments.add(arg);
    }


    public Argument getArgument(int index) {
        return this.arguments.get(index);
    }


    public void removeArgument(int index) {
        this.arguments.remove(index);
    }


    public int numberOfArguments() {
        return this.arguments.size();
    }


    public List<Argument> getArguments() {
        return this.arguments;
    }


    public Alias getAlias() {
        return this.alias;
    }
}
