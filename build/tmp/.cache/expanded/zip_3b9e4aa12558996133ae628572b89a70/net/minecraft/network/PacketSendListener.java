package net.minecraft.network;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;

public interface PacketSendListener {
    static PacketSendListener thenRun(final Runnable pOnSuccessOrFailure) {
        return new PacketSendListener() {
            @Override
            public void onSuccess() {
                pOnSuccessOrFailure.run();
            }

            @Nullable
            @Override
            public Packet<?> onFailure() {
                pOnSuccessOrFailure.run();
                return null;
            }
        };
    }

    static PacketSendListener exceptionallySend(final Supplier<Packet<?>> pExceptionalPacketSupplier) {
        return new PacketSendListener() {
            @Nullable
            @Override
            public Packet<?> onFailure() {
                return pExceptionalPacketSupplier.get();
            }
        };
    }

    default void onSuccess() {
    }

    @Nullable
    default Packet<?> onFailure() {
        return null;
    }
}