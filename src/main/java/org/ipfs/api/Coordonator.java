package org.ipfs.api;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by grant on 3/4/16.
 */
public class Coordonator {
    private static Storage storage = new Storage(StorageLevel.MEMORY);
    private static Transport transport = new NettyTransport();
    private static AtomixReplica replica;
    DistributedGroup ipfsReplicationGroup;
    DistributedMessageBus bus;
    LocalGroupMember membership;

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
