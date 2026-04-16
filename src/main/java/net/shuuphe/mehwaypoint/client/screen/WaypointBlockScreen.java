package net.shuuphe.mehwaypoint.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.shuuphe.mehwaypoint.MehWaypoint;
import net.shuuphe.mehwaypoint.network.WaypointUpgradePayload;
import net.shuuphe.mehwaypoint.registry.ModItems;
import net.shuuphe.mehwaypoint.screen.WaypointBlockScreenHandler;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class WaypointBlockScreen extends HandledScreen<WaypointBlockScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of(MehWaypoint.MOD_ID, "textures/gui/waypoint.png");

    static final Identifier BUTTON_DISABLED_TEXTURE = Identifier.ofVanilla("container/beacon/button_disabled");
    static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/beacon/button_highlighted");
    static final Identifier BUTTON_TEXTURE = Identifier.ofVanilla("container/beacon/button");
    static final Identifier CONFIRM_TEXTURE = Identifier.ofVanilla("container/beacon/confirm");
    static final Identifier CANCEL_TEXTURE = Identifier.ofVanilla("container/beacon/cancel");

    private static final Identifier REGEN_SPRITE = Identifier.ofVanilla("mob_effect/regeneration");
    private static final Identifier JUMP_SPRITE = Identifier.ofVanilla("mob_effect/jump_boost");
    private static final Identifier SPEED_SPRITE = Identifier.ofVanilla("mob_effect/speed");
    private static final Identifier STRENGTH_SPRITE = Identifier.ofVanilla("mob_effect/strength");

    private WaypointButtonWidget confirmButton;

    public WaypointBlockScreen(WaypointBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 230;
        this.backgroundHeight = 219;
        this.playerInventoryTitleY = 125;
        this.playerInventoryTitleX = 8;
    }

    @Override
    protected void init() {
        super.init();
        confirmButton = new WaypointButtonWidget(this.x + 164, this.y + 107, CONFIRM_TEXTURE, ScreenTexts.DONE) {
            @Override
            public void onPress(AbstractInput input) {
                if (handler.canConfirm()) {
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new WaypointUpgradePayload());
                }
            }
        };
        confirmButton.active = handler.canConfirm();
        this.addDrawableChild(confirmButton);
        this.addDrawableChild(new WaypointButtonWidget(this.x + 190, this.y + 107, CANCEL_TEXTURE, ScreenTexts.CANCEL) {
            @Override
            public void onPress(AbstractInput input) {
                WaypointBlockScreen.this.client.player.closeHandledScreen();
            }
        });
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (confirmButton != null) {
            confirmButton.active = handler.canConfirm();
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int level = handler.getWaypointLevel();
        context.drawText(this.textRenderer, Text.literal("Waypoint Powers").formatted(Formatting.WHITE, Formatting.BOLD), 12, 9, 0xFFFFFFFF, true);

        drawLevelRow(context, 12, 26, 1, "Regeneration", REGEN_SPRITE, level);
        drawLevelRow(context, 12, 49, 2, "Regeneration II", REGEN_SPRITE, level);
        drawLevelRow(context, 12, 72, 3, "Range: 60 blks", null, level);
        drawLevelRow(context, 122, 26, 4, "Jump Boost", JUMP_SPRITE, level);
        drawLevelRow(context, 122, 49, 5, "Speed", SPEED_SPRITE, level);
        drawLevelRow(context, 122, 72, 6, "Strength/range++", STRENGTH_SPRITE, level);

        if (level < 6) {
            int cost = WaypointBlockScreenHandler.getRequiredRubyCount(level);
            context.drawText(this.textRenderer, Text.literal("Next Lv-" + (level + 1) + ": " + cost + " Rubies").formatted(Formatting.LIGHT_PURPLE), 120, 9, 0xFFFFFFFF, false);
        } else {
            context.drawText(this.textRenderer, Text.literal("MAX LEVEL <3").formatted(Formatting.GOLD, Formatting.BOLD), 120, 9, 0xFFFFFFFF, false);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int i = this.x;
        int j = this.y;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0f, 0f, this.backgroundWidth, this.backgroundHeight, 256, 256);

        context.drawItem(new ItemStack(Items.EMERALD), i + 41, j + 109);
        context.drawItem(new ItemStack(Items.GOLD_INGOT), i + 63, j + 109);
        context.drawItem(new ItemStack(Items.IRON_INGOT), i + 86, j + 109);
        context.drawItem(new ItemStack(ModItems.RUBY), i + 108, j + 109);
    }

    private void drawLevelRow(DrawContext context, int x, int y, int rowLevel, String effectDesc, Identifier effectSprite, int currentLevel) {
        boolean isCurrent = (rowLevel == currentLevel);
        boolean isUnlocked = (rowLevel <= currentLevel);

        int squareColor = isUnlocked ? 0xFFFFFFFF : 0xFF404040;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, x - 1, y - 1, 18, 18, squareColor);

        if (effectSprite != null) {
            int spriteColor = isUnlocked ? 0xFFFFFFFF : 0x66FFFFFF;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, effectSprite, x + 1, y + 1, 14, 14, spriteColor);
        } else {
            context.drawItem(new ItemStack(Items.COMPASS), x + 1, y + 1);
            if (!isUnlocked) {
                context.fill(x + 1, y + 1, x + 15, y + 15, 0x99202020);
            }
        }

        int lvColor = isCurrent ? 0xFFFFD700 : (isUnlocked ? 0xFFAAAAAA : 0xFF555555);
        int descColor = isCurrent ? 0xFFFFFFFF : (isUnlocked ? 0xFF888888 : 0xFF444444);

        float scale = 0.80f;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x + 20, y + 1);
        context.getMatrices().scale(scale, scale);
        context.drawText(this.textRenderer, "Lv-" + rowLevel, 0, 0, lvColor, false);
        context.drawText(this.textRenderer, effectDesc, 0, 10, descColor, false);
        context.getMatrices().popMatrix();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int i = this.x;
        int j = this.y;

        int[][] activationIcons = {{i + 41, j + 109}, {i + 63, j + 109}, {i + 86, j + 109}};
        String[] activationTips = {
                "Emerald – activate waypoint effects",
                "Gold Ingot – activate waypoint effects",
                "Iron Ingot – activate waypoint effects"
        };

        for (int k = 0; k < activationIcons.length; k++) {
            if (mouseX >= activationIcons[k][0] && mouseX < activationIcons[k][0] + 16 && mouseY >= activationIcons[k][1] && mouseY < activationIcons[k][1] + 16) {
                context.drawTooltip(this.textRenderer, Text.literal(activationTips[k]), mouseX, mouseY);
            }
        }

        if (mouseX >= i + 108 && mouseX < i + 124 && mouseY >= j + 109 && mouseY < j + 125) {
            List<Text> rubyTips = new ArrayList<>();
            rubyTips.add(Text.literal("Ruby – upgrade waypoint level").formatted(Formatting.RED));
            rubyTips.add(Text.literal("Found in: buried treasure, shipwrecks,").formatted(Formatting.GRAY));
            rubyTips.add(Text.literal("villages, ancient city & more.").formatted(Formatting.GRAY));
            rubyTips.add(Text.literal("Explore more places ot find them.").formatted(Formatting.GRAY));
            context.drawTooltip(this.textRenderer, rubyTips, mouseX, mouseY);
        }

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Environment(EnvType.CLIENT)
    abstract static class WaypointButtonWidget extends PressableWidget {
        private final Identifier icon;
        protected WaypointButtonWidget(int x, int y, Identifier icon, Text message) {
            super(x, y, 22, 22, message);
            this.icon = icon;
        }
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            Identifier bg = !this.active ? BUTTON_DISABLED_TEXTURE : (this.isHovered() ? BUTTON_HIGHLIGHTED_TEXTURE : BUTTON_TEXTURE);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, bg, this.getX(), this.getY(), this.width, this.height);
            int color = this.active ? 0xFFFFFFFF : 0x88FFFFFF;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.icon, this.getX() + 2, this.getY() + 2, 18, 18, color);
        }
        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}