package net.fabricmc.example.edited;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface ClientPosArgument {
	Vec3d toAbsolutePos(FabricClientCommandSource source);

	Vec2f toAbsoluteRotation(FabricClientCommandSource source);

	default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
		return new BlockPos(this.toAbsolutePos(source));
	}

}
