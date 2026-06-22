package fun.vegax.features.impl.misc;

import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BooleanSetting;
import fun.vegax.features.module.setting.implement.SelectSetting;
import fun.vegax.features.module.setting.implement.SliderSettings;
import fun.vegax.features.module.setting.implement.TextSetting;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FakeMessage extends Module {

    // ── Ник игрока — меняешь прямо в кликгуи ────────────────────────────
    private final TextSetting nickname = new TextSetting("Ник", "Имя игрока")
            .setText("MrDomer");

    // ── Сообщение — меняешь прямо в кликгуи ─────────────────────────────
    private final TextSetting message = new TextSetting("Сообщение", "Текст")
            .setText("CodeDLC бустит я конченный еблан я поклоняюсь CodeDLC Покупайте CodeDLC");

    // ── Донат — выбираешь в списке в кликгуи ────────────────────────────
    private final SelectSetting donate = new SelectSetting("Донат", "Ранг")
            .value(
                    "Player", "Hero", "Titan", "Avenger", "Overlord",
                    "Magister", "Imperator", "Dragon", "Bull", "Rabbit",
                    "Tiger", "Dracula", "Bunny", "Hydra", "Cobra",
                    "Media", "YT", "D. Helper", "Helper",
                    "ML. Moder", "Moder", "Moder+", "ST. Moder", "GL. Moder",
                    "ML. Admin", "Admin", "Свой"
            )
            .selected("Player");

    // ── Свой символ — только если выбрано "Свой" ─────────────────────────
    private final TextSetting customDonate = new TextSetting("Свой символ", "Символ ранга")
            .setText("ꔨ")
            .visible(() -> donate.isSelected("Свой"));

    // ── Глобальный или локальный чат (цвет ▣) ────────────────────────────
    private final BooleanSetting globalChat = new BooleanSetting("Глобальный чат", "Золотой ▣ вместо голубого")
            .setValue(true);

    // ── RW+ значок после ника ────────────────────────────────────────────
    private final BooleanSetting rwPlus = new BooleanSetting("RW+", "Значок + после ника")
            .setValue(false);

    // ── Отступ между ▣ и символом доната ────────────────────────────────
    private final SliderSettings donateSpacing = new SliderSettings("Отступ", "Пробелы до символа доната")
            .range(0f, 4f)
            .setValue(1f);

    public FakeMessage() {
        super("FakeMessage", "FakeMessage", ModuleCategory.MISC);
    }

    @Override
    public void activate() {
        if (mc.player == null || mc.inGameHud == null) {
            setState(false);
            return;
        }

        String donateSymbol = switch (donate.getSelected()) {
            case "Player"    -> "ꔀ";
            case "Hero"      -> "ꔄ";
            case "Titan"     -> "ꔈ";
            case "Avenger"   -> "ꔒ";
            case "Overlord"  -> "ꔖ";
            case "Magister"  -> "ꔠ";
            case "Imperator" -> "ꔤ";
            case "Dragon"    -> "ꔨ";
            case "Bull"      -> "ꔲ";
            case "Rabbit"    -> "ꕒ";
            case "Tiger"     -> "ꔶ";
            case "Dracula"   -> "ꕄ";
            case "Bunny"     -> "ꕖ";
            case "Hydra"     -> "ꕀ";
            case "Cobra"     -> "ꕈ";
            case "Media"     -> "ꔁ";
            case "YT"        -> "ꔅ";
            case "D. Helper" -> "ꕠ";
            case "Helper"    -> "ꔉ";
            case "ML. Moder" -> "ꔓ";
            case "Moder"     -> "ꔗ";
            case "Moder+"    -> "ꔡ";
            case "ST. Moder" -> "ꔥ";
            case "GL. Moder" -> "ꔩ";
            case "ML. Admin" -> "ꔳ";
            case "Admin"     -> "ꔷ";
            case "Свой"      -> customDonate.getText();
            default          -> "ꔀ";
        };

        String channel = (globalChat.isValue() ? Formatting.GOLD : Formatting.DARK_AQUA) + "▣";
        String plus    = rwPlus.isValue() ? (Formatting.BOLD + "" + Formatting.GOLD + "+" + Formatting.RESET) : "";
        String spaces  = " ".repeat(Math.max(0, Math.round(donateSpacing.getValue())));

        String result = channel
                + spaces
                + Formatting.RESET + donateSymbol
                + " " + Formatting.GRAY + nickname.getText() + plus
                + " " + Formatting.DARK_GRAY + "» "
                + Formatting.RESET + message.getText();

        mc.inGameHud.getChatHud().addMessage(Text.literal(result));

        setState(false);
    }
}