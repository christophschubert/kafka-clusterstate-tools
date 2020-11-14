package net.christophschubert.kafka.clusterstate.formats.env;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Cluster {
    @JsonProperty("type")
    public final String type;

    @JsonProperty("clusterId")
    public final String clusterId;

    @JsonProperty("clusterType")
    public final String clusterType;

    @JsonProperty("provider")
    public final String provider;

    @JsonProperty("region")
    public final String region;

    @JsonProperty("availability")
    public final String availability;

    @JsonProperty("name")
    public final String name;

    @JsonProperty("owner")
    public final String owner;

    @JsonProperty("ownerContact")
    public final String ownerContact;

    @JsonProperty("org")
    public final String org;

    @JsonProperty("tags")
    public final Set<String> tags;

    @JsonProperty("clientProperties")
    public final Map<String, Map<String, String>> clientProperties;


    String pathToClusterLevelPriviledges;
    Set<String> pathstoDomainDescriptions;
    @JsonCreator
    public Cluster(
            @JsonProperty("type") String type,
            @JsonProperty("clusterId") String clusterId,
            @JsonProperty("clusterType") String clusterType,
            @JsonProperty("provider") String provider,
            @JsonProperty("region") String region,
            @JsonProperty("availability") String availability,
            @JsonProperty("name") String name,
            @JsonProperty("owner")String owner,
            @JsonProperty("ownerContact") String ownerContact,
            @JsonProperty("org") String org,
            @JsonProperty("tags") Set<String> tags,
            @JsonProperty("clientProperties") Map<String, Map<String, String>> clientProperties
    ) {
        this.type = type;
        this.clusterId = clusterId;
        this.clusterType = clusterType;
        this.provider = provider;
        this.region = region;
        this.availability = availability;
        this.name = name;
        this.owner = owner;
        this.ownerContact = ownerContact;
        this.org = org;
        this.tags = Helpers.emptyForNull(tags);
        this.clientProperties = clientProperties == null ? Collections.emptyMap() : clientProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cluster)) return false;
        Cluster cluster = (Cluster) o;
        return Objects.equals(type, cluster.type) &&
                Objects.equals(clusterId, cluster.clusterId) &&
                Objects.equals(clusterType, cluster.clusterType) &&
                Objects.equals(provider, cluster.provider) &&
                Objects.equals(region, cluster.region) &&
                Objects.equals(availability, cluster.availability) &&
                Objects.equals(name, cluster.name) &&
                Objects.equals(owner, cluster.owner) &&
                Objects.equals(ownerContact, cluster.ownerContact) &&
                Objects.equals(org, cluster.org) &&
                Objects.equals(tags, cluster.tags) &&
                Objects.equals(clientProperties, cluster.clientProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, clusterId, clusterType, provider, region, availability, name, owner, ownerContact, org, tags, clientProperties);
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "type='" + type + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", clusterType='" + clusterType + '\'' +
                ", provider='" + provider + '\'' +
                ", region='" + region + '\'' +
                ", availability='" + availability + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", ownerContact='" + ownerContact + '\'' +
                ", org='" + org + '\'' +
                ", tags=" + tags +
                ", clientProperties=" + clientProperties +
                '}';
    }
}
