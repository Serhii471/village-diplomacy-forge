package com.diplomacy.villagediplomacy.menu;

import com.diplomacy.villagediplomacy.garrison.GarrisonSavedData.Role;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GarrisonSnapshotTest {
    @Test
    void largeNamedRosterFitsForgeMenuPacketAndRetainsPriorityOrder() {
        var entries = IntStream.range(0, 256)
                .mapToObj(i -> new GarrisonSnapshot.Entry(new UUID(0, i),
                        Component.literal("ї".repeat(i == 0 ? 1_000 : 400)),
                        i == 0 ? Role.TRAINING : Role.CIVILIAN, true, 100))
                .toList();
        var bounded = GarrisonSnapshot.fitRoster(entries);
        assertTrue(bounded.size() > 1 && bounded.size() < entries.size());
        assertEquals(entries.get(0).identity(), bounded.get(0).identity());
        assertEquals(Role.TRAINING, bounded.get(0).role());
        assertEquals(entries.get(1), bounded.get(1));

        var snapshot = new GarrisonSnapshot("Г".repeat(256), true, 255, 1, 0, 64, true, bounded, "");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            snapshot.write(buffer);
            assertTrue(buffer.readableBytes() <= DiplomacyTableSnapshot.MAX_NETWORK_BYTES);
            assertEquals(snapshot, GarrisonSnapshot.read(buffer));
        } finally {
            buffer.release();
        }
    }
}
