package net.fabricmc.example.edited;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ClientDefaultPosArgument implements ClientPosArgument {
	private final CoordinateArgument x;
	private final CoordinateArgument y;
	private final CoordinateArgument z;

	public ClientDefaultPosArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public Vec3d toAbsolutePos(FabricClientCommandSource source) {
		Vec3d vec3d = source.getPlayer().getPos();
		return new Vec3d(this.x.toAbsoluteCoordinate(vec3d.x), this.y.toAbsoluteCoordinate(vec3d.y),
				this.z.toAbsoluteCoordinate(vec3d.z));

	}

	@Override
	public Vec2f toAbsoluteRotation(FabricClientCommandSource source) {
		Vec2f vec2f = source.getPlayer().getRotationClient();
		return new Vec2f((float) this.x.toAbsoluteCoordinate((double) vec2f.x),
				(float) this.y.toAbsoluteCoordinate((double) vec2f.y));

	}

	public static ClientDefaultPosArgument parse(StringReader reader, boolean centerIntegers)
			throws CommandSyntaxException {
		int i = reader.getCursor();
		CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader, centerIntegers);
		if (reader.canRead() && reader.peek() == ' ') {
			reader.skip();
			CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader, false);
			if (reader.canRead() && reader.peek() == ' ') {
				reader.skip();
				CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader, centerIntegers);
				return new ClientDefaultPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
			} else {
				reader.setCursor(i);
				throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
			}
		} else {
			reader.setCursor(i);
			throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
		}
	}

}
