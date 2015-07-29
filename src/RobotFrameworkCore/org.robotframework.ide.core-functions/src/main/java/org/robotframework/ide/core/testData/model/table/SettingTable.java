package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.Documentation;


public class SettingTable extends ARobotSectionTable {

    private List<AImported> imports = new LinkedList<>();
    private List<Documentation> documentations = new LinkedList<>();


    public List<AImported> getImports() {
        return imports;
    }


    public void addImported(final AImported imported) {
        imports.add(imported);
    }


    public List<Documentation> getDocumentation() {
        return documentations;
    }


    public void addDocumentation(final Documentation doc) {
        documentations.add(doc);
    }
}
