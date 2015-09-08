package org.robotframework.ide.core.testData.model.listener;

public abstract class ARobotModelEvent<T> {

    private final T eventSource;
    private final IType eventType;


    public ARobotModelEvent(final T eventSource, final IType eventType) {
        this.eventSource = eventSource;
        this.eventType = eventType;
    }


    public T getSource() {
        return eventSource;
    }


    public IType getType() {
        return eventType;
    }

    public interface IType {

        long getEventId();
    }
}
