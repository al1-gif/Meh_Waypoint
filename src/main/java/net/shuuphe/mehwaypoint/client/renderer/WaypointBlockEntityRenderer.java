package net.shuuphe.mehwaypoint.client.renderer;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shuuphe.mehwaypoint.entity.WaypointBlockEntity;
import org.jetbrains.annotations.Nullable;

public class WaypointBlockEntityRenderer implements BlockEntityRenderer<WaypointBlockEntity, WaypointBlockEntityRenderState> {

    private static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png");
    private static final int   BEAM_HEIGHT = 1024;
    private static final float BEAM_RADIUS = 0.075f;

    public WaypointBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public WaypointBlockEntityRenderState createRenderState() {
        return new WaypointBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(WaypointBlockEntity blockEntity, WaypointBlockEntityRenderState state,
                                  float tickProgress, Vec3d cameraPos,
                                  @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        long time = blockEntity.getWorld() != null ? blockEntity.getWorld().getTime() : 0;
        state.beamScrollOffset = -(Math.floorMod(time, 40) + tickProgress) / 40.0f;
    }

    @Override
    public void render(WaypointBlockEntityRenderState state, MatrixStack matrices,
                       OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {

        matrices.push();
        matrices.translate(0.5, 1.0, 0.5);
        float scroll = state.beamScrollOffset;
        queue.submitCustom(matrices, RenderLayer.getBeaconBeam(BEAM_TEXTURE, true),
                (entry, consumer) -> renderBeam(entry, consumer, scroll,
                        BEAM_HEIGHT, BEAM_RADIUS, 30, 100, 255, 200));
        matrices.pop();
    }

    private static void renderBeam(MatrixStack.Entry entry, VertexConsumer consumer,
                                   float scrollOffset, float height, float radius,
                                   int r, int g, int b, int a) {
        float vMin = scrollOffset;
        float vMax = scrollOffset + height / 10.0f;

        addBeamVertex(entry, consumer, -radius, 0,      -radius, 0, vMin, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, 0,      -radius, 1, vMin, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, height, -radius, 1, vMax, r, g, b, a);
        addBeamVertex(entry, consumer, -radius, height, -radius, 0, vMax, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, 0,       radius, 0, vMin, r, g, b, a);
        addBeamVertex(entry, consumer, -radius, 0,       radius, 1, vMin, r, g, b, a);
        addBeamVertex(entry, consumer, -radius, height,  radius, 1, vMax, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, height,  radius, 0, vMax, r, g, b, a);
        addBeamVertex(entry, consumer, -radius, 0,       radius, 0, vMin, r, g, b, a);
        addBeamVertex(entry, consumer, -radius, 0,      -radius, 1, vMin, r, g, b, a);
        addBeamVertex(entry, consumer, -radius, height, -radius, 1, vMax, r, g, b, a);
        addBeamVertex(entry, consumer, -radius, height,  radius, 0, vMax, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, 0,      -radius, 0, vMin, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, 0,       radius, 1, vMin, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, height,  radius, 1, vMax, r, g, b, a);
        addBeamVertex(entry, consumer,  radius, height, -radius, 0, vMax, r, g, b, a);
    }

    private static void addBeamVertex(MatrixStack.Entry entry, VertexConsumer consumer,
                                      float x, float y, float z, float u, float v,
                                      int r, int g, int b, int a) {
        consumer.vertex(entry, x, y, z)
                .color(r, g, b, a)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(entry, 0f, 1f, 0f);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }
}