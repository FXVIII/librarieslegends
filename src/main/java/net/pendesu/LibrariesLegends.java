package net.pendesu;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;
import net.pendesu.item.ModItems;
import net.pendesu.item.custom.BookBundleItem;
import net.pendesu.networking.ScrollBookBundlePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibrariesLegends implements ModInitializer {
	public static final String MOD_ID = "librarieslegends";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
			ModItems.registerModItems();

		PayloadTypeRegistry.playC2S().register(
				ScrollBookBundlePayload.ID,
				ScrollBookBundlePayload.CODEC
		);

		ServerPlayNetworking.registerGlobalReceiver(
				ScrollBookBundlePayload.ID,
				(payload, context) -> {
					context.server().execute(() -> {
						int slotId = payload.slotId();

						if (slotId < 0 || slotId >= context.player().currentScreenHandler.slots.size()) {
							return;
						}

						Slot slot = context.player().currentScreenHandler.getSlot(slotId);
						ItemStack stack = slot.getStack();

						if (!stack.isOf(ModItems.BOOK_BUNDLE)) {
							return;
						}

						int direction = MathHelper.clamp(payload.direction(), -1, 1);

						if (direction == 0) {
							return;
						}

						BookBundleItem.scrollSelectedStack(stack, direction);

						slot.markDirty();
						context.player().currentScreenHandler.sendContentUpdates();
					});
				}
		);




			// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
	}
}
