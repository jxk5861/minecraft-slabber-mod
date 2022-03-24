package net.fabricmc.example.edited;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.command.CommandSource.RelativePosition;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class ClientBlockPosArgumentType
		implements ArgumentType<ClientPosArgument>, SuggestionProvider<FabricClientCommandSource> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5",
			"~0.5 ~1 ~-5");
	public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType(
			new TranslatableText("argument.pos.unloaded"));
	public static final SimpleCommandExceptionType OUT_OF_WORLD_EXCEPTION = new SimpleCommandExceptionType(
			new TranslatableText("argument.pos.outofworld"));

	public static ClientBlockPosArgumentType blockPos() {
		return new ClientBlockPosArgumentType();
	}

	public static BlockPos getLoadedBlockPos(CommandContext<FabricClientCommandSource> context, String name)
			throws CommandSyntaxException {
		BlockPos blockPos = context.getArgument(name, ClientPosArgument.class)
				.toAbsoluteBlockPos(context.getSource());
		if (!(context.getSource()).getWorld().isChunkLoaded(blockPos)) {
			throw UNLOADED_EXCEPTION.create();
		} else {
			if (!ClientWorld.isInBuildLimit(blockPos)) {
				throw OUT_OF_WORLD_EXCEPTION.create();
			} else {
				return blockPos;
			}
		}
	}

	public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name)
			throws CommandSyntaxException {
		return context.getArgument(name, ClientPosArgument.class).toAbsoluteBlockPos(context.getSource());
	}

	public ClientPosArgument parse(StringReader stringReader) throws CommandSyntaxException {
		return (ClientPosArgument) (stringReader.canRead() && stringReader.peek() == '^'
				? ClientLookingPosArgument.parse(stringReader)
				: ClientDefaultPosArgument.parse(stringReader, true));
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof CommandSource)) {
			return Suggestions.empty();
		} else {
			String string = builder.getRemaining();
			Collection<RelativePosition> collection2;
			if (!string.isEmpty() && string.charAt(0) == '^') {
				collection2 = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
			} else {
				collection2 = ((CommandSource) context.getSource()).getBlockPositionSuggestions();
			}

			return CommandSource.suggestPositions(string, collection2, builder,
					CommandManager.getCommandValidator(this::parse));
		}
	}

	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		if (!(context.getSource() instanceof CommandSource)) {
			return Suggestions.empty();
		} else {
			String string = builder.getRemaining();
			Collection<RelativePosition> collection2;
			if (!string.isEmpty() && string.charAt(0) == '^') {
				collection2 = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
			} else {
				collection2 = context.getSource().getBlockPositionSuggestions();
			}

			return CommandSource.suggestPositions(string, collection2, builder,
					CommandManager.getCommandValidator(this::parse));
		}
	}

}
