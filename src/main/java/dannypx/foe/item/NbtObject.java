package dannypx.foe.item;

import com.mojang.serialization.DataResult;
import dannypx.foe.helper.ItemStackHelper;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.helper.TextHelper;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NbtObject {

    protected final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    public static final String ID = "id";
    public static final String CATCHER = "catcher";
    public static final String UUID = "uuid";
    public static final String COUNTER = "counter";
    public static final String TYPE = "type";
    public static final String RARITY = "rarity";
    public static final String RENDER_INFO = "renderInfo";
    public static final String MONEY = "money";

    public static final int RARITY_LINE = 1;
    public static final int RARITY_SIBLING = 1;

    //From the bottom
    public static final int SHOP_PRICE_LINE = 5;

    public static final int BORDER_LINE = 2;

    protected final NbtCompound nbtCompound;
    protected final ItemStack itemStack;

    protected NbtObject() {
        this.nbtCompound = new NbtCompound();
        this.itemStack = ItemStack.EMPTY;
    }

    protected NbtObject(@NotNull NbtCompound nbtCompound, ItemStack itemStack) {
        this.nbtCompound = nbtCompound;
        this.itemStack = itemStack.copy();
    }

    //region Generics
    public boolean contains(String key) {
        return this.nbtCompound.contains(key);
    }

    public int getInt(String key) {
        return this.nbtCompound.getInt(key);
    }

    public float getFloat(String key) {
        return this.nbtCompound.getFloat(key);
    }

    public String getString(String key) {
        return this.nbtCompound.getString(key);
    }

    public boolean getBoolean(String key) {
        return this.nbtCompound.getBoolean(key);
    }

    public UUID getUuid(String key) {
        return this.nbtCompound.getUuid(key);
    }

    public NbtElement get(String key) {
        return this.nbtCompound.get(key);
    }

    public byte getType(String key) {
        return this.nbtCompound.getType(key);
    }
    //endregion

    public UUID getID() {
        return this.getUuid(ID);
    }

    public UUID getPlayerUUID() {
        if(this.contains(CATCHER)) {
            return this.getUuid(CATCHER);
        } else if (this.contains(UUID)) {
            return this.getUuid(UUID);
        }
        return null;
    }

    //region Generic
    public Text getName() {
        Text name = this.itemStack.getCustomName();
        return name != null ? name : this.itemStack.getName();
    }

    public int getCount() {
        if(this.contains(COUNTER)) {
            return this.getInt(COUNTER);
        }
        return this.itemStack.getCount();
    }

    public boolean isOwn() {
        if(minecraftClient.player != null && getPlayerUUID() != null) {
            return minecraftClient.player.getUuid().equals(getPlayerUUID());
        }
        return false;
    }

    public @NotNull String getType() {
        if(this.contains(TYPE)) {
            return this.getString(TYPE);
        } else if (this.contains(FishNbtObject.FISH)) {
            return "fish";
        }
        return "";
    }

    public @NotNull String getRarity() {
        if(this.contains(RARITY)) {
            return this.getString(RARITY);
        }
        return "";
    }

    public Text getRarityText() {
        if(this.itemStack.get(DataComponentTypes.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            try {
                List<Text> textList = this.getLore();
                Text rarity = textList.get(RARITY_LINE).getSiblings().get(RARITY_SIBLING);
                return TextHelper.trim(rarity);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Text.empty();
            }
        }
        return Text.empty();
    }

    public Text getBorderText() {
        if(this.itemStack.get(DataComponentTypes.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            List<Text> textList = this.getLore();
            if(!textList.isEmpty()) {
                Text borderLine = textList.get(BORDER_LINE);
                if (!borderLine.getSiblings().isEmpty()) return borderLine.getSiblings().getFirst();
            }
        }
        return Text.empty();
    }

    public static Text getBorderText(ItemStack itemStack) {
        if(itemStack.get(DataComponentTypes.LORE) != null) {
            List<Text> textList = itemStack.get(DataComponentTypes.LORE).lines();
            if(!textList.isEmpty()) {
                Text borderLine = textList.get(BORDER_LINE);
                if (!borderLine.getSiblings().isEmpty()) return borderLine.getSiblings().getFirst();
            }
        }
        return Text.empty();
    }

    public List<Text> getLore() {
        if(this.itemStack.get(DataComponentTypes.LORE) != null) {
            return Objects.requireNonNull(this.itemStack.get(DataComponentTypes.LORE)).lines();
        }
        return List.of();
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    protected List<NbtObject> getItemStackList(String key) {
        if(this.contains(key)) {
            DataResult<List<ItemStack>> result =
                    ItemStack.CODEC.listOf().parse(NbtOps.INSTANCE, this.get(key));
            List<ItemStack> itemStackList = result.result().orElse(List.of());

            return itemStackList.stream().map(item -> {
                Pair<Boolean, NbtObject> validatedItem = ValidateItem.isType(item);
                return validatedItem.value2();
            }).filter(Objects::nonNull).toList();
        }
        return List.of();
    }

    public NbtList getRenderInfo() {
        if(this.contains(RENDER_INFO)) {
            return (NbtList) this.get(RENDER_INFO);
        }
        return new NbtList();
    }

    public float getMoney() {
        NbtList renderInfo = this.getRenderInfo();
        if(!renderInfo.isEmpty()) {
            if(((NbtCompound) renderInfo.getFirst()).contains(MONEY)) {
                return ((NbtCompound) renderInfo.getFirst()).getFloat(MONEY);
            }
        }
        return 0f;
    }

    protected boolean isAuctionItem() {
        NbtList nbtList = this.getRenderInfo();

        if(!nbtList.isEmpty()) {
            return ((NbtCompound) nbtList.getFirst()).contains(MONEY);
        }
        return false;
    }
    //endregion

    public static NbtObject of(@NotNull NbtCompound nbtCompound, @NotNull ItemStack itemStack) {
        return new NbtObject(nbtCompound, itemStack);
    }

    public static NbtObject empty() {
        return new NbtObject(ItemStackHelper.getNbt(ItemStack.EMPTY), ItemStack.EMPTY);
    }
}
