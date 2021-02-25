package net.christophschubert.kafka.clusterstate;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Set;

/**
 * Helper class to generate the most common entry combinations.
 */
public class AclEntries {
    public static ACLEntry allowAnyHostLiteral(AclOperation operation, String principal, String resourceName, ResourceType type) {
        return new ACLEntry(operation, principal, "*", AclPermissionType.ALLOW, resourceName, type, PatternType.LITERAL);
    }

    public static ACLEntry allowAnyHostPrefix(AclOperation operation, String principal, String resourceName, ResourceType type) {
        return new ACLEntry(operation, principal, "*", AclPermissionType.ALLOW, resourceName, type, PatternType.PREFIXED);
    }


    public static ACLEntry allowAnyHostWildcard(AclOperation operation, String principal, ResourceType type) {
        return new ACLEntry(operation, principal, "*", AclPermissionType.ALLOW, ResourcePattern.WILDCARD_RESOURCE, type, PatternType.LITERAL);
    }

    public static Set<ACLEntry> topicLiteralProducer(String principal, String topicName) {
        return Set.of(
                allowAnyHostLiteral(AclOperation.DESCRIBE, principal, topicName, ResourceType.TOPIC),
                allowAnyHostLiteral(AclOperation.WRITE, principal, topicName, ResourceType.TOPIC)
        );
    }

    public static Set<ACLEntry> topicLiteralConsumer(String principal, String topicName, String groupId) {
        return Set.of(
                allowAnyHostLiteral(AclOperation.DESCRIBE, principal, topicName, ResourceType.TOPIC),
                allowAnyHostLiteral(AclOperation.READ, principal, topicName, ResourceType.TOPIC),
                allowAnyHostLiteral(AclOperation.READ, principal, groupId, ResourceType.GROUP)
        );
    }

    public static Set<ACLEntry> topicLiteralConsumer(String principal, String topicName, String groupId, boolean prefixGroup) {
        return prefixGroup ? topicLiteralConsumerPrefix(principal, topicName, groupId) :
                topicLiteralConsumer(principal, topicName, groupId);
    }

    public static Set<ACLEntry> topicLiteralConsumerPrefix(String principal, String topicName, String groupIdPrefix) {
        return Set.of(
                allowAnyHostLiteral(AclOperation.DESCRIBE, principal, topicName, ResourceType.TOPIC),
                allowAnyHostLiteral(AclOperation.READ, principal, topicName, ResourceType.TOPIC),
                allowAnyHostPrefix(AclOperation.READ, principal, groupIdPrefix, ResourceType.GROUP)
        );
    }

    public static Set<ACLEntry> topicPrefixProducer(String principal, String topicNamePrefix) {
        return Set.of(
                allowAnyHostPrefix(AclOperation.DESCRIBE, principal, topicNamePrefix, ResourceType.TOPIC),
                allowAnyHostPrefix(AclOperation.WRITE, principal, topicNamePrefix, ResourceType.TOPIC)
        );
    }

    public static Set<ACLEntry> topicPrefixConsumer(String principal, String topicNamePrefix, String groupId) {
        return Set.of(
                allowAnyHostPrefix(AclOperation.DESCRIBE, principal, topicNamePrefix, ResourceType.TOPIC),
                allowAnyHostPrefix(AclOperation.READ, principal, topicNamePrefix, ResourceType.TOPIC),
                allowAnyHostLiteral(AclOperation.READ, principal, groupId, ResourceType.GROUP)
        );
    }

    public static Set<ACLEntry> topicPrefixConsumer(String principal, String topicNamePrefix, String groupId, boolean prefixGroup) {
        return prefixGroup ? topicPrefixConsumerPrefix(principal, topicNamePrefix, groupId) :
                topicPrefixConsumer(principal, topicNamePrefix, groupId);
    }

    /**
     * @param principal
     * @param topicNamePrefix
     * @param groupIdPrefix
     * @return
     */
    public static Set<ACLEntry> topicPrefixConsumerPrefix(String principal, String topicNamePrefix, String groupIdPrefix) {
        return Set.of(
                allowAnyHostPrefix(AclOperation.DESCRIBE, principal, topicNamePrefix, ResourceType.TOPIC),
                allowAnyHostPrefix(AclOperation.READ, principal, topicNamePrefix, ResourceType.TOPIC),
                allowAnyHostPrefix(AclOperation.READ, principal, groupIdPrefix, ResourceType.GROUP)
        );
    }

    public static Set<ACLEntry> topicLiteralConsumerWildcard(String principal, String topicName) {
        return Set.of(
                allowAnyHostLiteral(AclOperation.DESCRIBE, principal, topicName, ResourceType.TOPIC),
                allowAnyHostLiteral(AclOperation.READ, principal, topicName, ResourceType.TOPIC),
                allowAnyHostWildcard(AclOperation.READ, principal, ResourceType.GROUP)
        );
    }


    public static Set<ACLEntry> topicPrefixConsumerWildcard(String principal, String topicNamePrefix) {
        return Set.of(
                allowAnyHostPrefix(AclOperation.DESCRIBE, principal, topicNamePrefix, ResourceType.TOPIC),
                allowAnyHostPrefix(AclOperation.READ, principal, topicNamePrefix, ResourceType.TOPIC),
                allowAnyHostWildcard(AclOperation.READ, principal, ResourceType.GROUP)
        );
    }
}
