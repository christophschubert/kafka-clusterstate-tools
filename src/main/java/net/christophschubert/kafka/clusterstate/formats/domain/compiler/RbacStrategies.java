package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.formats.domain.Consumer;
import net.christophschubert.kafka.clusterstate.formats.domain.Producer;
import net.christophschubert.kafka.clusterstate.formats.domain.StreamsApp;
import net.christophschubert.kafka.clusterstate.formats.domain.Topic;
import net.christophschubert.kafka.clusterstate.mds.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RbacStrategies {
    public static final String PREFIXED = "PREFIXED";
    public static final String LITERAL = "LITERAL";

    public static ExtensibleProjectAuthorizationStrategy<RbacBindingInScope> strategyForScope(Scope scope) {
        return new ExtensibleProjectAuthorizationStrategy<>(
                new ConsumerRbacStrategy(scope),
                new ProducerRbacStrategy(scope),
                new StreamsRbacStrategy(scope),
                new ForeignAccessRbacStrategy(scope)
        );
    }

    static class ProducerRbacStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<RbacBindingInScope, Producer> {

        private final Scope scope;

        public ProducerRbacStrategy(Scope scope) {
            this.scope = scope;
        }

        //TODO: rename method of interface
        @Override
        public Set<RbacBindingInScope> acls(Producer producer, ResourceNamingStrategy namingStrategy) {
            final Set<RbacBindingInScope> bindings = new HashSet<>();
            final var project = producer.parent;
            final var restrictToTopics = producer.topics;
            if (restrictToTopics.isEmpty()) {
                //give DeveloperWrite role on whole project
                final var dwRole = new RbacBinding(producer.principal, Roles.DeveloperWrite,
                        new ResourcePattern(Resources.Topic, namingStrategy.projectPrefix(project), PREFIXED));
                bindings.add(new RbacBindingInScope(dwRole, scope));
            }
            final var topicBindings = restrictToTopics.stream()
                    .map(topic -> new RbacBinding(producer.principal, Roles.DeveloperWrite,
                            new ResourcePattern(Resources.Topic, namingStrategy.name(project, topic), LITERAL)))
                    .collect(Collectors.toSet());
            topicBindings.forEach(t -> bindings.add(new RbacBindingInScope(t, scope)));
            //TODO: add roles for transactional IDs
            return bindings;
        }
    }


    static class ConsumerRbacStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<RbacBindingInScope, Consumer> {

        private final Scope scope;

        public ConsumerRbacStrategy(Scope scope) {
            this.scope = scope;
        }

        private RbacBindingInScope addScope(RbacBinding binding) {
            return new RbacBindingInScope(binding, scope);
        }

        @Override
        public Set<RbacBindingInScope> acls(Consumer consumer, ResourceNamingStrategy namingStrategy) {
            final Set<RbacBindingInScope> bindings = new HashSet<>();

            final var project = consumer.parent;
            final var restrictToTopics = consumer.topics;
            if (restrictToTopics.isEmpty()) {
                //give DeveloperRead role on whole project
                final var drRole = new RbacBinding(consumer.principal, Roles.DeveloperRead, new ResourcePattern(Resources.Topic,
                        namingStrategy.projectPrefix(project), PREFIXED));
                bindings.add(addScope(drRole));
            }
            restrictToTopics.stream()
                    .map(topic -> new RbacBinding(consumer.principal, Roles.DeveloperRead,
                            new ResourcePattern(Resources.Topic, namingStrategy.name(project, topic), LITERAL)))
                    .forEach(t -> bindings.add(addScope(t)));

            String patternType = consumer.prefixGroup ? PREFIXED : LITERAL;
            bindings.add(addScope(new RbacBinding(consumer.principal, Roles.DeveloperRead,
                        new ResourcePattern(Resources.Group, namingStrategy.name(project, consumer.groupId), patternType))));

            return bindings;
        }
    }

    static class StreamsRbacStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<RbacBindingInScope, StreamsApp> {

        private final Scope scope;

        public StreamsRbacStrategy(Scope scope) {
            this.scope = scope;
        }

        private RbacBindingInScope addScope(RbacBinding binding) {
            return new RbacBindingInScope(binding, scope);
        }

        @Override
        public Set<RbacBindingInScope> acls(StreamsApp streamsApp, ResourceNamingStrategy namingStrategy) {
            final Set<RbacBindingInScope> bindings = new HashSet<>();
            final var project = streamsApp.parent;
            final var projectPrefix = namingStrategy.projectPrefix(project);
            bindings.add(addScope( new RbacBinding(streamsApp.principal, Roles.DeveloperRead, new ResourcePattern(Resources.Topic,
                    projectPrefix, PREFIXED))));
            bindings.add(addScope( new RbacBinding(streamsApp.principal, Roles.DeveloperWrite, new ResourcePattern(Resources.Topic,
                    projectPrefix, PREFIXED))));
            bindings.add(addScope( new RbacBinding(streamsApp.principal, Roles.DeveloperWrite, new ResourcePattern(Resources.TransactionalId,
                    projectPrefix, PREFIXED))));
            bindings.add(addScope( new RbacBinding(streamsApp.principal, Roles.DeveloperRead, new ResourcePattern(Resources.Group, projectPrefix, PREFIXED))));

            //principal should already have read/write access to all topics from the project, grant DeveloperManage to internal topics:
            bindings.add(addScope( new RbacBinding(streamsApp.principal, Roles.DeveloperManage, new ResourcePattern(Resources.Topic,
                    namingStrategy.name(streamsApp), PREFIXED))));

            return bindings;
        }
    }

    static class ForeignAccessRbacStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<RbacBindingInScope, Topic> {

        private final Scope scope;

        public ForeignAccessRbacStrategy(Scope scope) {
            this.scope = scope;
        }

        @Override
        public Set<RbacBindingInScope> acls(Topic resource, ResourceNamingStrategy namingStrategy) {
            //TODO: implement properly
            return Collections.emptySet();
        }
    }
}
