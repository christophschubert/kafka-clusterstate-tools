package net.christophschubert.kafka.clusterstate.mds;

public class MdsRestException extends Exception{
    public final int statusCode;
    public final String url;
    public final String body;

    public MdsRestException(int statusCode, String url, String body) {
        this.statusCode = statusCode;
        this.url = url;
        this.body = body;
    }
}
