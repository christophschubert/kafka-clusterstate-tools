package net.christophschubert.kafka.clusterstate;

import net.christophschubert.kafka.clusterstate.actions.*;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.SecurityDisabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ClusterStateManager {

    private static final Logger logger = LoggerFactory.getLogger(ClusterStateManager.class);

    public static ClusterState build(ClientBundle bundle) throws ExecutionException, InterruptedException {
        Map<String, TopicDescription> topicDescriptions = new HashMap<>();
        final Set<String> strings = bundle.adminClient.listTopics().names().get();
        final Collection<ConfigResource> collect = strings.stream().map(s -> new ConfigResource(ConfigResource.Type.TOPIC, s)).collect(Collectors.toSet());
        bundle.adminClient.describeConfigs(collect).all().get().forEach((resource, config) ->
                topicDescriptions.put(resource.name(),
                        new TopicDescription(resource.name(), config.entries().stream().collect( Collectors.toMap(ConfigEntry::name, ConfigEntry::value)
                        )))
        );
        Set<ACLEntry> aclEntries = Collections.emptySet();
        try {
            final Collection<AclBinding> aclBindings = bundle.adminClient.describeAcls(AclBindingFilter.ANY).values().get();
            aclEntries
                    = aclBindings.stream().map(ACLEntry::fromAclBinding).collect(Collectors.toSet());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof SecurityDisabledException) {
                logger.info("Security is not enabled on cluster");
            } else {
                throw e;
            }
        }
        // TODO: extract RoleBindings
        return new ClusterState(aclEntries, Collections.emptySet(), topicDescriptions);
    }




    public List<Action> buildActionList(ClusterStateDiff diff) {
        List<Action> actions = new ArrayList<>();

        //TODO: add logic to check for deletes
        diff.deletedAclEntries.forEach(aclEntry -> actions.add(new DeleteAclAction(aclEntry)));
        diff.addedAclEntries.forEach(aclEntry -> actions.add(new CreateAclAction(aclEntry)));

        //TODO: add logic to check for deletes
        diff.deletedTopicNames.forEach(topicName -> actions.add(new DeleteTopicAction(topicName)));
        diff.addedTopics.forEach((topicName, topicDescription) -> actions.add(new CreateTopicAction(topicDescription)));

        //TODO: add logic for updates

        return actions;
    }
}
