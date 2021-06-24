package net.fabricmc.example;

import java.awt.Color;

import org.lwjgl.glfw.GLFW;

import com.mojang.brigadier.arguments.DoubleArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.Util.DrawingUtils;
import net.fabricmc.example.Util.SpawnUtil;
import net.fabricmc.example.edited.ClientBlockPosArgumentType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
	final static int CACHE_WIDTH = 512;
	final static int CACHE_HEIGHT = 256;

	// The result from the mob spawning computation is stored in an array since it
	// is very expensive.
	// The "center" of the cache is stored at 0, 0 initially. Once it fills, the
	// farthest blocks data are deleted and the center is shifted.
	int[][][] cache = new int[CACHE_WIDTH][CACHE_HEIGHT][CACHE_WIDTH];

	private boolean renderOverlay = false;
	private boolean renderThroughWalls = true;

	int overlayR1 = 50;
	int overlayR2 = 40;

	private double radius = 0;

	private BlockPos cacheCenter;
	private BlockPos circleCenter = null;

	private static KeyBinding overlayBinding;
	private static KeyBinding depthBinding;

	@Override
	public void onInitialize() {
		this.registerKeybinds();
		this.registerCommands();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {overlayR1 = 50;overlayR2 = 40;
			while (overlayBinding.wasPressed()) {
				// client.player.sendMessage(new LiteralText("\u00A7dToggled Overlay " +
				// (display ? "Off" : "On")), false);
				renderOverlay = !renderOverlay;
				for (int i = 0; i < CACHE_WIDTH; i++) {
					for (int j = 0; j < CACHE_HEIGHT; j++) {
						for (int k = 0; k < CACHE_WIDTH; k++) {
							cache[i][j][k] = 0;
						}
					}
				}
				cacheCenter = null;
			}
			while (depthBinding.wasPressed()) {
				renderThroughWalls = !renderThroughWalls;
			}
			
//			if(renderOverlay && !renderThroughWalls && false) {
//				MinecraftClient minecraft = MinecraftClient.getInstance();
//				ClientPlayerEntity player = minecraft.player;
//				ClientWorld world = minecraft.world;
//				///slabcircle -287 66 1365 134
//				if(world == null || player == null) {
//					return;
//				}
//				
//				BlockPos pos = player.getBlockPos();
//				
//				for(int i = -4; i <= 4; i++) {
//					for(int j = -4; j <= 4; j++) {
//						for(int k = -4; k <= 4; k++) {
//							BlockPos pos2 = pos.add(i, j, k);
//							if (circleCenter != null && !pos2.isWithinDistance(circleCenter, radius)) {
//								continue;
//							}
//							if(player.getMainHandStack() != null && player.getMainHandStack().getItem() == Items.STONE_SLAB) {
//								if (SpawnUtil.canSpawn(world, pos2)) {
//									Hand hand = Hand.MAIN_HAND;
//									BlockHitResult hitResult = new BlockHitResult(Vec3d.of(pos2), Direction.UP, pos2, false);
//									minecraft.interactionManager.interactBlock(player, world, hand, hitResult);
//									//player.networkHandler.sendPacket(new );
//								}
//							}
//						}
//					}
//				}
//			}
		});

		WorldRenderEvents.BEFORE_DEBUG_RENDER.register((context) -> {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			if (minecraft.world != null && minecraft.player != null && renderOverlay) {
				DrawingUtils.begin(renderThroughWalls);

				ClientPlayerEntity player = minecraft.player;
				BlockPos pos = player.getBlockPos();
				Camera camera = minecraft.gameRenderer.getCamera();
				ClientWorld world = minecraft.world;
				int color = 0xff00ff;
				ShapeContext scontext = ShapeContext.of(player);

				if (cacheCenter == null) {
					cacheCenter = pos;
				}

				if (cacheCenter.getSquaredDistance(pos) > CACHE_WIDTH * CACHE_WIDTH / 4) {
					for (int i = 0; i < CACHE_WIDTH; i++) {
						for (int j = 0; j < CACHE_HEIGHT; j++) {
							for (int k = 0; k < CACHE_WIDTH; k++) {
								cache[i][j][k] = 0;
							}
						}
					}
					cacheCenter = pos;
				}

				final BlockPos shift = pos.subtract(cacheCenter);
				int updates = 0;

//				for(var e : MinecraftClient.getInstance().world.getEntities()) {
//					if(!(e instanceof HostileEntity))continue;
//					if (circleCenter != null && !e.getBlockPos().isWithinDistance(circleCenter, radius+1)) {
//						continue;
//					}
//					DrawingUtils.renderBoundingBox(camera, world, e.getBlockPos(), Color.RED.getRGB(), scontext);
//				}
				
				for (int i = -overlayR1; i <= overlayR1; i++) {
					for (int j = -overlayR2; j <= overlayR2; j++) {
						for (int k = -overlayR1; k <= overlayR1; k++) {
							BlockPos pos2 = pos.add(i, j, k);
							if (!world.isChunkLoaded(pos2)) {
								continue;
							}
							if (circleCenter != null && !pos2.isWithinDistance(circleCenter, radius)) {
								continue;
							}

							final int x = Math.floorMod(i + shift.getX(), CACHE_WIDTH);
							final int y = Math.floorMod(j + shift.getY(), CACHE_HEIGHT);
							final int z = Math.floorMod(k + shift.getZ(), CACHE_WIDTH);

							if (updates < 10000 && (i * i + j * j + k * k < 7 * 7 || cache[x][y][z] == 0)) {
								if (SpawnUtil.canSpawn(world, pos2)) {
									cache[x][y][z] = 2;
									DrawingUtils.renderBoundingBox(camera, world, pos2, color, scontext);
								} else {
									cache[x][y][z] = 1;
								}
								updates++;
								continue;
							}

							if (cache[x][y][z] == 2) {
								DrawingUtils.renderBoundingBox(camera, world, pos2, color, scontext);
							}
						}
					}
				}

				DrawingUtils.end();
			}
		});
	}

	private void registerKeybinds() {
		overlayBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("Toggle Overlay", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "Mob Spawning Overlay"));

		depthBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Toggle Overlay Through Walls",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F12, "Mob Spawning Overlay"));
	}

	private void registerCommands() {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("slabcircle")
				.then(ClientCommandManager.argument("center", ClientBlockPosArgumentType.blockPos()).then(
						ClientCommandManager.argument("radius", DoubleArgumentType.doubleArg(0)).executes(context -> {
							circleCenter = ClientBlockPosArgumentType.getBlockPos(context, "center");
							radius = DoubleArgumentType.getDouble(context, "radius");

							if (radius == 0) {
								circleCenter = null;
								client.player.sendMessage(new LiteralText("\u00A7dSet Overlay Center to disabled."),
										false);
							} else {
								client.player.sendMessage(new LiteralText("\u00A7dSet Overlay Center to "
										+ circleCenter.toShortString() + " with radius " + radius + "."), false);
							}

							return 1;
						}))));
	}
}
