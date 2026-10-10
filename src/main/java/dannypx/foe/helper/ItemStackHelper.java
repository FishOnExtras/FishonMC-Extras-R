package dannypx.foe.helper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.function.UnaryOperator;

import dannypx.foe.handler.logic.LoggerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ItemStackHelper {
    private static final GsonBuilder gson = new GsonBuilder();

    public static CompoundTag getTag(ItemStack stack) {
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        return component != null ? component.copyTag() : new CompoundTag();
    }

    public static ItemStack jsonToItemStack(String json) {
        return ItemStack.CODEC
                .decode(RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().player.registryAccess()), gson.create().fromJson(json, JsonElement.class))
                .mapOrElse((Pair::getFirst), (pairError -> ItemStack.EMPTY));
    }

    public static String itemStackToJson(ItemStack itemStack) {
        if(!itemStack.isEmpty()) {
            return gson.setPrettyPrinting().create().toJson(ItemStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().player.registryAccess()), itemStack).getOrThrow());
        }
        return "{}";
    }

    public static String itemStackListToJson(List<ItemStack> itemStacks) {
        List<ItemStack> stacksToSerialize = itemStacks.stream().filter(stack -> !stack.isEmpty()).toList();
        if (stacksToSerialize.isEmpty()) return "[]";
        return gson.setPrettyPrinting().create().toJson(ItemStack.CODEC.listOf().encodeStart(RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().player.registryAccess()), stacksToSerialize).getOrThrow());
    }

    public static <T> NonNullList<T> deepCopy(
            NonNullList<T> original,
            T defaultValue,
            UnaryOperator<T> copier
    ) {
        NonNullList<T> copy =
                NonNullList.withSize(original.size(), defaultValue);

        for (int i = 0; i < original.size(); i++) {
            copy.set(i, copier.apply(original.get(i)));
        }
        return copy;
    }

    public static ItemStack valueOf(String value) {
        if(Minecraft.getInstance().player != null) {
            HolderLookup.Provider lookup = Minecraft.getInstance().player.registryAccess();
            try {
                ItemParser itemParser = new ItemParser(lookup);
                StringReader stringReader = new StringReader(value);
                ItemParser.ItemResult result = itemParser.parse(stringReader);

                ItemStack itemStack = new ItemStack(result.item(), 1);
                itemStack.applyComponents(result.components());

                return itemStack;
            } catch (Exception e) {
                LoggerHandler._debug(e.getMessage());
            }
        }
        return ItemStack.EMPTY;
    }
}
