package org.robotframework.ide.core.testData.importer;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;


public class VariablesFileImportReference {

    public static final long FILE_NOT_EXIST_EPOCH = 0;
    private VariablesImport importDeclaration;
    private long lastModificationEpoch = FILE_NOT_EXIST_EPOCH;
    private File variablesFile;
    @SuppressWarnings("rawtypes")
    private List<AVariableImported> variables = new LinkedList<>();


    public VariablesFileImportReference(final VariablesImport importDeclaration) {
        this.importDeclaration = importDeclaration;
    }


    public VariablesImport getImportDeclaration() {
        return importDeclaration;
    }


    public void setVariablesFile(final File variablesFile) {
        this.variablesFile = variablesFile;
        this.lastModificationEpoch = variablesFile.lastModified();
    }


    public File getVariablesFile() {
        return variablesFile;
    }


    @SuppressWarnings("rawtypes")
    public List<AVariableImported> getVariables() {
        return variables;
    }


    public void setLastModificationEpochTime(final long lastModificationEpoch) {
        this.lastModificationEpoch = lastModificationEpoch;
    }


    public long getLastModificationEpochTime() {
        return lastModificationEpoch;
    }


    @SuppressWarnings("rawtypes")
    public void map(final Map<?, ?> varsRead) {
        Set<?> variablesNames = varsRead.keySet();
        for (Object varName : variablesNames) {
            Object varValue = varsRead.get(varName);
            AVariableImported var;
            if (varValue instanceof List) {
                ListVariableImported listVar = new ListVariableImported(""
                        + varName);
                listVar.setValue((List) varValue);
                var = listVar;
            } else if (varValue instanceof Map) {
                DictionaryVariableImported dictVar = new DictionaryVariableImported(
                        "" + varName);
                dictVar.setValue(convert((Map) varValue));
                var = dictVar;
            } else {
                ScalarVariableImported scalarVar = new ScalarVariableImported(
                        "" + varName);
                scalarVar.setValue("" + varValue);
                var = scalarVar;
            }

            variables.add(var);
        }
    }


    private Map<String, Object> convert(@SuppressWarnings("rawtypes") Map m) {
        Map<String, Object> map = new LinkedHashMap<>();
        @SuppressWarnings("rawtypes")
        Set keySet = m.keySet();
        for (Object key : keySet) {
            map.put("" + key, m.get(key));
        }

        return map;
    }
}
