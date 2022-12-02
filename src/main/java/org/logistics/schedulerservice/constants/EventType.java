package org.logistics.schedulerservice.constants;

public enum EventType {
    CREATE_SCHEDULE("create_schedule"),
    UPDATE_SCHEDULE("update_schedule"),
    DELETE_SCHEDULE("delete_schedule");

    private final String text;

    EventType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
