package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

public class RbacBindingInScope {
    @JsonProperty("binding")
    public final RbacBinding binding;

    @JsonProperty("scope")
    public final Scope scope;

    @JsonCreator
    public RbacBindingInScope(
            @JsonProperty("binding") RbacBinding binding,
            @JsonProperty("scope") Scope scope
    ) {
        this.binding = binding;
        this.scope = scope;
    }

    public boolean isClusterLevel() {
        final Set<String> clusterRoleNames = Set.of(
                "super.user",
                "SystemAdmin",
                "ClusterAdmin",
                "UserAdmin",
                "SecurityAdmin",
                "AuditAdmin",
                "Operator"
        );
        return clusterRoleNames.contains(binding.roleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RbacBindingInScope)) return false;
        RbacBindingInScope that = (RbacBindingInScope) o;
        return Objects.equals(binding, that.binding) &&
                Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(binding, scope);
    }

    @Override
    public String toString() {
        return "RbacBindingInScope{" +
                "binding=" + binding +
                ", scope=" + scope +
                '}';
    }
}
