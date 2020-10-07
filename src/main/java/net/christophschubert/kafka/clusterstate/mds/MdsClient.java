package net.christophschubert.kafka.clusterstate.mds;

import net.christophschubert.kafka.clusterstate.formats.domain.Helpers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class MdsClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        final String username = "alice";
        final String password = "alice-secret";
        final String baseUrl = "http://localhost:8090";

        HttpClient client = HttpClient.newBuilder().build();


        final var authorization = HttpRequest.newBuilder(URI.create(baseUrl + "/security/1.0/authenticate"))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes())).build();
        final var send = client.send(authorization, HttpResponse.BodyHandlers.ofString());
        System.out.println(send);

        final var listRoles = HttpRequest.newBuilder(URI.create(baseUrl + "/security/1.0/roles")).header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes())).build();
        final var send1 = client.send(listRoles, HttpResponse.BodyHandlers.ofString());

        System.out.println(send1);
        System.out.println(send1.body());
    }


}
