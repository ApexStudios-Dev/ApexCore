package xyz.apex.forge.apexcore.revamp.block;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

import xyz.apex.forge.apexcore.lib.util.ContainerHelper;
import xyz.apex.forge.apexcore.lib.util.NameableMutable;
import xyz.apex.java.utility.nullness.NonnullConsumer;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public class BaseBlock extends Block implements SimpleWaterloggedBlock
{
	public static final DirectionProperty FACING_4_WAY = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BaseBlock(Properties properties)
	{
		super(properties);

		postBlockConstructor();
	}

	// region: Core
	protected void registerProperties(NonnullConsumer<Property<?>> consumer)
	{
	}

	private void postBlockConstructor()
	{
		var builder = new StateDefinition.Builder<Block, BlockState>(this);
		registerProperties(builder::add);
		var stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
		registerDefaultState(stateDefinition.any());
		ObfuscationReflectionHelper.setPrivateValue(Block.class, this, stateDefinition, "f_49792_");
	}
	// endregion

	// region: Overrides
	@Override
	public final BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		var blockState = defaultBlockState();

		var level = ctx.getLevel();
		var pos = ctx.getClickedPos();

		if(blockState.hasProperty(WATERLOGGED))
		{
			var fluidState = level.getFluidState(pos);
			var waterLogged = fluidState.is(FluidTags.WATER);
			blockState = blockState.setValue(WATERLOGGED, waterLogged);
		}

		if(blockState.hasProperty(FACING_4_WAY))
			blockState = blockState.setValue(FACING_4_WAY, getFourWayFacing(ctx));

		return blockState;
	}

	@Override
	public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingBlockState, LevelAccessor level, BlockPos pos, BlockPos facingPos)
	{
		if(blockState.hasProperty(WATERLOGGED) && blockState.getValue(WATERLOGGED))
			level.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(blockState, facing, facingBlockState, level, pos, facingPos);
	}

	@Override
	protected final void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
	}

	@Override
	public final BlockState rotate(BlockState blockState, Rotation rotation)
	{
		if(blockState.hasProperty(FACING_4_WAY))
			return blockState.setValue(FACING_4_WAY, rotation.rotate(blockState.getValue(FACING_4_WAY)));

		return blockState;
	}

	@Override
	public final BlockState mirror(BlockState blockState, Mirror mirror)
	{
		if(blockState.hasProperty(FACING_4_WAY))
			return blockState.rotate(mirror.getRotation(blockState.getValue(FACING_4_WAY)));

		return blockState;
	}
	// endregion

	// region: WaterLogging
	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter level, BlockPos pos)
	{
		if(blockState.hasProperty(WATERLOGGED))
			return !blockState.getValue(WATERLOGGED);
		return super.propagatesSkylightDown(blockState, level, pos);
	}

	@Override
	public final FluidState getFluidState(BlockState blockState)
	{
		return blockState.hasProperty(WATERLOGGED) && blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public final boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid)
	{
		return blockState.hasProperty(WATERLOGGED) && SimpleWaterloggedBlock.super.canPlaceLiquid(level, pos, blockState, fluid);
	}

	@Override
	public final boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState blockState, FluidState fluidState)
	{
		return blockState.hasProperty(WATERLOGGED) && SimpleWaterloggedBlock.super.placeLiquid(level, pos, blockState, fluidState);
	}

	@Override
	public final ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState blockState)
	{
		return !blockState.hasProperty(WATERLOGGED) ? ItemStack.EMPTY : SimpleWaterloggedBlock.super.pickupBlock(level, pos, blockState);
	}
	// endregion

	// region: FourWay
	protected Direction getFourWayFacing(BlockPlaceContext ctx)
	{
		return ctx.getHorizontalDirection().getOpposite();
	}
	// endregion

	public static abstract class WithBlockEntity<BLOCK_ENTITY extends BlockEntity> extends BaseBlock implements EntityBlock
	{
		public WithBlockEntity(Properties properties)
		{
			super(properties);
		}

		// region: Core
		protected abstract BlockEntityType<BLOCK_ENTITY> getBlockEntityType();

		@Nullable
		protected BlockEntityTicker<BLOCK_ENTITY> getBlockEntityTicker(boolean clientSide)
		{
			return null;
		}
		// endregion

		// region: Overrides
		@Nullable
		protected final BLOCK_ENTITY getBlockEntity(BlockGetter level, BlockPos pos)
		{
			return getBlockEntityType().getBlockEntity(level, pos);
		}

		@Override
		public final boolean triggerEvent(BlockState blockState, Level level, BlockPos pos, int event, int param)
		{
			var blockEntity = getBlockEntity(level, pos);
			return blockEntity != null && blockEntity.triggerEvent(event, param);
		}

		@Nullable
		@Override
		public final BlockEntity newBlockEntity(BlockPos pos, BlockState blockState)
		{
			return getBlockEntityType().create(pos, blockState);
		}

		@Nullable
		@Override
		public final <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType)
		{
			var blockEntityTicker = getBlockEntityTicker(level.isClientSide);
			return blockEntityTicker != null ? createTickerHelper(blockEntityType, getBlockEntityType(), blockEntityTicker) : null;
		}

		@Nullable
		@Override
		public final <T extends BlockEntity> GameEventListener getListener(Level level, T blockEntity)
		{
			return blockEntity instanceof GameEventListener gameEventListener ? gameEventListener : null;
		}
		// endregion

		@Nullable
		protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> otherBlockEntityType, BlockEntityTicker<? super E> blockEntityTicker)
		{
			return otherBlockEntityType == blockEntityType ? (BlockEntityTicker<A>) blockEntityTicker : null;
		}
	}

	public static abstract class WithContainer<BLOCK_ENTITY extends BlockEntity, CONTAINER extends AbstractContainerMenu> extends WithBlockEntity<BLOCK_ENTITY>
	{
		public WithContainer(Properties properties)
		{
			super(properties);
		}

		// region: Core
		protected abstract MenuType<CONTAINER> getContainerType();

		protected final InteractionResult tryOpenContainerScreen(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
		{
			var provider = getMenuProvider(blockState, level, pos);

			if(provider != null)
			{
				if(level.isClientSide)
					return InteractionResult.SUCCESS;

				if(player instanceof ServerPlayer serverPlayer)
				{
					NetworkHooks.openGui(serverPlayer, provider, buffer -> buffer.writeBlockPos(pos));
					return InteractionResult.CONSUME;
				}
			}

			return InteractionResult.PASS;
		}
		// endregion

		// region: Overrides
		@Override
		public final boolean hasAnalogOutputSignal(BlockState blockState)
		{
			return true;
		}

		@Override
		public final int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos)
		{
			return ContainerHelper.getRedstoneSignalFromContainer(level, pos);
		}

		@Override
		public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
		{
			return tryOpenContainerScreen(blockState, level, pos, player, hand, rayTraceResult);
		}

		@OverridingMethodsMustInvokeSuper
		@Override
		public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack)
		{
			super.setPlacedBy(level, pos, blockState, placer, stack);

			var blockEntity = getBlockEntity(level, pos);

			if(blockEntity instanceof NameableMutable nameable && stack.hasCustomHoverName())
			{
				var customName = stack.getHoverName();
				nameable.setCustomName(customName);
			}
		}

		@OverridingMethodsMustInvokeSuper
		@Override
		public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving)
		{
			if(!blockState.is(newBlockState.getBlock()))
			{
				var blockEntity = getBlockEntity(level, pos);

				if(blockEntity != null)
				{
					blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
						for(var i = 0; i < itemHandler.getSlots(); i++)
						{
							ItemStack stack = itemHandler.getStackInSlot(i);
							Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
						}
					});
				}
			}

			super.onRemove(blockState, level, pos, newBlockState, isMoving);
		}

		@Nullable
		@Override
		public final MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos pos)
		{
			var blockEntity = getBlockEntity(level, pos);

			if(blockEntity != null)
			{
				Component containerName = new TranslatableComponent(getDescriptionId());

				if(blockEntity instanceof Nameable nameable)
					containerName = nameable.getDisplayName();

				return new SimpleMenuProvider((windowId, playerInventory, player) -> {
					var buffer = new FriendlyByteBuf(Unpooled.buffer());
					buffer.writeBlockPos(pos);
					return getContainerType().create(windowId, playerInventory, buffer);
				}, containerName);
			}

			return null;
		}
		// endregion
	}
}