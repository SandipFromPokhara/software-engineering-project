package util;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public final class TooltipUtil {

    private static final double DEFAULT_TOOLTIP_DELAY_MS = 100.0;

    private TooltipUtil() { /* utility */ }

    public static Tooltip createLocalizedTooltip(String key) {
        Tooltip t = new Tooltip(Localization.get(key));
        setTooltipDelay(t);
        return t;
    }

    public static Tooltip createTooltip(String text) {
        Tooltip t = new Tooltip(text);
        setTooltipDelay(t);
        return t;
    }

    public static void setTooltipDelay(Tooltip tooltip) {
        if (tooltip == null) return;
        tooltip.setShowDelay(Duration.millis(DEFAULT_TOOLTIP_DELAY_MS));
    }
}
