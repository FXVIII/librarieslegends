package net.pendesu.item.custom;


import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.pendesu.LibrariesLegends;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.tooltip.TooltipData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Optional;
import java.util.function.Consumer;

public class BookBundleItem extends BundleItem {

    private static final int MAX_BOOK_BUNDLE_ITEMS = 12;

    private static final int ITEM_BAR_COLOR =
            ColorHelper.fromFloats(1.0F, 0.44F, 0.53F, 1.0F);

    public static final TagKey<Item> BOOK_BUNDLE_ALLOWED =
            TagKey.of(
                    RegistryKeys.ITEM,
                    Identifier.of(LibrariesLegends.MOD_ID, "book_bundle_allowed")
            );

    public BookBundleItem(Item.Settings settings) {
        super(settings
                .maxCount(1)
                .component(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT));
    }

    private static boolean isAllowedInBookBundle(ItemStack stack) {
        return !stack.isEmpty()
                && stack.isIn(BOOK_BUNDLE_ALLOWED)
                && BundleContentsComponent.canBeBundled(stack);
    }

    private static BundleContentsComponent getContents(ItemStack bundleStack) {
        return bundleStack.getOrDefault(
                DataComponentTypes.BUNDLE_CONTENTS,
                BundleContentsComponent.DEFAULT
        );
    }

    private static List<ItemStack> copyContents(BundleContentsComponent contents) {
        List<ItemStack> stacks = new ArrayList<>();

        for (ItemStack storedStack : contents.iterateCopy()) {
            stacks.add(storedStack);
        }

        return stacks;
    }

    private static Text getBookBundleDisplayName(ItemStack stack) {
        ItemEnchantmentsComponent storedEnchantments = stack.getOrDefault(
                DataComponentTypes.STORED_ENCHANTMENTS,
                ItemEnchantmentsComponent.DEFAULT
        );

        ItemEnchantmentsComponent normalEnchantments = stack.getOrDefault(
                DataComponentTypes.ENCHANTMENTS,
                ItemEnchantmentsComponent.DEFAULT
        );

        ItemEnchantmentsComponent enchantments = !storedEnchantments.isEmpty()
                ? storedEnchantments
                : normalEnchantments;

        if (enchantments.isEmpty()) {
            return stack.getName();
        }

        Text firstEnchantName = null;
        int extraEnchantCount = 0;

        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
            Text enchantmentName = Enchantment.getName(entry.getKey(), entry.getIntValue());

            if (firstEnchantName == null) {
                firstEnchantName = enchantmentName;
            } else {
                extraEnchantCount++;
            }
        }

        if (firstEnchantName == null) {
            return stack.getName();
        }

        if (extraEnchantCount > 0) {
            return Text.empty()
                    .append(firstEnchantName)
                    .append(Text.literal(" + " + extraEnchantCount));
        }

        return firstEnchantName;
    }

    private static BundleContentsComponent createTooltipContents(BundleContentsComponent contents) {
        List<ItemStack> displayStacks = new ArrayList<>();

        for (ItemStack storedStack : contents.iterateCopy()) {
            ItemStack displayStack = storedStack.copy();

            Text displayName = getBookBundleDisplayName(displayStack);

            if (!displayName.equals(displayStack.getName())) {
                displayStack.set(DataComponentTypes.CUSTOM_NAME, displayName);
            }

            displayStacks.add(displayStack);
        }

        BundleContentsComponent tooltipContents = new BundleContentsComponent(displayStacks);

        if (contents.hasSelectedStack()) {
            BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(tooltipContents);
            builder.setSelectedStackIndex(contents.getSelectedStackIndex());
            return builder.build();
        }

        return tooltipContents;
    }

    private static int getStoredItemCount(List<ItemStack> stacks) {
        int count = 0;

        for (ItemStack stack : stacks) {
            count += stack.getCount();
        }

        return count;
    }

    private static int getStoredItemCount(BundleContentsComponent contents) {
        int count = 0;

        for (ItemStack storedStack : contents.iterate()) {
            count += storedStack.getCount();
        }

        return count;
    }

    private static int getStoredItemCount(ItemStack bundleStack) {
        return getStoredItemCount(getContents(bundleStack));
    }

    private static boolean hasRoom(ItemStack bundleStack) {
        return getStoredItemCount(bundleStack) < MAX_BOOK_BUNDLE_ITEMS;
    }

    private static void setContents(ItemStack bundleStack, List<ItemStack> stacks) {
        bundleStack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(stacks));
    }

    private static void addSingleStackDirectly(ItemStack bundleStack, ItemStack stackToAdd) {
        BundleContentsComponent contents = getContents(bundleStack);
        List<ItemStack> stacks = copyContents(contents);

        ItemStack storedCopy = stackToAdd.copy();
        storedCopy.setCount(1);

        // Add to the front so the newest item comes out first.
        stacks.add(0, storedCopy);

        setContents(bundleStack, stacks);
    }

    public static void scrollSelectedStack(ItemStack bundleStack, int direction) {
        BundleContentsComponent contents = getContents(bundleStack);

        if (contents.isEmpty()) {
            return;
        }

        int size = contents.size();

        int currentIndex = BundleItem.hasSelectedStack(bundleStack)
                ? BundleItem.getSelectedStackIndex(bundleStack)
                : 0;

        int newIndex = currentIndex + direction;

        if (newIndex < 0) {
            newIndex = size - 1;
        }

        if (newIndex >= size) {
            newIndex = 0;
        }

        BundleItem.setSelectedStackIndex(bundleStack, newIndex);
    }

    private static boolean tryAddOneItem(ItemStack bundleStack, ItemStack sourceStack) {
        if (!isAllowedInBookBundle(sourceStack)) {
            return false;
        }

        if (!hasRoom(bundleStack)) {
            return false;
        }

        ItemStack insertedStack = sourceStack.split(1);

        if (insertedStack.isEmpty()) {
            return false;
        }

        addSingleStackDirectly(bundleStack, insertedStack);
        return true;
    }

    private static ItemStack removeOneItem(ItemStack bundleStack) {
        BundleContentsComponent contents = getContents(bundleStack);
        List<ItemStack> stacks = copyContents(contents);

        if (stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int index = contents.hasSelectedStack()
                ? contents.getSelectedStackIndex()
                : 0;

        if (index < 0 || index >= stacks.size()) {
            index = 0;
        }

        ItemStack storedStack = stacks.get(index);
        ItemStack removedStack = storedStack.split(1);

        if (storedStack.isEmpty()) {
            stacks.remove(index);
        }

        setContents(bundleStack, stacks);
        return removedStack;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        BundleContentsComponent contents = getContents(stack);

        if (contents.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new BundleTooltipData(createTooltipContents(contents)));
    }

    @Override
    public boolean onStackClicked(ItemStack bundleStack, Slot slot, ClickType clickType, PlayerEntity player) {
        ItemStack slotStack = slot.getStack();

        // Left click a slot with the bundle: insert 1 allowed item from the slot.
        if (clickType == ClickType.LEFT && !slotStack.isEmpty()) {
            if (!slot.canTakePartial(player)) {
                playInsertFailSound(player);
                return true;
            }

            if (tryAddOneItem(bundleStack, slotStack)) {
                slot.markDirty();
                playInsertSound(player);
            } else {
                playInsertFailSound(player);
            }

            return true;
        }

        // Right click an empty slot with the bundle: remove 1 item into that slot.
        if (clickType == ClickType.RIGHT && slotStack.isEmpty()) {
            ItemStack removedStack = removeOneItem(bundleStack);

            if (!removedStack.isEmpty()) {
                ItemStack remainder = slot.insertStack(removedStack);

                if (!remainder.isEmpty()) {
                    addSingleStackDirectly(bundleStack, remainder);
                } else {
                    playRemoveOneSound(player);
                }

                slot.markDirty();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onClicked(
            ItemStack bundleStack,
            ItemStack cursorStack,
            Slot slot,
            ClickType clickType,
            PlayerEntity player,
            StackReference cursorStackReference
    ) {
        // Left click bundle with empty cursor: remove the selected internal item.
        if (clickType == ClickType.LEFT && cursorStack.isEmpty()) {
            if (!slot.canTakePartial(player)) {
                return true;
            }

            BundleContentsComponent contents = getContents(bundleStack);

            if (!contents.isEmpty() && contents.hasSelectedStack()) {
                ItemStack removedStack = removeOneItem(bundleStack);

                if (!removedStack.isEmpty()) {
                    cursorStackReference.set(removedStack);
                    playRemoveOneSound(player);
                }

                return true;
            }

            // No selected item, allow normal pickup behavior.
            return false;
        }

        // Left click bundle with item on cursor: insert 1 allowed item from cursor.
        if (clickType == ClickType.LEFT && !cursorStack.isEmpty()) {
            if (!slot.canTakePartial(player)) {
                playInsertFailSound(player);
                return true;
            }

            if (tryAddOneItem(bundleStack, cursorStack)) {
                cursorStackReference.set(cursorStack.isEmpty() ? ItemStack.EMPTY : cursorStack);
                playInsertSound(player);
            } else {
                playInsertFailSound(player);
            }

            return true;
        }

        // Right click bundle with empty cursor: remove 1 item to cursor.
        if (clickType == ClickType.RIGHT && cursorStack.isEmpty()) {
            if (!slot.canTakePartial(player)) {
                return true;
            }

            ItemStack removedStack = removeOneItem(bundleStack);

            if (!removedStack.isEmpty()) {
                cursorStackReference.set(removedStack);
                playRemoveOneSound(player);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getStoredItemCount(stack) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int stored = getStoredItemCount(stack);

        return Math.min(
                13,
                Math.round(13.0F * stored / MAX_BOOK_BUNDLE_ITEMS)
        );
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }



    private static void playInsertSound(PlayerEntity player) {
        player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F);
    }

    private static void playInsertFailSound(PlayerEntity player) {
        player.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

    private static void playRemoveOneSound(PlayerEntity player) {
        player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F);
    }
}