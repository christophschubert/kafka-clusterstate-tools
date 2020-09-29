package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Objects;

/**
 * Wrapper around Kafka Acl types to allow for easy JSON serialization.
 */
public class ACLEntry {
    final AclOperation operation;
    final String principal;
    final String host;
    final AclPermissionType permissionType;

    final String resourceName;
    final ResourceType resourceType;
    final PatternType patternType;

    //TODO: check for permission types 'UNKNOWN'
    @JsonCreator
    public ACLEntry(@JsonProperty("operation")String operation,
                    @JsonProperty("principal")String principal,
                    @JsonProperty("host") String host,
                    @JsonProperty("permissionType") String permissionType,
                    @JsonProperty("resourceName") String resourceName,
                    @JsonProperty("resourceType") String resourceType,
                    @JsonProperty("patternType") String patternType
                    )
    {
        this(
            AclOperation.fromString(operation),
                principal,
                host,
                AclPermissionType.fromString(permissionType),
                resourceName,
                ResourceType.fromString(resourceType),
                PatternType.fromString(patternType)
        );
    }

    public ACLEntry(
             AclOperation operation,
            String principal,
            String host,
           AclPermissionType permissionType,
            String resourceName,
            ResourceType resourceType,
             PatternType patternType) {
        this.operation = operation;
        this.principal = principal;
        this.host = host;
        this.permissionType = permissionType;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.patternType = patternType;
    }

    public static ACLEntry fromAclBinding(final AclBinding binding) {
        return new ACLEntry(binding.entry().operation(),
                binding.entry().principal(),
                binding.entry().host(),
                binding.entry().permissionType(),
                binding.pattern().name(),
                binding.pattern().resourceType(),
                binding.pattern().patternType());
    }

    public AclBinding toAclBinding() {
        return new AclBinding(new ResourcePattern(resourceType, resourceName, patternType),
                new AccessControlEntry(principal, host, operation, permissionType));
    }

    @Override
    public String toString() {
        return "ACLEntry{" +
                "operation=" + operation +
                ", principal='" + principal + '\'' +
                ", host='" + host + '\'' +
                ", permissionType=" + permissionType +
                ", resourceName='" + resourceName + '\'' +
                ", resourceType=" + resourceType +
                ", patternType=" + patternType +
                '}';
    }

    public static void main(String[] args) throws JsonProcessingException {

    }


    public AclOperation operation() {
        return operation;
    }

    @JsonGetter("operation")
    public String operationAsString() {
        return operation.name();
    }

    @JsonGetter("principal")
    public String principal() {
        return principal;
    }

    @JsonGetter("host")
    public String host() {
        return host;
    }

    public AclPermissionType permissionType() {
        return permissionType;
    }



    @JsonGetter("permissionType")
    public String permissionTypeAsString() {
        return permissionType.toString();
    }

    @JsonGetter("resourceName")
    public String resourceName() {
        return resourceName;
    }

    public ResourceType resourceType() {
        return resourceType;
    }

    @JsonGetter("resourceType")
    public String resourceTypeAsString() {
        return resourceType.toString();
    }

    public PatternType patternType() {
        return patternType;
    }

    @JsonGetter("patternType")
    public String patternTypeAsString() {
        return patternType.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ACLEntry)) return false;
        ACLEntry aclEntry = (ACLEntry) o;
        return operation == aclEntry.operation &&
                Objects.equals(principal, aclEntry.principal) &&
                Objects.equals(host, aclEntry.host) &&
                permissionType == aclEntry.permissionType &&
                Objects.equals(resourceName, aclEntry.resourceName) &&
                resourceType == aclEntry.resourceType &&
                patternType == aclEntry.patternType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, principal, host, permissionType, resourceName, resourceType, patternType);
    }
}
