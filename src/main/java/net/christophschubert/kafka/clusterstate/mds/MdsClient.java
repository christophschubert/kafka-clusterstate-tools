package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MdsClient {

    private final String username;
    private final String password;
    private final String baseUrl;

    HttpClient client = HttpClient.newBuilder().build();

    final ObjectMapper mapper = new ObjectMapper();

    public MdsClient(String username, String password, String baseUrl) {
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
    }

    private HttpRequest buildRequest(String endpoint) {
        return HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .build();

    }

    public String metadataClusterId() throws IOException, InterruptedException {
        final var request = buildRequest("/security/1.0/metadataClusterId");
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    //TODO: parse to proper class
    public Map<String, ?> features() throws IOException, InterruptedException {
        final var request = buildRequest("/security/1.0/features");
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), Map.class);
    }

    public List<Map<String, ?>> roles() throws IOException, InterruptedException {
        final var request = buildRequest("/security/1.0/roles");
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), List.class);
    }

    public Map<String, ?> roles(String rolename) throws IOException, InterruptedException {
        final var request = buildRequest("/security/1.0/roles/" + rolename);
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), Map.class);
    }

    public List<String> roleNames() throws IOException, InterruptedException {
        final var request = buildRequest("/security/1.0/roleNames");
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), List.class);
    }


    private HttpRequest buildPostRequest(String endpoint, String body) {
        return buildRequest(endpoint, "POST", body);
    }

    private HttpRequest buildRequest(String endpoint, String method, String body) {
        return HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .build();
    }


    /**
     * Wraps POST /security/1.0/principals/{principal}/roles/{roleName}
     *
     * @param principal
     * @param roleName
     */
    public void bindClusterRole(String principal, String roleName, Scope scope) throws Exception {
        final var scopeStr = mapper.writeValueAsString(scope);
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName;
        final var request = buildPostRequest(endpoint, scopeStr);
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
        final var scopeStr = mapper.writeValueAsString(scope);
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName;
        final var request = buildRequest(endpoint, "DELETE", scopeStr);
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
            return mapper.readValue(response.body(), new TypeReference<List<ResourcePattern>>() {});
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
        final var body = mapper.writeValueAsString(new ResourceResponse(scope, resources));
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName + "/bindings";
        final var request = buildPostRequest(endpoint, body);
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }



    /**
     *  Incrementally remove the resources from the principal at the given scope/cluster using the given role.
     *
     *  Wraps DELETE /security/1.0/principals/{principal}/roles/{roleName}/bindings
     * @param principal
     * @param roleName
     * @param scope
     * @param resources
     * @throws Exception
     */
    public void removeBinding(String principal, String roleName, Scope scope, List<ResourcePattern> resources) throws Exception {
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName + "/bindings";
        final var response = post(endpoint, new ResourceResponse(scope, resources));

        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }




    /**
     * Overwrite existing resource grants.
     *
     * Wraps PUT /security/1.0/principals/{principal}/roles/{roleName}/bindings
     *
     * @param principal
     * @param roleName
     * @param scope
     * @param resources
     * @throws Exception
     */
    public void setBindings(String principal, String roleName, Scope scope, List<ResourcePattern> resources) throws Exception {
        final var body = mapper.writeValueAsString(new ResourceResponse(scope, resources));
        final var endpoint = "/security/1.0/principals/" + principal + "/roles/" + roleName + "/bindings";
        final var request = buildRequest(endpoint, "PUT", body);
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw exceptionFromResponse(response);
        }
    }


    MdsRestException exceptionFromResponse(HttpResponse<String> response) {
        return new MdsRestException(
                response.statusCode(),
                response.uri().toString(),
                response.body()
        );
    }



    /**
     * Returns the effective list of role names for a principal.
     *
     * Wraps POST /security/1.0/lookup/principals/{principal}/roleNames
     *
     * @param principal
     * @param scope
     * @return
     */
    public List<String> roleNamesForPrincipal(String principal, Scope scope) throws Exception {
        final var scopeStr = mapper.writeValueAsString(scope);
        final var endpoint = "/security/1.0/lookup/principals/" + principal + "/roleNames";
        final var request = buildPostRequest(endpoint, scopeStr);
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), List.class);
        }
        throw new Exception(); //TODO: proper error handling
    }



    /**
     *  Look up the resource bindings for the principal at the given scope/cluster.
     *
     * Wraps POST /security/1.0/lookup/principal/{principal}/resources
     *
     * @param principal
     * @param scope
     * @return
     */
    public Map<String, Map<String, List<ResourcePattern>>> bindingsForPrincipal(String principal, Scope scope) throws Exception {
        final var scopeStr = mapper.writeValueAsString(scope);
        final var endpoint = "/security/1.0/lookup/principal/" + principal + "/resources";
        final var request = buildPostRequest(endpoint, scopeStr);
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<Map<String, Map<String, List<ResourcePattern>>>>() {
            });
        }
        throw exceptionFromResponse(response);
    }


    /**
     *  Look up the KafkaPrincipals who have the given role for the given scope.
     *  Wraps: POST /security/1.0/lookup/role/{roleName}
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
     *
     * @param endpoint
     * @param payload
     * @param <T>
     * @return
     */
    <T> HttpResponse<String> post(String endpoint, T payload) throws IOException, InterruptedException {
        final var body = mapper.writeValueAsString(payload);
        final var request = buildPostRequest(endpoint, body);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    /**
     * Look up the KafkaPrincipals who have the given role on the specified resource for the given scope.
     *
     * Wraps POST /security/1.0/lookup/role/{roleName}/resource/{resourceType}/name/{resourceName}
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


    // Kafka ACL management


    // Cluster Registry


    // Audit Log configuration




    public static void main(String[] args) throws Exception {

        final var client = new MdsClient("alice", "alice-secret", "http://localhost:8090");

        System.out.println(client.metadataClusterId());
        final Map<String, ?> features = client.features();

        System.out.println(client.roles());
        System.out.println(client.roles("ClusterAdmin"));
        System.out.println(client.roleNames());

        final Scope kafkaScope = Scope.forClusterId("GKdJd8MhSYKzmosSO2xaKA");

//        client.unbindClusterRole("User:charlie", "ClusterAdmin", Scope.forClusterId("GKdJd8MhSYKzmosSO2xaKA"));
//
//
//        client.addBinding("User:charlie", "ResourceOwner", kafkaScope,
//                List.of(new ResourcePattern("Topic", "test-2", "PREFIXED")));
//
//        client.lookup("User:charlie", kafkaScope);
//        client.removeBinding("User:charlie", "ResourceOwner", kafkaScope,
//                List.of(new ResourcePattern("Topic", "test-2", "PREFIXED")));
//        client.lookup("User:charlie", kafkaScope);
//
//        System.out.println(client.roleNamesForPrincipal("User:alice", kafkaScope));
//        System.out.println(client.bindingsForPrincipal("User:barnie", kafkaScope));

        System.out.println(client.principalsForRole("ResourceOwner", kafkaScope));
        System.out.println(client.principalsForResource("ResourceOwner", "Group", "schema-registry",kafkaScope));

//        System.out.println(client.lookupRolebindings("User:barnie", "ResourceOwner", kafkaScope));

        //strategy to extract all RBAC role bindings:
        // 1. list all rolenames
        // 2. for a fixed scope (just one Kafka cluster in the MVP), get all principals who have the given role
        // 3. for each principal, get all rolebindings

        final var roleNames = client.roleNames();
        System.out.println(roleNames);
        final var allPrincipals = roleNames.stream().flatMap(roleName -> {
            try { //TODO: fix this mess!
                return client.principalsForRole(roleName, kafkaScope).stream();
            } catch (Exception e) {
                e.printStackTrace();
                return Stream.empty();
            }
        }).collect(Collectors.toSet());
        System.out.println(allPrincipals);

        Set<RbacBinding> rbacBindings = new HashSet<>();
        allPrincipals.forEach(principal -> {
            try {
                final var bfp = client.bindingsForPrincipal(principal, kafkaScope);
                System.out.println("    " + bfp);
                bfp.forEach((bindingPrincipal, rbs) -> {
                  rbs.forEach((roleName, patters) -> {
                      if (patters.isEmpty()) {
                          rbacBindings.add(new RbacBinding(bindingPrincipal, roleName, ResourcePattern.CLUSTERPATTERN));
                      }
                      for (ResourcePattern pattern : patters) {
                          rbacBindings.add(new RbacBinding(bindingPrincipal, roleName, pattern));
                      }
                  });
                });
            } catch (Exception e) {

            }});

        System.out.println("all bindings");
        rbacBindings.forEach(System.out::println);

    }

}
