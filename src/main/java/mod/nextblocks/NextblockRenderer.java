package mod.nextblocks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

public class NextblockRenderer extends EntityRenderer<NextblockEntity, NextblockRenderState> {
    private EntityRendererProvider.Context ctx;

    public NextblockRenderer(EntityRendererProvider.Context context) {
        super(context);
        ctx = context;
    }

    @Override
    public NextblockRenderState createRenderState() {
        return new NextblockRenderState();
    }

    @Override
    public void extractRenderState(NextblockEntity entity, NextblockRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.block = entity.getBlock();
    }

    @Override
    public void render(NextblockRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(state, poseStack, bufferSource, packedLight);
        poseStack.pushPose();
        Matrix4f matrix = new Matrix4f();

        float rot = state.ageInTicks * 5 % 360 * (float) Math.PI / 180;
        matrix.rotateY(rot);
        poseStack.mulPose(matrix);

        poseStack.translate(-1, 0, -1);
        poseStack.scale(2, 2, 2);

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        blockRenderer.renderSingleBlock(
                NextblockEntity.blockFromId(state.block).defaultBlockState(),
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.v(true),
                ModelData.EMPTY,
                RenderType.translucent()
        );
        poseStack.popPose();
    }
}