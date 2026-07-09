package net.pendesu.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pendesu.LibrariesLegends;

public record ScrollBookBundlePayload(int slotId, int direction) implements CustomPayload {

    public static final CustomPayload.Id<ScrollBookBundlePayload> ID =
            new CustomPayload.Id<>(Identifier.of(LibrariesLegends.MOD_ID, "scroll_book_bundle"));

    public static final PacketCodec<RegistryByteBuf, ScrollBookBundlePayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    ScrollBookBundlePayload::slotId,
                    PacketCodecs.INTEGER,
                    ScrollBookBundlePayload::direction,
                    ScrollBookBundlePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}