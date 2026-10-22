package dannypx.foe.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.screens.RodEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

public class RodCubeLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final Identifier CUBE_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "textures/misc/empty.png");

    private static final int PREVIEW_LIGHT_COORDS = 15728880;
    private static final double SAME_POSITION_TOLERANCE_SQ = 1.0E-6;

    private record Face(int axis, int sign, float shade) {}

    private static final Face[] FACES = {
            new Face(0, 1, 0.70f), new Face(0, -1, 0.70f),
            new Face(2, 1, 0.50f), new Face(2, -1, 0.50f),
            new Face(1, -1, 1.00f),
            new Face(1, 1, 0.30f),
    };
    private static final float[][] QUAD_CORNER_SIGNS = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};

    public RodCubeLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    public static <S extends LivingEntityRenderState, M extends EntityModel<? super S>> RenderLayer<S, M> create(RenderLayerParent<S, M> parent) {
        return new RodCubeLayer<>(parent);
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, @NonNull S renderState, float yRot, float xRot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof RodEditorScreen editorScreen)) return;

        Player player = minecraft.player;
        if (player == null) return;

        if (lightCoords != PREVIEW_LIGHT_COORDS) return;

        if (player.distanceToSqr(renderState.x, renderState.y, renderState.z) > SAME_POSITION_TOLERANCE_SQ) return;

        RodEditorScreen.CubeBounds cube = editorScreen.cubeInModelSpace(player);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(CUBE_TEXTURE), (pose, consumer) -> writeCube(pose, consumer, cube));
    }

    private static void writeCube(PoseStack.Pose pose, VertexConsumer consumer, RodEditorScreen.CubeBounds cube) {
        float[] center = {cube.centerX(), cube.centerY(), cube.centerZ()};
        float halfEdge = cube.halfEdge();

        for (Face face : FACES) {
            int color = 0xFF000000 | (Math.round(255 * face.shade()) << 16);
            int tangentAxis = (face.axis() + 1) % 3;
            int bitangentAxis = (face.axis() + 2) % 3;

            float[] normal = new float[3];
            normal[face.axis()] = face.sign();

            float[][] quad = new float[4][3];
            for (int corner = 0; corner < 4; corner++) {
                quad[corner][face.axis()] = center[face.axis()] + face.sign() * halfEdge;
                quad[corner][tangentAxis] = center[tangentAxis] + QUAD_CORNER_SIGNS[corner][0] * halfEdge;
                quad[corner][bitangentAxis] = center[bitangentAxis] + QUAD_CORNER_SIGNS[corner][1] * halfEdge;
            }

            for (int corner = 0; corner < 4; corner++) {
                addVertex(pose, consumer, quad[corner], normal, color);
            }
            for (int corner = 3; corner >= 0; corner--) {
                addVertex(pose, consumer, quad[corner], normal, color);
            }
        }
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, float[] position, float[] normal, int color) {
        consumer.addVertex(pose, position[0], position[1], position[2])
                .setColor(color)
                .setUv(0.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(PREVIEW_LIGHT_COORDS)
                .setNormal(pose, normal[0], normal[1], normal[2]);
    }
}
