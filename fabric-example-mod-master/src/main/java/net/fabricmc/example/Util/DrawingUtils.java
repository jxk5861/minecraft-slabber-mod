package net.fabricmc.example.Util;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class DrawingUtils {

	public static void begin(boolean renderThroughWalls) {
		if (!renderThroughWalls) {
			RenderSystem.enableDepthTest();
		}
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.enableCull();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		GL11.glLineWidth(1);
		GL11.glBegin(GL11.GL_LINES);
	}

	public static void end() {
		GL11.glEnd();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	public static void renderBoundingBox(Camera camera, ClientWorld world, BlockPos pos, int color,
			ShapeContext shapeContext) {
		BlockState state = world.getBlockState(pos);
		VoxelShape collisionShape = state.getCollisionShape(world, pos, shapeContext);

		BlockPos spawn = pos.add(0, 1, 0);
		BlockState blockState = world.getBlockState(spawn);

		if (blockState.isSolidBlock(world, pos)) {
			return;
		}

		int red = (color >> 16) & 255;
		int green = (color >> 8) & 255;
		int blue = color & 255;
		RenderSystem.color4f(red / 255f, green / 255f, blue / 255f, 1f);

		Box box;
		if (collisionShape.isEmpty()) {
			box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + .5, pos.getZ() + 1);
			box = box.offset(camera.getPos().negate());
		} else {
			box = collisionShape.getBoundingBox().offset(pos).offset(camera.getPos().negate());
		}

//		GL11.glVertex3d(box.minX, box.minY, box.minZ);
//		GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
//		
//		GL11.glVertex3d(box.minX, box.minY, box.maxZ);
//		GL11.glVertex3d(box.maxX, box.minY, box.minZ);

		GL11.glVertex3d(box.minX, box.minY, box.minZ);
		GL11.glVertex3d(box.maxX, box.minY, box.minZ);
		GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
		GL11.glVertex3d(box.minX, box.minY, box.maxZ);
		GL11.glVertex3d(box.minX, box.minY, box.minZ);
		GL11.glVertex3d(box.minX, box.minY, box.maxZ);
		GL11.glVertex3d(box.maxX, box.minY, box.minZ);
		GL11.glVertex3d(box.maxX, box.minY, box.maxZ);

		GL11.glVertex3d(box.minX, box.minY, box.minZ);
		GL11.glVertex3d(box.minX, box.maxY, box.minZ);
		GL11.glVertex3d(box.maxX, box.minY, box.minZ);
		GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
		GL11.glVertex3d(box.minX, box.minY, box.maxZ);
		GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
		GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
		GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);

		GL11.glVertex3d(box.minX, box.maxY, box.minZ);
		GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
		GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
		GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
		GL11.glVertex3d(box.minX, box.maxY, box.minZ);
		GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
		GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
		GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
	}
}
