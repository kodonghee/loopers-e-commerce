package com.loopers.collector.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_handled")
@Getter
@NoArgsConstructor
public class EventHandled {

    @Id
    private String eventId;

    public EventHandled(String eventId) {
        this.eventId = eventId;
    }
}
