package org.robotframework.ide.core.testData.text.write;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.SettingTableElementsComparator;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TableHeaderComparator;


public class SectionBuilder {

    @SuppressWarnings("rawtypes")
    public void split(final RobotFile model) {
        List<TableHeader> sortedTableHeaders = getSortedTableHeaders(model);
        for (TableHeader tableHeader : sortedTableHeaders) {
            buildSection(tableHeader);
        }
    }


    @SuppressWarnings("unchecked")
    private void buildSection(
            @SuppressWarnings("rawtypes") final TableHeader header) {
        ModelType modelType = header.getModelType();
        if (modelType == ModelType.SETTINGS_TABLE_HEADER) {
            buildSettingsSection((TableHeader<SettingTable>) header);
        } else if (modelType == ModelType.VARIABLES_TABLE_HEADER) {

        } else if (modelType == ModelType.TEST_CASE_TABLE_HEADER) {

        } else if (modelType == ModelType.KEYWORDS_TABLE_HEADER) {

        } else {
            throw new UnsupportedOperationException("Type " + modelType
                    + " for " + header + " is not supported!");
        }
    }


    private void buildSettingsSection(final TableHeader<SettingTable> header) {
        SettingTable settings = (SettingTable) header.getParent();
        FilePosition beginPosition = header.getBeginPosition();
        List<AModelElement<SettingTable>> modelElements = new LinkedList<>();
        if (beginPosition.isNotSet()) {
            modelElements.addAll(settings.getDefaultTags());
            modelElements.addAll(settings.getDocumentation());
            modelElements.addAll(settings.getForceTags());
            modelElements.addAll(settings.getImports());
            modelElements.addAll(settings.getMetadatas());
            modelElements.addAll(settings.getSuiteSetups());
            modelElements.addAll(settings.getSuiteTeardowns());
            modelElements.addAll(settings.getTestSetups());
            modelElements.addAll(settings.getTestTeardowns());
            modelElements.addAll(settings.getTestTemplates());
            modelElements.addAll(settings.getTestTimeouts());
            modelElements.addAll(settings.getTestTimeouts());
            modelElements.add(header);

            Collections.sort(modelElements,
                    new SettingTableElementsComparator());
        } else {

        }
    }


    @SuppressWarnings("rawtypes")
    private List<TableHeader> getSortedTableHeaders(final RobotFile model) {
        List<TableHeader> headers = new LinkedList<>();
        headers.addAll(model.getSettingTable().getHeaders());
        headers.addAll(model.getVariableTable().getHeaders());
        headers.addAll(model.getTestCaseTable().getHeaders());
        headers.addAll(model.getKeywordTable().getHeaders());

        Collections.sort(headers, new TableHeaderComparator());
        return headers;
    }
}
