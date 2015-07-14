package org.robotframework.ide.core.testData.model.table.settings.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Argument;
import org.robotframework.ide.core.testData.model.table.settings.AExternalImported;
import org.robotframework.ide.core.testData.model.table.settings.ImportElementLocation;


public class Variables extends AExternalImported {

    private ImportedVariables variablesWord;
    private List<Argument> initialArguments = new LinkedList<>();


    public Variables(ImportedVariables variablesWord,
            ImportElementLocation location) {
        super(ImportTypes.VARIABLES, location);
        this.variablesWord = variablesWord;
    }
}
