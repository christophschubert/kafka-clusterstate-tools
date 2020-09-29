package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;

import java.util.concurrent.ExecutionException;

public interface Action {
    boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException;
}
