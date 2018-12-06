/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;
import org.rf.ide.core.testdata.text.write.tables.settings.DefaultTagsDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.ForceTagsDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.LibraryImportDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.MetadataDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.ResourceImportDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.SuiteDocumentationDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.SuiteSetupDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.SuiteTeardownDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.SuiteTestTemplateDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.SuiteTestTimeoutDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.TestSetupDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.TestTeardownDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.UnknownSettingDumper;
import org.rf.ide.core.testdata.text.write.tables.settings.VariablesImportDumper;

public class SettingsSectionTableDumper extends ANotExecutableTableDumper<SettingTable> {

    public SettingsSectionTableDumper(final DumperHelper helper) {
        super(helper, getDumpers(helper), true);
    }

    private static List<ISectionElementDumper<SettingTable>> getDumpers(final DumperHelper helper) {
        final List<ISectionElementDumper<SettingTable>> dumpers = new ArrayList<>();
        dumpers.add(new SuiteDocumentationDumper(helper));
        dumpers.add(new SuiteSetupDumper(helper));
        dumpers.add(new SuiteTeardownDumper(helper));
        dumpers.add(new TestSetupDumper(helper));
        dumpers.add(new TestTeardownDumper(helper));
        dumpers.add(new ForceTagsDumper(helper));
        dumpers.add(new DefaultTagsDumper(helper));
        dumpers.add(new SuiteTestTemplateDumper(helper));
        dumpers.add(new SuiteTestTimeoutDumper(helper));
        dumpers.add(new MetadataDumper(helper));
        dumpers.add(new LibraryImportDumper(helper));
        dumpers.add(new ResourceImportDumper(helper));
        dumpers.add(new VariablesImportDumper(helper));
        dumpers.add(new UnknownSettingDumper(helper));

        return dumpers;
    }

    @Override
    public SectionType getSectionType() {
        return SectionType.SETTINGS;
    }
}
