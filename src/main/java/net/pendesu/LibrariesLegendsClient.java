package net.pendesu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.pendesu.item.ModItems;
import net.pendesu.item.custom.BookBundleItem;
import net.pendesu.mixin.client.HandledScreenAccessor;
import net.pendesu.networking.ScrollBookBundlePayload;

public class LibrariesLegendsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof HandledScreen<?>)) {
                return;
            }

            ScreenMouseEvents.allowMouseScroll(screen).register((currentScreen, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
                if (!(currentScreen instanceof HandledScreen<?> handledScreen)) {
                    return true;
                }

                Slot hoveredSlot = ((HandledScreenAccessor) handledScreen).librarieslegends$getFocusedSlot();

                if (hoveredSlot == null || !hoveredSlot.hasStack()) {
                    return true;
                }

                ItemStack hoveredStack = hoveredSlot.getStack();

                if (!hoveredStack.isOf(ModItems.BOOK_BUNDLE)) {
                    return true;
                }

                int direction = verticalAmount > 0 ? -1 : 1;

                // Optional client-side update so the highlight feels instant.
                BookBundleItem.scrollSelectedStack(hoveredStack, direction);

                // Real server-side update.
                ClientPlayNetworking.send(new ScrollBookBundlePayload(hoveredSlot.id, direction));

                // false = consume the scroll so the screen does not also handle it normally.
                return false;
            });
        });
    }
}