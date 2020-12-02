package net.christophschubert.kafka.clusterstate.formats.env;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Model class for a cluster in Confluent Cloud.
 */
public class CloudCluster {
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

    @JsonProperty("principals")
    public final Map<String, String> principals;

    @JsonProperty("domainFileFolders")
    public final Set<String> domainFileFolders;

    @JsonProperty("clusterLevelAccessPath")
    public final String clusterLevelAccessPath;

    @JsonCreator
    public CloudCluster(
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
            @JsonProperty("clientProperties") Map<String, Map<String, String>> clientProperties,
            @JsonProperty("principals") Map<String, String> principals,
            @JsonProperty("domainFileFolders") Set<String> domainFileFolders,
            @JsonProperty("clusterLevelAccessPath") String clusterLevelAccessPath
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
        this.clientProperties = Helpers.emptyForNull(clientProperties);
        this.principals = Helpers.emptyForNull(principals);
        this.domainFileFolders = domainFileFolders;
        this.clusterLevelAccessPath = clusterLevelAccessPath;
    }

    @Override
    public String toString() {
        return "CloudCluster{" +
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
                ", principals=" + principals +
                ", domainFileFolder='" + domainFileFolders + '\'' +
                ", clusterLevelAccessFolder='" + clusterLevelAccessPath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CloudCluster)) return false;
        CloudCluster that = (CloudCluster) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(clusterId, that.clusterId) &&
                Objects.equals(clusterType, that.clusterType) &&
                Objects.equals(provider, that.provider) &&
                Objects.equals(region, that.region) &&
                Objects.equals(availability, that.availability) &&
                Objects.equals(name, that.name) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(ownerContact, that.ownerContact) &&
                Objects.equals(org, that.org) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(clientProperties, that.clientProperties) &&
                Objects.equals(principals, that.principals) &&
                Objects.equals(domainFileFolders, that.domainFileFolders) &&
                Objects.equals(clusterLevelAccessPath, that.clusterLevelAccessPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, clusterId, clusterType, provider, region, availability, name, owner, ownerContact, org, tags, clientProperties, principals, domainFileFolders, clusterLevelAccessPath);
    }
}
