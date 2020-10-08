package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class HostPort {
    @JsonProperty("host")
    public final String host;

    @JsonProperty("port")
    public final int port;

    @JsonCreator
    public HostPort(
            @JsonProperty("host") String host,
            @JsonProperty("port") int port
    ) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostPort)) return false;
        HostPort hostPort = (HostPort) o;
        return port == hostPort.port &&
                Objects.equals(host, hostPort.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "HostPort{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
