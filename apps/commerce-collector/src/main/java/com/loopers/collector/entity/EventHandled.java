package com.loopers.collector.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_handled")
@Getter
@NoArgsConstructor
public class EventHandled {

    @Id
    private String id;

    private String eventId;
    private String consumer;

    public EventHandled(String eventId, String consumer) {
        this.eventId = eventId;
        this.consumer = consumer;
        this.id = eventId + ":" + consumer;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) {
            this.id = eventId + ":" + consumer;
        }
    }
}
