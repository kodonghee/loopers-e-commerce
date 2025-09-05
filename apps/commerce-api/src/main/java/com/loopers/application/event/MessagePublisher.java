package com.loopers.application.event;


public interface MessagePublisher {
    void publish(Object event);
}
