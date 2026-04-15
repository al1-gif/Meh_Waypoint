package net.shuuphe.mehwaypoint.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shuuphe.mehwaypoint.MehWaypoint;
import net.shuuphe.mehwaypoint.registry.ModItems;
import net.shuuphe.mehwaypoint.screen.WaypointBlockScreenHandler;

@Environment(EnvType.CLIENT)
public class WaypointBlockScreen extends HandledScreen<WaypointBlockScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of(MehWaypoint.MOD_ID, "textures/gui/waypoint.png");
    static final Identifier BUTTON_DISABLED_TEXTURE = Identifier.ofVanilla("container/beacon/button_disabled");
    static final Identifier BUTTON_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/beacon/button_highlighted");
    static final Identifier BUTTON_TEXTURE = Identifier.ofVanilla("container/beacon/button");
    static final Identifier CONFIRM_TEXTURE = Identifier.ofVanilla("container/beacon/confirm");
    static final Identifier CANCEL_TEXTURE = Identifier.ofVanilla("container/beacon/cancel");

    public WaypointBlockScreen(WaypointBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 230;
        this.backgroundHeight = 219;
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(new WaypointButtonWidget(this.x + 164, this.y + 107, CONFIRM_TEXTURE, ScreenTexts.DONE) {
            @Override
            public void onPress(AbstractInput input) {
                if (handler.canUpgrade()) {
                    WaypointBlockScreen.this.client.player.closeHandledScreen();
                }
            }

            @Override
            public void tick() {
                this.active = handler.canUpgrade();
            }
        });

        this.addDrawableChild(new WaypointButtonWidget(this.x + 190, this.y + 107, CANCEL_TEXTURE, ScreenTexts.CANCEL) {
            @Override
            public void onPress(AbstractInput input) {
                WaypointBlockScreen.this.client.player.closeHandledScreen();
            }
        });
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
        context.drawItem(new ItemStack(Items.EMERALD), i + 41, j + 109);
        context.drawItem(new ItemStack(Items.GOLD_INGOT), i + 63, j + 109);
        context.drawItem(new ItemStack(Items.IRON_INGOT), i + 86, j + 109);
        context.drawItem(new ItemStack(ModItems.RUBY), i + 108, j + 109);

        int rubyX = i + 138;
        int rubyY = j + 109;
        if (mouseX >= rubyX && mouseX < rubyX + 16 && mouseY >= rubyY && mouseY < rubyY + 16) {
            context.drawTooltip(this.textRenderer, Text.literal("Ruby: It is found mostly in Buried treasure, shipwreak, villages and so on. Explore the world and find out."), mouseX, mouseY);
        }
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
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
            Identifier identifier;
            if (!this.active) {
                identifier = BUTTON_DISABLED_TEXTURE;
            } else if (this.isSelected()) {
                identifier = BUTTON_HIGHLIGHTED_TEXTURE;
            } else {
                identifier = BUTTON_TEXTURE;
            }

            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.width, this.height);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.icon, this.getX() + 2, this.getY() + 2, 18, 18);
        }

        public void tick() {}

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}