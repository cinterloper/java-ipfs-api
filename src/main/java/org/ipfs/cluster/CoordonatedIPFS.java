package org.ipfs.cluster;

import org.ipfs.api.IPFS;
import org.ipfs.api.Multihash;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by grant
 * <p>
 * for writes
 * this should instruct a specific number of peer nodes to 'pin'
 * an object hash locally, after we have written it to the destionation.
 * so write -> target (sync, ack first storage and return operation id for replication
 * when the replication operation has been confirmed by the target number of peers
 * we should wake up a callback, and mark the replication id as completed
 */


public abstract class CoordonatedIPFS extends IPFS {

    private boolean initalized = false;
    public CoordonatedIPFS(String host, int port) {
        super(host, port);
    }

    public abstract boolean initCluster(IPFS local);

    //cb gets submitted to exe when all replicas have returned
    public abstract void pinRemote(Multihash obj, int replicas, Executor exe, Runnable cb);

    //cbs is the list of call backs to trigger, one for each replica, local is always [0]
    public abstract void pinRemote(Multihash obj, int replicas, Executor exe, List<Runnable> cbs);


}
