/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Optional;

public class SettingTableMultipleDeclarationsJoiner {

    public Optional<SuiteDocumentation> joinSuiteDoc(final List<SuiteDocumentation> docs) {
        Optional<SuiteDocumentation> doc = Optional.absent();

        if (!docs.isEmpty()) {
            SuiteDocumentation sDoc = new SuiteDocumentation(docs.get(0).getDeclaration());

            for (final SuiteDocumentation d : docs) {
                for (final RobotToken docText : d.getDocumentationText()) {
                    sDoc.addDocumentationText(docText);
                }

                for (final RobotToken commentText : d.getComment()) {
                    sDoc.addCommentPart(commentText);
                }
            }

            doc = Optional.of(sDoc);
        }

        return doc;
    }

    public Optional<SuiteSetup> joinSuiteSetup(final List<SuiteSetup> setups) {
        Optional<SuiteSetup> setup = Optional.absent();

        if (!setups.isEmpty()) {
            SuiteSetup sSetup = new SuiteSetup(setups.get(0).getDeclaration());
            joinKeywordBase(sSetup, setups);

            setup = Optional.of(sSetup);
        }

        return setup;
    }

    public Optional<SuiteTeardown> joinSuiteTeardown(final List<SuiteTeardown> teardowns) {
        Optional<SuiteTeardown> teardown = Optional.absent();

        if (!teardowns.isEmpty()) {
            SuiteTeardown sTeardown = new SuiteTeardown(teardowns.get(0).getDeclaration());
            joinKeywordBase(sTeardown, teardowns);

            teardown = Optional.of(sTeardown);
        }

        return teardown;
    }

    public Optional<TestSetup> joinTestSetup(final List<TestSetup> setups) {
        Optional<TestSetup> setup = Optional.absent();

        if (!setups.isEmpty()) {
            TestSetup tSetup = new TestSetup(setups.get(0).getDeclaration());
            joinKeywordBase(tSetup, setups);

            setup = Optional.of(tSetup);
        }

        return setup;
    }

    public Optional<TestTeardown> joinTestTeardown(final List<TestTeardown> teardowns) {
        Optional<TestTeardown> teardown = Optional.absent();

        if (!teardowns.isEmpty()) {
            TestTeardown tTeardown = new TestTeardown(teardowns.get(0).getDeclaration());
            joinKeywordBase(tTeardown, teardowns);

            teardown = Optional.of(tTeardown);
        }

        return teardown;
    }

    private void joinKeywordBase(final AKeywordBaseSetting<?> current,
            final List<? extends AKeywordBaseSetting<?>> base) {
        for (final AKeywordBaseSetting<?> s : base) {
            if (s.getKeywordName() != null) {
                if (current.getKeywordName() != null) {
                    current.addArgument(s.getKeywordName());
                } else {
                    current.setKeywordName(s.getKeywordName());
                }
            }

            for (final RobotToken arg : s.getArguments()) {
                current.addArgument(arg);
            }

            for (final RobotToken commentText : s.getComment()) {
                current.addCommentPart(commentText);
            }
        }
    }

    public Optional<ForceTags> joinForceTag(final List<ForceTags> tags) {
        Optional<ForceTags> tagged = Optional.absent();
        if (tags.isEmpty()) {
            ForceTags ftag = new ForceTags(tags.get(0).getDeclaration());
            joinATag(ftag, tags);

            tagged = Optional.of(ftag);
        }

        return tagged;
    }

    public Optional<DefaultTags> joinDefaultTag(final List<DefaultTags> tags) {
        Optional<DefaultTags> tagged = Optional.absent();
        if (tags.isEmpty()) {
            DefaultTags dtag = new DefaultTags(tags.get(0).getDeclaration());
            joinATag(dtag, tags);

            tagged = Optional.of(dtag);
        }

        return tagged;
    }

    private void joinATag(final ATags<?> currentTag, final List<? extends ATags<?>> tags) {
        for (final ATags<?> t : tags) {
            for (final RobotToken tag : t.getTags()) {
                currentTag.addTag(tag);
            }

            for (final RobotToken comment : t.getComment()) {
                currentTag.addCommentPart(comment);
            }
        }
    }

    public Optional<TestTimeout> joinTestTimeout(final List<TestTimeout> timeouts) {
        Optional<TestTimeout> timeout = Optional.absent();
        if (!timeouts.isEmpty()) {
            TestTimeout tTimeout = new TestTimeout(timeouts.get(0).getDeclaration());

            for (final TestTimeout time : timeouts) {
                if (time.getTimeout() != null) {
                    if (tTimeout.getTimeout() != null) {
                        tTimeout.addMessageArgument(time.getTimeout());
                    } else {
                        tTimeout.setTimeout(time.getTimeout());
                    }
                }

                for (final RobotToken msg : time.getMessageArguments()) {
                    tTimeout.addMessageArgument(msg);
                }

                for (final RobotToken comment : time.getComment()) {
                    tTimeout.addCommentPart(comment);
                }
            }

            timeout = Optional.of(tTimeout);
        }

        return timeout;
    }
}
