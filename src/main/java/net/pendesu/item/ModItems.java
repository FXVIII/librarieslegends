package net.pendesu.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.pendesu.LibrariesLegends;
import net.pendesu.item.custom.BookBundleItem;

import java.util.function.Function;

public class ModItems {
    public static final Item BOOK_BUNDLE =
            registerItem("book_bundle", BookBundleItem::new);




    public static Item registerItem(String name, Function<Item.Settings, Item> factory) {
        Identifier id = Identifier.of(LibrariesLegends.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        Item item = factory.apply(new Item.Settings().registryKey(key));

        return Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModItems() {
        LibrariesLegends.LOGGER.info("Registering ModItems for" + LibrariesLegends.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(BOOK_BUNDLE);
        });
    }
}
