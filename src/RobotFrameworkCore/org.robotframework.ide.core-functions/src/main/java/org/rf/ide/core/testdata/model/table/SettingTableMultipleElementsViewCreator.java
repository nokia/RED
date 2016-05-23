/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;

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

import com.google.common.base.Optional;

public class SettingTableMultipleElementsViewCreator {

    public Optional<SuiteDocumentation> createViewAboutSuiteDoc(final List<SuiteDocumentation> docs) {
        Optional<SuiteDocumentation> doc = Optional.absent();
        if (!docs.isEmpty()) {
            if (docs.size() == 1) {
                doc = Optional.of(docs.get(0));
            } else {
                doc = Optional.of(((SuiteDocumentation) new SuiteDocumentationView(docs)));
            }
        }

        return doc;
    }

    public Optional<SuiteSetup> createViewAboutSuiteSetup(final List<SuiteSetup> setups) {
        Optional<SuiteSetup> setup = Optional.absent();

        if (!setups.isEmpty()) {
            if (setups.size() == 1) {
                setup = Optional.of(setups.get(0));
            } else {
                setup = Optional.of(((SuiteSetup) new SuiteSetupView(setups)));
            }
        }

        return setup;
    }

    public Optional<SuiteTeardown> createViewAboutSuiteTeardown(final List<SuiteTeardown> teardowns) {
        Optional<SuiteTeardown> teardown = Optional.absent();

        if (!teardowns.isEmpty()) {
            if (teardowns.size() == 1) {
                teardown = Optional.of(teardowns.get(0));
            } else {
                teardown = Optional.of(((SuiteTeardown) new SuiteTeardownView(teardowns)));
            }
        }

        return teardown;
    }

    public Optional<TestSetup> createViewAboutTestSetup(final List<TestSetup> setups) {
        Optional<TestSetup> setup = Optional.absent();

        if (!setups.isEmpty()) {
            if (setups.size() == 1) {
                setup = Optional.of(setups.get(0));
            } else {
                setup = Optional.of(((TestSetup) new TestSetupView(setups)));
            }
        }

        return setup;
    }

    public Optional<TestTeardown> createViewAboutTestTeardown(final List<TestTeardown> teardowns) {
        Optional<TestTeardown> teardown = Optional.absent();

        if (!teardowns.isEmpty()) {
            if (teardowns.size() == 1) {
                teardown = Optional.of(teardowns.get(0));
            } else {
                teardown = Optional.of(((TestTeardown) new TestTeardownView(teardowns)));
            }
        }

        return teardown;
    }

    public Optional<ForceTags> createViewAboutForceTags(final List<ForceTags> tags) {
        Optional<ForceTags> tagged = Optional.absent();

        if (!tags.isEmpty()) {
            if (tags.size() == 1) {
                tagged = Optional.of(tags.get(0));
            } else {
                tagged = Optional.of(((ForceTags) new ForceTagsView(tags)));
            }
        }

        return tagged;
    }

    public Optional<DefaultTags> createViewAboutDefaultTags(final List<DefaultTags> tags) {
        Optional<DefaultTags> tagged = Optional.absent();
        if (!tags.isEmpty()) {
            if (tags.size() == 1) {
                tagged = Optional.of(tags.get(0));
            } else {
                tagged = Optional.of(((DefaultTags) new DefaultTagsView(tags)));
            }
        }

        return tagged;
    }

    public Optional<TestTimeout> createViewAboutTestTimeout(final List<TestTimeout> timeouts) {
        Optional<TestTimeout> timeout = Optional.absent();
        if (!timeouts.isEmpty()) {
            if (timeouts.size() == 1) {
                timeout = Optional.of(timeouts.get(0));
            } else {
                timeout = Optional.of(((TestTimeout) new TestTimeoutView(timeouts)));
            }
        }

        return timeout;
    }
}
