package xyz.apex.forge.apexcore.core.entity;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import xyz.apex.forge.apexcore.core.init.ACEntities;
import xyz.apex.forge.apexcore.lib.block.ISeatBlock;

public final class SeatEntity extends Entity
{
	private Block seatBlock = Blocks.AIR;

	// default constructor for registration, do not call manually
	@ApiStatus.Internal
	public SeatEntity(EntityType<? extends SeatEntity> entityType, Level level)
	{
		super(entityType, level);

		noPhysics = true;
	}

	// used for entity client factory, do not call manually
	@ApiStatus.Internal
	public SeatEntity(PlayMessages.SpawnEntity spawnEntity, Level level)
	{
		this(ACEntities.SEAT_ENTITY.get(), level);
	}

	@Override
	public void tick()
	{
		super.tick();

		if(!level.isClientSide)
		{
			var pos = blockPosition();

			if(getPassengers().isEmpty() || !level.getBlockState(pos).is(seatBlock))
			{
				remove(RemovalReason.DISCARDED);
				level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());
				ISeatBlock.setSeatOccupied(level, pos, false);
			}
		}
	}

	@Override
	public double getPassengersRidingOffset()
	{
		return 0D;
	}

	@Override
	protected boolean canRide(Entity entity)
	{
		return true;
	}

	@Override
	protected void defineSynchedData()
	{
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tagCompound)
	{
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tagCompound)
	{
	}

	@Override
	public Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public static boolean create(Level level, BlockPos pos, ISeatBlock seatBlock, Player player)
	{
		if(level.isClientSide)
			return true;

		var blockState = level.getBlockState(pos);
		pos = seatBlock.getSeatSitPos(blockState, pos);
		blockState = level.getBlockState(pos);

		var x = pos.getX();
		var y = pos.getY();
		var z = pos.getZ();

		var seat = ACEntities.SEAT_ENTITY.create(level);

		if(seat != null)
		{
			var seatYOffset = seatBlock.getSeatYOffset(blockState);

			seat.setPos(x + .5D, y + seatYOffset, z + .5D);
			seat.seatBlock = blockState.getBlock();

			level.addFreshEntity(seat);
			player.startRiding(seat, false);

			return true;
		}

		return false;
	}
}