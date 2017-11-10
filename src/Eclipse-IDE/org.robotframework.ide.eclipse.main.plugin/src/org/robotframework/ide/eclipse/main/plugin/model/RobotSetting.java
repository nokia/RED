/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Predicate;

public class RobotSetting extends RobotKeywordCall {

    private static final long serialVersionUID = 1L;

    private SettingsGroup group;

    RobotSetting(final RobotSettingsSection section, final AModelElement<?> linkedElement) {
        this(section, SettingsGroup.NO_GROUP, linkedElement);
    }

    public RobotSetting(final RobotSettingsSection section, final SettingsGroup group,
            final AModelElement<?> linkedElement) {
        super(section, linkedElement);
        this.group = group;
    }

    @Override
    public RobotSettingsSection getParent() {
        return (RobotSettingsSection) super.getParent();
    }

    public SettingsGroup getGroup() {
        return group;
    }

    public void setGroup(final SettingsGroup group) {
        this.group = group;
    }

    public String getNameInGroup() {
        final List<String> arguments = getArguments();
        return arguments.isEmpty() ? "" : arguments.get(0);
    }

    @Override
    public List<String> getArguments() {
        if (arguments == null) {
            final List<RobotToken> allTokens = getLinkedElement().getElementTokens();
            final Iterable<RobotToken> tokensWithoutComments = filter(allTokens, new Predicate<RobotToken>() {

                @Override
                public boolean apply(final RobotToken token) {
                    final List<IRobotTokenType> types = token.getTypes();
                    final IRobotTokenType type = types.isEmpty() ? null : types.get(0);
                    return type != RobotTokenType.START_HASH_COMMENT && type != RobotTokenType.COMMENT_CONTINUE
                            && type != RobotTokenType.SETTING_METADATA_DECLARATION
                            && type != RobotTokenType.SETTING_VARIABLES_DECLARATION
                            && type != RobotTokenType.SETTING_RESOURCE_DECLARATION
                            && type != RobotTokenType.SETTING_LIBRARY_DECLARATION
                            && type != RobotTokenType.SETTING_SUITE_SETUP_DECLARATION
                            && type != RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION
                            && type != RobotTokenType.SETTING_TEST_SETUP_DECLARATION
                            && type != RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION
                            && type != RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION
                            && type != RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION
                            && type != RobotTokenType.SETTING_FORCE_TAGS_DECLARATION
                            && type != RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION;
                }
            });
            return newArrayList(transform(tokensWithoutComments, TokenFunctions.tokenToString()));
        }
        return arguments;
    }

    public boolean isImportSetting() {
        return SettingsGroup.getImportsGroupsSet().contains(getGroup());
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getRobotSettingImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy() {
        return new PageActivatingOpeningStrategy(this);
    }

    public enum SettingsGroup {
        NO_GROUP {
            @Override
            public String getName() {
                return null;
            }
        },
        METADATA {
            @Override
            public String getName() {
                return "Metadata";
            }
        },
        LIBRARIES {
            @Override
            public String getName() {
                return "Library";
            }
        },
        RESOURCES {
            @Override
            public String getName() {
                return "Resource";
            }
        },
        VARIABLES {
            @Override
            public String getName() {
                return "Variables";
            }
        };

        public static EnumSet<SettingsGroup> getImportsGroupsSet() {
            return EnumSet.of(LIBRARIES, RESOURCES, VARIABLES);
        }

        public abstract String getName();
    }
}
