/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.importer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.AImported.Type;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;


public class ResourceImporter {

    public List<ResourceImportReference> importResources(
            final RobotParser parser, final RobotFileOutput robotFile) {
        List<ResourceImportReference> importedReferences = new LinkedList<>();

        SettingTable settingTable = robotFile.getFileModel().getSettingTable();
        if (settingTable.isPresent()) {
            List<AImported> imports = settingTable.getImports();
            for (AImported imported : imports) {
                Type type = imported.getType();
                if (type == Type.RESOURCE) {
                    File toImport = new File(imported.getPathOrName().getText()
                            .toString());
                    List<RobotFileOutput> parsed = parser.parse(toImport);
                    if (parsed.isEmpty()) {
                        robotFile.addBuildMessage(BuildMessage
                                .createErrorMessage(
                                        "Couldn't import resource file.",
                                        toImport.getAbsolutePath()));
                    } else {
                        importedReferences.add(new ResourceImportReference(
                                (ResourceImport) imported, parsed.get(0)));
                    }
                }
            }
        }
        robotFile.addResourceReferences(importedReferences);

        return importedReferences;
    }
}
