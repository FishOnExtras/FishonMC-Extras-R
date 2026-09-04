package dannypx.foe.placeholder.evaluator;

import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public record PlaceholderResult(MutableComponent text, boolean[] success, List<String> errors) {}
