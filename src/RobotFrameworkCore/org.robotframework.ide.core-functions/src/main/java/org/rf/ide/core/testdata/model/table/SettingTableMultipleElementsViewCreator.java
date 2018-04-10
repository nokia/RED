/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.views.DefaultTagsView;
import org.rf.ide.core.testdata.model.table.setting.views.ForceTagsView;
import org.rf.ide.core.testdata.model.table.setting.views.SuiteDocumentationView;
import org.rf.ide.core.testdata.model.table.setting.views.SuiteSetupView;
import org.rf.ide.core.testdata.model.table.setting.views.SuiteTeardownView;
import org.rf.ide.core.testdata.model.table.setting.views.TestSetupView;
import org.rf.ide.core.testdata.model.table.setting.views.TestTeardownView;
import org.rf.ide.core.testdata.model.table.setting.views.TestTimeoutView;

public class SettingTableMultipleElementsViewCreator {

    public Optional<SuiteDocumentation> createViewAboutSuiteDoc(final List<SuiteDocumentation> docs) {
        return createView(docs, SuiteDocumentationView::new);
    }

    public Optional<SuiteSetup> createViewAboutSuiteSetup(final List<SuiteSetup> setups) {
        return createView(setups, SuiteSetupView::new);
    }

    public Optional<SuiteTeardown> createViewAboutSuiteTeardown(final List<SuiteTeardown> teardowns) {
        return createView(teardowns, SuiteTeardownView::new);
    }

    public Optional<TestSetup> createViewAboutTestSetup(final List<TestSetup> setups) {
        return createView(setups, TestSetupView::new);
    }

    public Optional<TestTeardown> createViewAboutTestTeardown(final List<TestTeardown> teardowns) {
        return createView(teardowns, TestTeardownView::new);
    }

    public Optional<ForceTags> createViewAboutForceTags(final List<ForceTags> tags) {
        return createView(tags, ForceTagsView::new);
    }

    public Optional<DefaultTags> createViewAboutDefaultTags(final List<DefaultTags> tags) {
        return createView(tags, DefaultTagsView::new);
    }

    public Optional<TestTimeout> createViewAboutTestTimeout(final List<TestTimeout> timeouts) {
        return createView(timeouts, TestTimeoutView::new);
    }

    private static <T> Optional<T> createView(final List<T> settings, final Function<List<T>, T> viewCreator) {
        if (settings.isEmpty()) {
            return Optional.empty();
        } else if (settings.size() == 1) {
            return Optional.of(settings.get(0));
        } else {
            return Optional.of(viewCreator.apply(settings));
        }
    }
}
