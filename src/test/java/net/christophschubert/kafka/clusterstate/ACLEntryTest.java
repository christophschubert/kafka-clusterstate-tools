package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ACLEntryTest {
    @Test
    public void jsonRoundTrip() throws JsonProcessingException {
        final ACLEntry aclEntry = new ACLEntry(AclOperation.ALTER, "user:ppp", "local", AclPermissionType.ALLOW, "daTopic", ResourceType.TOPIC, PatternType.LITERAL);
        final ObjectMapper mapper = new ObjectMapper();
        final String s = mapper.writer().writeValueAsString(aclEntry);
        final ACLEntry aclEntryAfterRT = mapper.readValue(s, ACLEntry.class);

        assertEquals(aclEntry, aclEntryAfterRT);
        assertEquals(aclEntry.hashCode(), aclEntryAfterRT.hashCode());
    }

    @Test
    public void fromLocalToApache() {
        final ACLEntry aclEntry = new ACLEntry(AclOperation.ALTER, "user:ppp", "local", AclPermissionType.ALLOW, "daTopic", ResourceType.TOPIC, PatternType.LITERAL);
        final ACLEntry afterRoundTrip = ACLEntry.fromAclBinding(aclEntry.toAclBinding());
        assertEquals(aclEntry, afterRoundTrip);
    }

    @Test
    public void fromApacheToLocal() {
        AclBinding binding = new AclBinding(new ResourcePattern(ResourceType.GROUP, "resourcename", PatternType.LITERAL), new AccessControlEntry("User:sss", "*", AclOperation.ALTER_CONFIGS, AclPermissionType.ALLOW));

        final AclBinding binding1 = ACLEntry.fromAclBinding(binding).toAclBinding();
        assertEquals(binding, binding1);
    }
}
