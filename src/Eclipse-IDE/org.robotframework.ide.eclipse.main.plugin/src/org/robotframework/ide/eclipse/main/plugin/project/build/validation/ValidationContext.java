/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordNameSplitter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class ValidationContext {

    private final RobotRuntimeEnvironment runtimeEnvironment;

    private final RobotVersion version;

    private final Map<String, List<KeywordValidationContext>> accessibleKeywords;

    private final Map<String, LibrarySpecification> librarySpecifications;

    private final Map<ReferencedLibrary, LibrarySpecification> referencedLibrarySpecifications;

    public ValidationContext(final IFile file) {
        final RobotProject project = RedPlugin.getModelManager().getModel().createRobotProject(file.getProject());
        this.runtimeEnvironment = project.getRuntimeEnvironment();
        this.version = runtimeEnvironment != null ? RobotVersion.from(project.getVersion()) : null;
        this.accessibleKeywords = newHashMap();
        this.librarySpecifications = newHashMap();
        this.referencedLibrarySpecifications = newHashMap();
    }

    public SuiteExecutor getExecutorInUse() {
        return runtimeEnvironment.getInterpreter();
    }

    public RobotVersion getVersion() {
        return version;
    }

    public void setAccessibleKeywords(final Map<String, List<KeywordValidationContext>> keywords) {
        accessibleKeywords.putAll(keywords);
    }

    public Set<String> getAccessibleKeywords() {
        return ImmutableSet.copyOf(accessibleKeywords.keySet());
    }

    public boolean isKeywordAccessible(final String name) {
        final KeywordValidationContext keywordValidationContext = extractKeywordValidationContext(KeywordNameSplitter.splitKeywordName(name));
        return keywordValidationContext != null;
    }

    public boolean isKeywordDeprecated(final String name) {
        final KeywordValidationContext keywordValidationContext = extractKeywordValidationContext(KeywordNameSplitter.splitKeywordName(name));
        return keywordValidationContext != null && keywordValidationContext.isDeprecated();
    }

    public boolean isKeywordFromNestedLibrary(final String name) {
        final KeywordValidationContext keywordValidationContext = extractKeywordValidationContext(KeywordNameSplitter.splitKeywordName(name));
        return keywordValidationContext != null && keywordValidationContext.isFromNestedLibrary();
    }
    
    public void setLibrarySpecifications(final Map<String, LibrarySpecification> specs) {
        librarySpecifications.putAll(specs);
    }

    public Map<String, LibrarySpecification> getLibrarySpecificationsAsMap() {
        return librarySpecifications;
    }

    public void setReferencedLibrarySpecifications(final Map<ReferencedLibrary, LibrarySpecification> mapping) {
        referencedLibrarySpecifications.putAll(mapping);
    }

    public Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return referencedLibrarySpecifications;
    }
    
    private KeywordValidationContext extractKeywordValidationContext(final KeywordNameSplitter typedKeywordNameSplitter) {
        List<KeywordValidationContext> keywordsContextList = accessibleKeywords.get(typedKeywordNameSplitter.getKeywordName()
                .toLowerCase());
        if (keywordsContextList != null) {
            final String typedKeywordSourceName = typedKeywordNameSplitter.getKeywordSource();
            for (KeywordValidationContext keywordValidationContext : keywordsContextList) {
                if (hasEqualSources(typedKeywordSourceName, keywordValidationContext)) {
                    return keywordValidationContext;
                }
            }
        }
        return null;
    }
    
    private boolean hasEqualSources(final String typedKeywordSourceName,
            final KeywordValidationContext keywordValidationContext) {
        if (!typedKeywordSourceName.isEmpty()) {
            return !keywordValidationContext.getAlias().isEmpty() ? typedKeywordSourceName.equalsIgnoreCase(keywordValidationContext.getAlias())
                    : typedKeywordSourceName.equalsIgnoreCase(keywordValidationContext.getSourceName());
        }
        return true;
    }

    public static class RobotVersion implements Comparable<RobotVersion> {

        private final int major;

        private final int minor;

        private final Optional<Integer> patch;

        public static RobotVersion from(final String version) {
            final Matcher matcher = Pattern.compile("(\\d)\\.(\\d+)(\\.(\\d+))?").matcher(version);
            if (matcher.find()) {
                if (matcher.groupCount() == 4) {
                    if (matcher.group(4) == null) {
                        return new RobotVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                    } else {
                        return new RobotVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                                Integer.parseInt(matcher.group(4)));
                    }
                }
            }
            throw new IllegalStateException("Unable to recognize Robot Framework version number");
        }

        public RobotVersion(final int major, final int minor) {
            this.major = major;
            this.minor = minor;
            this.patch = Optional.absent();
        }

        public RobotVersion(final int major, final int minor, final int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = Optional.of(patch);
        }

        public boolean isEqualTo(final RobotVersion otherVersion) {
            return major == otherVersion.major && minor == otherVersion.minor && patch.equals(otherVersion.patch);
        }

        public boolean isNotEqualTo(final RobotVersion otherVersion) {
            return !isEqualTo(otherVersion);
        }

        public boolean isOlderThan(final RobotVersion otherVersion) {
            return major < otherVersion.major || (major == otherVersion.major && minor < otherVersion.minor)
                    || (major == otherVersion.major && minor == otherVersion.minor
                            && isLessPatch(patch, otherVersion.patch));
        }

        public boolean isOlderThanOrEqualTo(final RobotVersion otherVersion) {
            return isOlderThan(otherVersion) || isEqualTo(otherVersion);
        }

        public boolean isNewerThan(final RobotVersion otherVersion) {
            return major > otherVersion.major || (major == otherVersion.major && minor > otherVersion.minor)
                    || (major == otherVersion.major && minor == otherVersion.minor
                            && isLessPatch(otherVersion.patch, patch));
        }

        public boolean isNewerOrEqualTo(final RobotVersion otherVersion) {
            return isNewerThan(otherVersion) || isEqualTo(otherVersion);
        }

        private boolean isLessPatch(final Optional<Integer> patch1, final Optional<Integer> patch2) {
            if (!patch1.isPresent() && !patch2.isPresent()) {
                return false;
            } else if (patch1.isPresent() && !patch2.isPresent()) {
                return false;
            } else if (!patch1.isPresent() && patch2.isPresent()) {
                return true;
            } else {
                final int p1 = patch1.get();
                final int p2 = patch2.get();
                return p1 < p2;
            }
        }

        @Override
        public int compareTo(final RobotVersion otherVersion) {
            if (isEqualTo(otherVersion)) {
                return 0;
            } else if (isOlderThan(otherVersion)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
    
    public static class KeywordValidationContext {

        private String keywordName;

        private String sourceName;

        private String alias;

        private boolean isDeprecated;

        private boolean isFromNestedLibrary;

        public KeywordValidationContext(final String keywordName, final String sourceName, final String alias,
                final boolean isDeprecated, final boolean isFromNestedLibrary) {
            this.keywordName = keywordName;
            this.sourceName = sourceName;
            this.alias = alias;
            this.isDeprecated = isDeprecated;
            this.isFromNestedLibrary = isFromNestedLibrary;
        }

        public String getKeywordName() {
            return keywordName;
        }

        public String getSourceName() {
            return sourceName;
        }

        public boolean isDeprecated() {
            return isDeprecated;
        }

        public boolean isFromNestedLibrary() {
            return isFromNestedLibrary;
        }

        public String getAlias() {
            return alias;
        }
    }
}
