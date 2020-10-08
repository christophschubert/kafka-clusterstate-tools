package net.christophschubert.kafka.clusterstate.mds;

import java.util.Objects;

// to check: this only makes sense relative to a given scope (?), should scope be included in class?
public class RbacBinding {
    public final String principal;
    public final String roleName;
    public final ResourcePattern resourcePattern;

    public RbacBinding(String principal, String roleName, ResourcePattern resourcePattern) {
        this.principal = principal;
        this.roleName = roleName;
        this.resourcePattern = resourcePattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RbacBinding)) return false;
        RbacBinding that = (RbacBinding) o;
        return Objects.equals(principal, that.principal) &&
                Objects.equals(roleName, that.roleName) &&
                Objects.equals(resourcePattern, that.resourcePattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, roleName, resourcePattern);
    }

    @Override
    public String toString() {
        return "RbacBinding{" +
                "principal='" + principal + '\'' +
                ", roleName='" + roleName + '\'' +
                ", resourcePattern=" + resourcePattern +
                '}';
    }
}
