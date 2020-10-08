package net.christophschubert.kafka.clusterstate;

import net.christophschubert.kafka.clusterstate.actions.*;
import net.christophschubert.kafka.clusterstate.mds.MdsTools;
import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;
import net.christophschubert.kafka.clusterstate.mds.Scope;
import net.christophschubert.kafka.clusterstate.policies.IncrementalUpdateNoCheck;
import net.christophschubert.kafka.clusterstate.policies.TopicConfigUpdatePolicy;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.SecurityDisabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ClusterStateManager {

    private static final Logger logger = LoggerFactory.getLogger(ClusterStateManager.class);

    //TODO: extract schemas from cluster, whenever possible
    public static ClusterState build(ClientBundle bundle) throws ExecutionException, InterruptedException {
        Map<String, TopicDescription> topicDescriptions = new HashMap<>();
        final Set<String> strings = bundle.adminClient.listTopics().names().get();
        final Collection<ConfigResource> collect = strings.stream().map(s -> new ConfigResource(ConfigResource.Type.TOPIC, s)).collect(Collectors.toSet());
        bundle.adminClient.describeConfigs(collect).all().get().forEach((resource, config) ->
                topicDescriptions.put(resource.name(),
                        new TopicDescription(
                                resource.name(),
                                config.entries().stream().collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value)),
                                null //currently, we do not extract Schemas from cluster
                        ))
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

        Set<RbacBindingInScope> rbacBindings = Collections.emptySet();
        if (bundle.mdsClient != null) {
            //TODO: find a better way to figure out whether we should extract
            try {
                final var clusterId = bundle.mdsClient.metadataClusterId(); // TODO: check which clusterId we should use
                final Scope scope = Scope.forClusterId(clusterId);
                rbacBindings = MdsTools.extractAllRolebindings(bundle.mdsClient, scope);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        return new ClusterState(
                aclEntries,
                rbacBindings,
                topicDescriptions,
                Collections.emptySet() //we currently no not track managed prefixes
        );
    }


    //TODO: add config-setting
    TopicConfigUpdatePolicy topicConfigUpdatePolicy = new IncrementalUpdateNoCheck();

    public List<Action> buildActionList(ClusterStateDiff diff) {
        List<Action> actions = new ArrayList<>();

        //TODO: add logic to check for deletes
        diff.deletedAclEntries.forEach(aclEntry -> actions.add(new DeleteAclAction(aclEntry)));
        diff.addedAclEntries.forEach(aclEntry -> actions.add(new CreateAclAction(aclEntry)));

        //TODO: add logic to check for deletes
        diff.deletedTopicNames.stream().forEach(topicName -> actions.add(new DeleteTopicAction(topicName)));
        diff.addedTopics.forEach((topicName, topicDescription) -> actions.add(new CreateTopicAction(topicDescription)));

        diff.updatedTopicConfigs.values().stream()
                .flatMap(u -> topicConfigUpdatePolicy.calcChanges(u.after.name(), u.map(TopicDescription::configs)).stream())
                .forEach(actions::add);

        diff.deletedRbacBindings.forEach(rbacBinding -> actions.add(new DeleteRbacBindingAction(rbacBinding)));
        diff.addedRbacBindings.forEach(rbacBinding -> actions.add(new CreateRbacBindingAction(rbacBinding)));


        //TODO: questions to consider:
        // how to register schemas for internal streams topics?

        diff.addedSchemaPaths.forEach((topicName, schemaData) -> {
            final var schemaTags = RegisterSchemaAction.tagToProviderType.keySet();
            final var key = schemaData.key;
            if (key != null && schemaTags.contains(key.type)) {
                actions.add(new RegisterSchemaAction(topicName, "key", schemaData.key));
            }
            final var value = schemaData.value;
            if (value != null && schemaTags.contains(value.type)) {
                actions.add(new RegisterSchemaAction(topicName, "value", schemaData.value));
            }
        });
        return actions;
    }
}
