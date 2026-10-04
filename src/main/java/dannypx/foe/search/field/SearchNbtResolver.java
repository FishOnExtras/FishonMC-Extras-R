package dannypx.foe.search.field;

import dannypx.foe.item.TagObject;
import dannypx.foe.search.value.FilterValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public final class SearchNbtResolver {
    public static @Nullable FilterValue resolve(SearchContext ctx, String key) {
        List<String> values = Arrays.stream(key.split("\\.")).toList();
        return resolve(ctx.serverTag(), values);
    }

    public static @Nullable FilterValue resolve(TagObject itemTag, List<String> values) {
        if(itemTag != null && itemTag.contains(values.getFirst())) {
            Tag data = itemTag.get(values.getFirst());
            return switch (data.getId()) {
                case 1 -> FilterValue.bool(itemTag.getBoolean(values.getFirst()));
                case 2 -> FilterValue.number(itemTag.getShort(values.getFirst()));
                case 3 -> FilterValue.number(itemTag.getInt(values.getFirst()));
                case 4 -> FilterValue.number(itemTag.getLong(values.getFirst()));
                case 5 -> FilterValue.number(itemTag.getFloat(values.getFirst()));
                case 6 -> FilterValue.number(itemTag.getDouble(values.getFirst()));
                case 7 -> {
                    if(values.size() > 1) {
                        try {
                            int index = Integer.parseInt(values.get(1));
                            yield FilterValue.number(itemTag.getByteFromArray(values.getFirst(), index));
                        } catch (NumberFormatException e) {
                            yield null;
                        }
                    }
                    yield null;
                }
                case 8 -> FilterValue.string(itemTag.getString(values.getFirst()));
                case 9 -> {
                    if(values.size() > 2) {
                        try {
                            int index = Integer.parseInt(values.get(1));
                            yield resolve(TagObject.of(itemTag.getList(values.getFirst()).getCompound(index).orElse(new CompoundTag())),
                                    values.subList(2, values.size())
                            );
                        } catch (NumberFormatException e) {
                            yield null;
                        }
                    }
                    yield null;
                }
                case 10 -> resolve(TagObject.of(itemTag.getTag(values.getFirst())), values.subList(1, values.size()));
                case 11 -> {
                    if(values.size() > 1) {
                        try {
                            int index = Integer.parseInt(values.get(1));
                            yield FilterValue.number(itemTag.getIntFromArray(values.getFirst(), index));
                        } catch (NumberFormatException e) {
                            yield null;
                        }
                    }
                    yield null;
                }
                case 12 -> {
                    if(values.size() > 1) {
                        try {
                            int index = Integer.parseInt(values.get(1));
                            yield FilterValue.number(itemTag.getLongFromArray(values.getFirst(), index));
                        } catch (NumberFormatException e) {
                            yield null;
                        }
                    }
                    yield null;
                }
                default -> null;
            };
        }
        return null;
    }
}
