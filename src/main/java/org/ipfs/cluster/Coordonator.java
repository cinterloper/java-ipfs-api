package org.ipfs.cluster;

import io.atomix.Atomix;


import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.transport.Transport;
import io.atomix.coordination.DistributedGroup;
import io.atomix.coordination.GroupMember;
import io.atomix.coordination.LocalGroupMember;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.messaging.DistributedMessageBus;
import org.ipfs.api.Multihash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by grant
 *
 * for writes
 * this should instruct a specific number of peer nodes to 'pin'
 * an object hash locally, after we have written it to the destionation.
 * so write -> target (sync, ack first storage and return operation id for replication
 * when the replication operation has been confirmed by the target number of peers
 * we should wake up a callback, and mark the replication id as completed
 *
 *
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

    Coordonator(Address address, List<Address> members) {

        replica = AtomixReplica.builder(address, members)
                .withStorage(storage)
                .withTransport(transport)
                .build();
        bus = Atomix.getMessageBus("bus").get();

    }


    public boolean Replicate(Multihash obj, int count) {

        Random randomizer = new Random();
        List<Object> remotes = Arrays.asList(ipfsReplicationGroup.members().toArray());
        GroupMember member = (GroupMember) remotes.get(randomizer.nextInt(remotes.size()));

        if (!member.id().equals(membership.id())) {
            bus.producer(member.id()).
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

        bus.consumer(membership.id(), message -> System.out.println("Consumed " + message));

        return (true);
    }


}
