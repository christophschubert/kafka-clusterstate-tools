package net.christophschubert.kafka.clusterstate;

import net.christophschubert.kafka.clusterstate.actions.*;
import org.apache.kafka.clients.admin.AlterConfigOp;
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
        // TODO: extract RoleBindings
        return new ClusterState(
                aclEntries,
                Collections.emptySet(),
                topicDescriptions,
                Collections.emptySet() //we currently no not track managed prefixes
        );
    }



    interface TopicConfigUpdatePolicy {
        List<Action> calcChanges(
                String topicName, Update<Map<String, String>> configChange);
    }

    static class DoNothingPolicy implements TopicConfigUpdatePolicy {
        @Override
        public List<Action> calcChanges(
                String topicName, Update<Map<String, String>> configChange) {
            return Collections.emptyList();
        }
    }

    static class IncrementalUpdateNoCheck implements TopicConfigUpdatePolicy {
        /**
         * Incrementally applies the new settings from 'desiredConfig'.
         *
         * <p> Settings which are part of currentConfig but not of desiredConfig will not be changed.
         *
         * @param topicName
         * @param configChange
         * @return
         **/

        @Override
        public List<Action> calcChanges(
                String topicName, Update<Map<String, String>> configChange) {
            Map<String, String> updatedConfigs = new HashMap<>();

            configChange.after.forEach(
                    (dKey, dValue) -> {
                        final var current = configChange.before;
                        if (current.containsKey(dKey) || !current.get(dKey).equals(dValue)) {
                            updatedConfigs.put(dKey, dValue);
                        }
                    });
            final IncrementallyUpdateTopicAction action =
                    new IncrementallyUpdateTopicAction(topicName, updatedConfigs);
            return Collections.singletonList(action);
        }
    }

    static class IncrementallyUpdateTopicAction implements Action {

        final String topicName;
        final Map<String, String> topicConfig;

        IncrementallyUpdateTopicAction(String topicName, Map<String, String> topicConfig) {
            this.topicName = topicName;
            this.topicConfig = topicConfig;
        }

        @Override
        public String toString() {
            return "Action: update settings for topic  " + topicName + " " + topicConfig;
        }

        @Override
        public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
            final var adminClient = bundle.adminClient;
            Collection<AlterConfigOp> ops = new ArrayList<>();

            topicConfig.forEach(
                    (k, v) -> {
                        ConfigEntry entry = new ConfigEntry(k, v);
                        AlterConfigOp op = new AlterConfigOp(entry, AlterConfigOp.OpType.SET);
                        ops.add(op);
                    });
            Map<ConfigResource, Collection<AlterConfigOp>> stuff =
                    Collections.singletonMap(new ConfigResource(ConfigResource.Type.TOPIC, topicName), ops);

            try {
                adminClient.incrementalAlterConfigs(stuff).all().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }


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

        //TODO: add changes for role-bindings

        //TODO: questions to consider:
        // how to register schemas for internal streams topics?

        diff.addedSchemaPaths.forEach((topicName, schemaData) -> {
            final String avroTag = "Avro";
            final var key = schemaData.key;
            if (key != null && key.type.equals(avroTag)) {
                actions.add(new RegisterSchemaAction(topicName, "key",schemaData.key));
            }
            final var value = schemaData.value;
            if (value != null && value.type.equals(avroTag)) {
                actions.add(new RegisterSchemaAction(topicName, "value",schemaData.value));
            }
        });
        return actions;
    }
}
