package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MdsClient {

    private final static String secV1 = "/security/1.0/";

    private final String username;
    private final String password;
    private final String baseUrl;

    private final HttpClient client = HttpClient.newBuilder().build();

    private final ObjectMapper mapper = new ObjectMapper();

    public MdsClient(String username, String password, String baseUrl) {
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
    }

    // helper functions to deal with requests

    private HttpRequest buildRequest(String endpoint) {
        return HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .build();

    }

    private HttpRequest buildRequest(String endpoint, String method, String body) {
        return HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .build();
    }

    /**
     * Issues a post request to an endpoint.
     *
     * @param endpoint the endpoint
     * @param payload payload, will be serialized to JSON
     * @param <T> type of the body
     * @return the result of the request
     */
    <T> HttpResponse<String> post(String endpoint, T payload) throws IOException, InterruptedException {
        final var body = mapper.writeValueAsString(payload);
        final var request = buildRequest(endpoint, "POST", body);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    <T> HttpResponse<String> delete(String endpoint, T payload) throws IOException, InterruptedException {
        final var body = mapper.writeValueAsString(payload);
        final var request = buildRequest(endpoint, "DELETE", body);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    <T> HttpResponse<String> put(String endpoint, T payload) throws IOException, InterruptedException {
        final var body = mapper.writeValueAsString(payload);
        final var request = buildRequest(endpoint, "PUT", body);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> get(String endpoint) throws IOException, InterruptedException {
        final var request = buildRequest(secV1 + endpoint);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    <T> T getAndParseAs(String endpoint, Class<T> clazz) throws IOException, InterruptedException {
        return mapper.readValue(get(endpoint).body(), clazz);
    }

    <T> T getAndParseAs(String endpoint, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return mapper.readValue(get(endpoint).body(), typeReference);
    }


    MdsRestException exceptionFromResponse(HttpResponse<String> response) {
        return new MdsRestException(
                response.statusCode(),
                response.uri().toString(),
                response.body()
        );
    }

    private static Scope splitScope(Scope scope) {
        if (scope.clusterName != null) {
            return Scope.forClusterName(scope.clusterName);// ensure that clusterName is the only non-null field in the scope if it is set.
        }
        return scope;
    }

    // cluster metadata

    public String metadataClusterId() throws IOException, InterruptedException {
        return get("metadataClusterId").body();
    }

    public FeaturesDescription features() throws IOException, InterruptedException {
        return getAndParseAs("features", FeaturesDescription.class);
    }

    public List<RoleDefinition> roles() throws IOException, InterruptedException {
        return getAndParseAs("roles", new TypeReference<List<RoleDefinition>>() {
        });
    }


    public RoleDefinition roles(String rolename) throws IOException, InterruptedException {
        return getAndParseAs("roles/" + rolename, RoleDefinition.class);
    }

    public List<String> roleNames() throws IOException, InterruptedException {
        return getAndParseAs("roleNames", List.class);
    }



    /**
     * Wraps POST /security/1.0/principals/{principal}/roles/{roleName}
     *
     * @param principal
     * @param roleName
     */
    public void bindClusterRole(String principal, String roleName, Scope scope) throws Exception {
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName;
        final var response = post(endpoint, scope);

        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }

    /**
     * Wraps DELETE /security/1.0/principals/{principal}/roles/{roleName}.
     *
     * @param principal
     * @param roleName
     * @param scope
     * @throws Exception
     */
    public void unbindClusterRole(String principal, String roleName, Scope scope) throws Exception {
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName;
        final var response = delete(endpoint, scope);

        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }

    /**
     * Look up the rolebindings for the principal at the given scope/cluster using the given role.
     * <p>
     * Wraps  POST /security/1.0/principals/{principal}/roles/{roleName}/resources
     *
     * @param principal
     * @param roleName
     * @param scope
     * @return
     */
    public final List<ResourcePattern> lookupRolebindings(String principal, String roleName, Scope scope) throws Exception {
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName + "/resources";
        final var response = post(endpoint, scope);
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<List<ResourcePattern>>() {
            });
        }
        throw exceptionFromResponse(response);
    }


    /**
     * Incrementally grant the resources to the principal at the given scope/cluster using the given role.
     * <p>
     * Wraps POST /security/1.0/principals/{principal}/roles/{roleName}/bindings
     *
     * @param principal
     * @param roleName
     * @param scope
     * @param resources
     */
    public void addBinding(String principal, String roleName, Scope scope, List<ResourcePattern> resources) throws Exception {
        final var body = new ResourceResponse(scope, resources);
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName + "/bindings";
        final var response = post(endpoint, body);
        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }


    /**
     * Incrementally remove the resources from the principal at the given scope/cluster using the given role.
     * <p>
     * Wraps DELETE /security/1.0/principals/{principal}/roles/{roleName}/bindings
     *
     * @param principal
     * @param roleName
     * @param scope
     * @param resources
     * @throws Exception
     */
    public void removeBinding(String principal, String roleName, Scope scope, List<ResourcePattern> resources) throws Exception {
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName + "/bindings";
        final var response = delete(endpoint, new ResourceResponse(scope, resources));
        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }


    /**
     * Overwrite existing resource grants.
     * <p>
     * Wraps PUT /security/1.0/principals/{principal}/roles/{roleName}/bindings
     *
     * @param principal
     * @param roleName
     * @param scope
     * @param resources
     * @throws Exception
     */
    public void setBindings(String principal, String roleName, Scope scope, List<ResourcePattern> resources) throws Exception {
        final var body = new ResourceResponse(scope, resources);
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName + "/bindings";
        final var response = put(endpoint, body);

        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }



    /**
     * Returns the effective list of role names for a principal.
     * <p>
     * Wraps POST /security/1.0/lookup/principals/{principal}/roleNames
     *
     * @param principal
     * @param scope
     * @return
     */
    public Set<String> roleNamesForPrincipal(String principal, Scope scope) throws Exception {
        final var endpoint = "/security/1.0/lookup/principals/" + principal + "/roleNames";
        final var response = post(endpoint, scope);

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<Set<String>>() {});
        }
        throw exceptionFromResponse(response);
    }


    /**
     * Look up the resource bindings for the principal at the given scope/cluster.
     * <p>
     * Wraps POST /security/1.0/lookup/principal/{principal}/resources
     *
     * @param principal
     * @param scope
     * @return
     */
    public Map<String, Map<String, List<ResourcePattern>>> bindingsForPrincipal(String principal, Scope scope) throws Exception {
        final var endpoint = "/security/1.0/lookup/principal/" + principal + "/resources";
        final var response = post(endpoint, scope);

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<Map<String, Map<String, List<ResourcePattern>>>>() {
            });
        }
        throw exceptionFromResponse(response);
    }


    /**
     * Look up the KafkaPrincipals who have the given role for the given scope.
     * Wraps: POST /security/1.0/lookup/role/{roleName}
     *
     * @param roleName
     * @param scope
     * @return
     */
    public List<String> principalsForRole(String roleName, Scope scope) throws Exception {
        final var endpoint = "/security/1.0/lookup/role/" + roleName;
        final var response = post(endpoint, scope);
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), List.class);
        }
        throw exceptionFromResponse(response);
    }



    /**
     * Look up the KafkaPrincipals who have the given role on the specified resource for the given scope.
     * <p>
     * Wraps POST /security/1.0/lookup/role/{roleName}/resource/{resourceType}/name/{resourceName}
     *
     * @param roleName
     * @param resourceType
     * @param resourceName
     * @return
     */
    public List<String> principalsForResource(String roleName, String resourceType, String resourceName, Scope scope) throws Exception {
        final var endpoint = String.format("/security/1.0/lookup/role/%s/resource/%s/name/%s",
                roleName, resourceType, resourceName);
        final var response = post(endpoint, scope);
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), List.class);
        }
        throw exceptionFromResponse(response);
    }

    //TODO
    // Kafka ACL management


    /**
     * Returns a list of all clusters in the registry.
     *
     * Wraps GET /security/1.0/registry/clusters
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Set<ClusterDescription> getClusters() throws IOException, InterruptedException {
        return getAndParseAs("registry/clusters", new TypeReference<Set<ClusterDescription>>(){});
    }


    /**
     *
     *
     * Wraps GET /security/1.0/registry/clusters/{clusterName}
     *
     * @param clusterName name of cluster (ASCII printable characters without spaces)
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public ClusterDescription getCluster(String clusterName) throws IOException, InterruptedException {
        return getAndParseAs("registry/clusters/" + clusterName, ClusterDescription.class);
    }

    /**
     * Define/overwrite named clusters.
     *
     * Wraps POST /security/1.0/registry/clusters
     *
     * @param clusterDescriptions
     */
    public void writeClusters(Set<ClusterDescription> clusterDescriptions) throws IOException, InterruptedException, MdsRestException {
        final var response = post("registry/clusters", clusterDescriptions);
        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }

    /**
     * Delete a named cluster.
     *
     * Wraps DELETE /security/1.0/registry/clusters/{clusterName}
     *
     * @param clusterName name of the cluster to be deleted
     * @throws IOException
     * @throws InterruptedException
     */
    public void deleteCluster(String clusterName) throws IOException, InterruptedException {
        delete("registry/clusters/" + clusterName, "");
    }

    //TODO
    // Audit Log configuration


}
