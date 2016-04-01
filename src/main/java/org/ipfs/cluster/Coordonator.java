package org.ipfs.cluster;

import io.atomix.Atomix;


import io.atomix.AtomixClient;
import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.transport.Transport;
import io.atomix.collections.DistributedQueue;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.group.DistributedGroup;
import io.atomix.group.LocalGroupMember;
import io.atomix.messaging.DistributedMessageBus;
import org.ipfs.api.JSONParser;
import org.ipfs.api.Multihash;

import java.security.acl.Group;
import java.util.*;
import java.util.function.Function;

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


public class Coordonator {
    //enh.... the worst that could happen is the replication operation is never confirmed
    // but the sync write to the first target is either already happened or never finishes
    private static Storage storage = new Storage(StorageLevel.MEMORY);

    private static Transport transport = new NettyTransport();
    private static AtomixReplica replica;
    private DistributedGroup ipfsReplicationGroup;
    private DistributedMessageBus bus;
    private LocalGroupMember membership;
    private AtomixClient atomix;
    private Address local = new Address("localhost", 5000);

    Coordonator(Address address, List<Address> members) {

        replica = AtomixReplica.builder(address, members)
                .withStorage(storage)
                .withTransport(transport)
                .build();
        atomix = AtomixClient.builder(local).withTransport(new NettyTransport()).build();

        try {
            bus = replica.getMessageBus("bus").get();
        } catch (Exception e) {
            System.out.println("exception getting bus handle");
        }

    }


    public boolean Replicate(Multihash obj, int count) {

        try {
            bus = replica.getMessageBus(membership.id()).get();
            bus.consumer(membership.id(), message -> {
                Map msg = (Map)JSONParser.parse(message);
                //callbacks from peers replication attemps
                if(msg.get("result") == "success"){
                    System.out.println("got replication sucess for:"+msg.get("taskid"));
                }else{
                    //resubmit task
                    //count/limit number of times? 
                }
            });
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    public boolean joinGroup() {
        try {
            ipfsReplicationGroup = replica.getGroup("ipfsCluster").get();
            membership = ipfsReplicationGroup.join().get();
        } catch (Exception e) {
            System.out.println("could not join replication group");
            return (false);
        }


        return (true);
    }

    public UUID replicationRequest(String dataId, final int count) {
        //put this in my local ipfs store if i am a replication sink
        //top prevent me from completing then just my part of the job
        UUID taskId = UUID.randomUUID();
        replica.<String>getQueue(membership.id()).thenAccept(queue -> {
            for (int tasks = count; tasks > 0; tasks--) {
                queue.add(taskId.toString() + ':' + dataId);
            }
        }); //the queue name has my id in it, it holds
        return (taskId);
    }


}
