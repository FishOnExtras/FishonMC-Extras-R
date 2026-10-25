package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CatchingHandler;
import dannypx.foe.handler.logic.RippleHandler;
import dannypx.foe.type.tuple.Pair;

import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TitleHandler extends Handler {
    private static TitleHandler INSTANCE = new TitleHandler();

    public static TitleHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new TitleHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private MutableComponent title = Component.empty();
    private MutableComponent subTitle = Component.empty();

    public MutableComponent getTitle() {
        return title;
    }

    public MutableComponent getSubTitle() {
        return subTitle;
    }
    //endregion

    //region Methods
    public void setTitle(Component title) {
        this.title = title.copy();
        this.forwardTitleEvent(title);
    }

    public void setSubTitle(Component subTitle) {
        this.subTitle = subTitle.copy();
        this.forwardSubTitleEvent(subTitle);
    }

    private void forwardTitleEvent(Component title) {
        RippleHandler.instance().checkIfBite(title);
        CatchingHandler.instance().scanFishListener(title);
    }

    private void forwardSubTitleEvent(Component subTitle) {
        CatchingHandler.instance().scanFishNameListener(subTitle);
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "title", Pair.of(getTitle(), Component.empty()),
                "subTitle", Pair.of(getSubTitle(), Component.empty())
        );
    }
    //endregion
}
