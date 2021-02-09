package net.christophschubert.kafka.clusterstate;

import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy;

//TODO: find better name for this class
public enum SubjectNameStrategyName {

    TOPIC(new TopicNameStrategy()),
    RECORD(new RecordNameStrategy()),
    TOPICRECORD(new TopicRecordNameStrategy());

    public final SubjectNameStrategy strategy;

    SubjectNameStrategyName(SubjectNameStrategy strategy) {
        this.strategy = strategy;
    }
}
