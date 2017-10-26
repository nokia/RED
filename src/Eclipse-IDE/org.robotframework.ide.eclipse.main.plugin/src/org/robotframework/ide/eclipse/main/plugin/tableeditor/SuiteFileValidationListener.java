package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.RfLintProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;

public class SuiteFileValidationListener implements IResourceChangeListener, SuiteFileMarkersContainer {

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile suiteModel;

    @Inject
    private IEventBroker eventBroker;

    private final Map<Long, IMarker> markers = new HashMap<>();

    @VisibleForTesting
    Map<Long, IMarker> getMarkers() {
        return markers;
    }

    public void init() {
        if (suiteModel.getFile() == null) {
            return;
        }
        try {
            final IMarker[] initialRobotMarkers = suiteModel.getFile().findMarkers(RobotProblem.TYPE_ID, true, 1);
            for (final IMarker marker : initialRobotMarkers) {
                markers.put(marker.getId(), marker);
            }
            final IMarker[] initialRfLintMarkers = suiteModel.getFile().findMarkers(RfLintProblem.TYPE_ID, true, 1);
            for (final IMarker marker : initialRfLintMarkers) {
                markers.put(marker.getId(), marker);
            }
            eventBroker.post(RobotModelEvents.MARKERS_CACHE_RELOADED, suiteModel);
        } catch (final CoreException e) {
            RedPlugin.logWarning("Unable to check changes in markers", e);
        }
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        if (suiteModel.getFile() == null) {
            return;
        }
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {

                @Override
                public boolean visit(final IResourceDelta delta) throws CoreException {
                    if ((delta.getFlags() & IResourceDelta.MARKERS) != 0
                            && delta.getResource().equals(suiteModel.getFile())) {
                        refresh(delta.getMarkerDeltas());
                        return false;
                    }
                    return true;
                }
            });
            eventBroker.post(RobotModelEvents.MARKERS_CACHE_RELOADED, suiteModel);
        } catch (final CoreException e) {
            RedPlugin.logWarning("Unable to check changes in markers", e);
        }
    }

    private synchronized void refresh(final IMarkerDelta[] markerDeltas) throws CoreException {
        for (final IMarkerDelta delta : markerDeltas) {
            if (markers.containsKey(delta.getId()) && delta.getKind() == IResourceDelta.REMOVED) {
                markers.remove(delta.getId());
            } else if (delta.getKind() != IResourceDelta.REMOVED && delta.getMarker().exists()
                    && (delta.getMarker().isSubtypeOf(RobotProblem.TYPE_ID)
                            || delta.getMarker().isSubtypeOf(RfLintProblem.TYPE_ID))) {
                markers.put(delta.getId(), delta.getMarker());
            }
        }
    }

    @Override
    public List<String> getMarkersMessagesFor(final Optional<? extends RobotFileInternalElement> element) {
        if (!element.isPresent()) {
            return new ArrayList<>();
        }
        final List<RobotToken> allTokens = getTokensFor(element.get());

        final List<String> markerDescriptions = new ArrayList<>();
        browseMatchingMarkers(new MarkerVisitor() {
            @Override
            public boolean visit(final IMarker matchingMarker) {
                final String msg = matchingMarker.getAttribute(IMarker.MESSAGE, null);
                if (msg != null) {
                    markerDescriptions.add(msg);
                }
                return true;
            }
        }, allTokens);
        return markerDescriptions;
    }

    @Override
    public Optional<Severity> getHighestSeverityMarkerFor(final Optional<? extends RobotFileInternalElement> element) {
        if (!element.isPresent()) {
            return Optional.empty();
        }
        final List<RobotToken> allTokens = getTokensFor(element.get());

        final AtomicBoolean hasError = new AtomicBoolean(false);
        final AtomicBoolean hasWarning = new AtomicBoolean(false);
        final AtomicBoolean hasInfo = new AtomicBoolean(false);
        browseMatchingMarkers(new MarkerVisitor() {

            @Override
            public boolean visit(final IMarker matchingMarker) {
                if (matchingMarker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
                    hasError.set(true);
                    return false;
                } else if (matchingMarker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
                    hasWarning.set(true);
                } else if (matchingMarker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_INFO) {
                    hasInfo.set(true);
                }
                return true;
            }
        }, allTokens);
        if (hasError.get()) {
            return Optional.of(Severity.ERROR);
        } else if (hasWarning.get()) {
            return Optional.of(Severity.WARNING);
        } else if (hasInfo.get()) {
            return Optional.of(Severity.INFO);
        }
        return Optional.empty();
    }

    private List<RobotToken> getTokensFor(final RobotFileInternalElement element) {
        final AModelElement<?> modelElement = (AModelElement<?>) element.getLinkedElement();

        if (modelElement.getModelType() == ModelType.TEST_CASE) {
            return newArrayList(modelElement.getDeclaration());
        } else if (modelElement.getModelType() == ModelType.USER_KEYWORD) {
            // for keywords we'are displaying arguments in the same line as keyword name,
            // so we need to get markers for arguments setting
            final UserKeyword keyword = (UserKeyword) modelElement;

            final List<RobotToken> tokens = new ArrayList<>();
            tokens.add(modelElement.getDeclaration());
            for (final KeywordArguments arguments : keyword.getArguments()) {
                tokens.addAll(arguments.getElementTokens());
            }
            return tokens;
        } else {
            return modelElement.getElementTokens();
        }
    }

    private synchronized void browseMatchingMarkers(final MarkerVisitor visitor, final List<RobotToken> tokens) {
        // TODO : consider using e.g. segment tree for searching using segments (ranges), when
        // performance becomes the issue
        final Set<IMarker> alreadyVisitedMarkers = new HashSet<>();
        for (final RobotToken token : tokens) {
            for (final IMarker marker : markers.values()) {
                if (alreadyVisitedMarkers.contains(marker)) {
                    continue;
                }
                final Range<Integer> tokenRange = getTokenRange(token);
                final Range<Integer> markerRange = getMarkerRange(marker);
                final int markerLine = marker.getAttribute(IMarker.LINE_NUMBER, -2);
                
                if ((markerRange != null && tokenRange.isConnected(markerRange))
                        || (markerRange == null && token.getFilePosition().getLine() == markerLine)) {
                    alreadyVisitedMarkers.add(marker);
                    final boolean shallContinue = visitor.visit(marker);
                    if (!shallContinue) {
                        return;
                    }
                }
            }
        }
    }

    private Range<Integer> getTokenRange(final RobotToken token) {
        final FilePosition tokenPosition = token.getFilePosition();
        return Range.closed(tokenPosition.getOffset(), tokenPosition.getOffset() + token.getRaw().length());
    }

    private Range<Integer> getMarkerRange(final IMarker marker) {
        final int start = marker.getAttribute(IMarker.CHAR_START, -1);
        final int end = marker.getAttribute(IMarker.CHAR_END, -1);
        return start != -1 && end != -1 ? Range.closed(start, end) : null;
    }

    private static interface MarkerVisitor {

        boolean visit(IMarker matchingMarker);
    }
}
