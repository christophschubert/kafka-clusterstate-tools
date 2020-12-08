package net.christophschubert.kafka.clusterstate;

import net.christophschubert.kafka.clusterstate.actions.*;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.DefaultStrategies;
import net.christophschubert.kafka.clusterstate.mds.MdsTools;
import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;
import net.christophschubert.kafka.clusterstate.mds.Scope;
import net.christophschubert.kafka.clusterstate.policies.IncrementalUpdateNoCheck;
import net.christophschubert.kafka.clusterstate.policies.TopicConfigUpdatePolicy;
import net.christophschubert.kafka.clusterstate.utils.Sets;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
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

    public static ClusterState build(ClientBundle bundle) throws ExecutionException, InterruptedException {
        return build(bundle, true);
    }

    /**
     *
     * @param bundle
     * @param includeDefaults
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    //TODO: extract schemas from cluster, whenever possible
    public static ClusterState build(ClientBundle bundle, boolean includeDefaults) throws ExecutionException, InterruptedException {
        Map<String, TopicDescription> topicDescriptions = new HashMap<>();
        final Set<String> topicNames = bundle.adminClient.listTopics().names().get();
        final Collection<ConfigResource> collect = topicNames.stream().map(s -> new ConfigResource(ConfigResource.Type.TOPIC, s)).collect(Collectors.toSet());
        bundle.adminClient.describeConfigs(collect).all().get().forEach((resource, config) ->
                topicDescriptions.put(resource.name(),
                        new TopicDescription(
                                resource.name(),
                                config.entries().stream()
                                        .filter(configEntry -> includeDefaults || !configEntry.isDefault())
                                        .collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value)),
                                null //currently, we do not extract Schemas from cluster
                        ))
        );
        //extract partitioninfo:
        final var describeTopicsResult = bundle.adminClient.describeTopics(topicNames);
        describeTopicsResult.all().get().forEach((topicName, description) -> {
            final var configs = topicDescriptions.get(topicName).configs();
            configs.put("num.partitions", Integer.toString(description.partitions().size()));
            //TODO: check whether there is a better way to extract the replication factor
            configs.put("replication.factor", Integer.toString(description.partitions().get(0).replicas().size()));
        });
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

        Set<RbacBindingInScope> rbacBindings = new HashSet<>();
        if (bundle.mdsClient != null) {
            //TODO: find a better way to figure out whether we should extract role bindings from cluster
            try {
                Set<Scope> scopes = bundle.mdsScopes;
                if (scopes.isEmpty()) {
                    final var clusterId = bundle.mdsClient.metadataClusterId(); // TODO: check which clusterId we should use
                    scopes = Set.of(Scope.forClusterId(clusterId));
                }
                scopes.forEach(scope ->
                        {
                            try {
                                Scope t = Scope.forClusterName(scope.clusterName);
                                final var rbacBindingInScope = MdsTools.extractAllRolebindings(bundle.mdsClient, t);
                                rbacBindings.addAll(rbacBindingInScope);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                );
            } catch (Exception e) {
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
