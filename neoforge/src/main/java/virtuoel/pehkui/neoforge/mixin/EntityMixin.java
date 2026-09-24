package virtuoel.pehkui.neoforge.mixin;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.data.RuleApplicationState;
import virtuoel.pehkui.neoforge.data.ScaleRules;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.util.PehkuiEntityExtensions;
import virtuoel.pehkui.util.ScaleUtils;
import virtuoel.pehkui.util.VanillaScaleSyncBack;

@Mixin(Entity.class)
public abstract class EntityMixin implements PehkuiEntityExtensions
{
	@Shadow
	private boolean onGround;
	@Shadow
	protected boolean firstTick;
	@Shadow
	private BlockPos blockPosition;

	@Unique
	protected void setPosDirectly(final BlockPos pos)
	{
		blockPosition = pos;
	}

	@Override
	public void pehkui_setPosDirectly(BlockPos pos)
	{
		setPosDirectly(pos);
	}

	@Unique
	private boolean pehkui_shouldSyncScales = false;
	@Unique
	private boolean pehkui_shouldIgnoreScaleNbt = false;
	@Unique
	private ScaleData[] pehkui_scaleCache = null;
	@Unique
	private RuleApplicationState pehkui_ruleState = null;
	
	@Override
	public ScaleData pehkui_constructScaleData(ScaleType type)
	{
		return ScaleData.Builder.create().type(type).entity((Entity) (Object) this).build();
	}
	
	@Override
	public ScaleData[] pehkui_getScaleCache()
	{
		return pehkui_scaleCache;
	}
	
	@Override
	public void pehkui_setScaleCache(ScaleData[] scaleCache)
	{
		pehkui_scaleCache = scaleCache;
	}
	
	@Override
	public void pehkui_setShouldSyncScales(boolean sync)
	{
		pehkui_shouldSyncScales = sync;
	}
	
	@Override
	public boolean pehkui_shouldSyncScales()
	{
		return pehkui_shouldSyncScales;
	}
	
	@Override
	public boolean pehkui_shouldIgnoreScaleNbt()
	{
		return pehkui_shouldIgnoreScaleNbt;
	}
	
	@Override
	public void pehkui_setShouldIgnoreScaleNbt(boolean ignore)
	{
		pehkui_shouldIgnoreScaleNbt = ignore;
	}
	
	@Inject(at = @At("HEAD"), method = "load")
	private void pehkui$readData(ValueInput view, CallbackInfo info)
	{
		final CompoundTag tag = new CompoundTag();

		view.read(Pehkui.MOD_ID + ":scale_data_types", CompoundTag.CODEC).ifPresent(typeData ->
			tag.put(Pehkui.MOD_ID + ":scale_data_types", typeData));

		view.read(Pehkui.MOD_ID + ":scale_rule_state", CompoundTag.CODEC).ifPresent(ruleState ->
			tag.put(Pehkui.MOD_ID + ":scale_rule_state", ruleState));

		if (!tag.isEmpty())
		{
			pehkui_readScaleNbt(tag);
		}
	}

	@Override
	public void pehkui_readScaleNbt(CompoundTag nbt)
	{
		if (pehkui_shouldIgnoreScaleNbt())
		{
			return;
		}

		// /scale debug delete_scale_data 标记的实体丢弃整份缩放数据；规则快照要跟着一起丢，
		// 否则基准还停在旧值上，规则下一轮会从错误的起点重算
		if (nbt.contains(Pehkui.MOD_ID + ":scale_data_types") && DebugCommand.unmarkEntityForScaleReset((Entity) (Object) this, nbt))
		{
			pehkui_setRuleState(null);
			return;
		}

		// 规则快照必须随实体存档：丢了它，重载后会把规则写进去的值当成基准，下一轮就开始累积
		if (nbt.contains(Pehkui.MOD_ID + ":scale_rule_state"))
		{
			final RuleApplicationState state = new RuleApplicationState();
			state.readNbt(nbt.getCompoundOrEmpty(Pehkui.MOD_ID + ":scale_rule_state"));
			pehkui_setRuleState(state.isEmpty() ? null : state);
		}

		if (nbt.contains(Pehkui.MOD_ID + ":scale_data_types"))
		{
			final CompoundTag typeData = nbt.getCompoundOrEmpty(Pehkui.MOD_ID + ":scale_data_types");

			String key;
			ScaleData scaleData;
			for (final Map.Entry<Identifier, ScaleType> entry : ScaleRegistries.SCALE_TYPES.entrySet())
			{
				key = entry.getKey().toString();

				if (typeData.contains(key))
				{
					scaleData = pehkui_getScaleData(entry.getValue());
					scaleData.readNbt(typeData.getCompoundOrEmpty(key));
				}
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "saveWithoutId")
	private void pehkui$writeData(ValueOutput view, CallbackInfo info)
	{
		final CompoundTag tag = pehkui_writeScaleNbt(new CompoundTag());

		if (tag.contains(Pehkui.MOD_ID + ":scale_data_types"))
		{
			view.store(Pehkui.MOD_ID + ":scale_data_types", CompoundTag.CODEC, tag.getCompoundOrEmpty(Pehkui.MOD_ID + ":scale_data_types"));
		}

		if (tag.contains(Pehkui.MOD_ID + ":scale_rule_state"))
		{
			view.store(Pehkui.MOD_ID + ":scale_rule_state", CompoundTag.CODEC, tag.getCompoundOrEmpty(Pehkui.MOD_ID + ":scale_rule_state"));
		}
	}
	
	@Override
	public CompoundTag pehkui_writeScaleNbt(CompoundTag nbt)
	{
		if (pehkui_shouldIgnoreScaleNbt())
		{
			return nbt;
		}
		
		final CompoundTag typeData = new CompoundTag();
		
		CompoundTag compound;
		for (final ScaleData scaleData : pehkui_getScales().values())
		{
			if (scaleData != null)
			{
				compound = scaleData.writeNbt(new CompoundTag());
				
				if (compound.size() != 0)
				{
					typeData.put(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, scaleData.getScaleType()).toString(), compound);
				}
			}
		}
		
		if (typeData.size() > 0)
		{
			nbt.put(Pehkui.MOD_ID + ":scale_data_types", typeData);
		}

		final RuleApplicationState ruleState = pehkui_getRuleState();

		if (ruleState != null && !ruleState.isEmpty())
		{
			nbt.put(Pehkui.MOD_ID + ":scale_rule_state", ruleState.writeNbt());
		}

		return nbt;
	}
	
	@Inject(at = @At("HEAD"), method = "tick")
	private void pehkui$tick(CallbackInfo info)
	{
		for (ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			ScaleUtils.tickScale(pehkui_getScaleData(type));
		}

		final Entity self = (Entity) (Object) this;

		VanillaScaleSyncBack.tick(self);

		final int interval = PehkuiConfig.COMMON.scaleRuleCheckInterval.get();

		if (interval > 0 && PehkuiConfig.COMMON.enableScaleRules.get() && !ScaleRules.isEmpty() && self.level() instanceof ServerLevel && self.tickCount % interval == 0)
		{
			final boolean affectPlayers = PehkuiConfig.COMMON.scaleRulesAffectPlayers.get();

			ScaleRules.applyRuleToEntity(self, (ServerLevel) self.level(), affectPlayers || !(self instanceof Player));
		}
	}
	
	@ModifyReturnValue(method = "getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;", at = @At("RETURN"))
	private EntityDimensions pehkui$getDimensions(EntityDimensions original)
	{
		return ScaleUtils.getScaledDimensions(original, (Entity) (Object) this);
	}

	@ModifyReturnValue(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
	private ItemEntity pehkui$dropStack(ItemEntity entity)
	{
		if (entity != null)
		{
			ScaleUtils.setScaleOfDrop(entity, (Entity) (Object) this);
		}
		return entity;
	}
	
	@ModifyExpressionValue(method = "move", at = @At(value = "CONSTANT", args = "doubleValue=1.0E-7D"))
	private double pehkui$move$minVelocity(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		return scale < 1.0F ? scale * scale * value : value;
	}
	
	@ModifyArg(method = "move", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;maybeBackOffFromEdge(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 pehkui$move$adjustMovementForSneaking(Vec3 movement, MoverType type)
	{
		if (type == MoverType.SELF || type == MoverType.PLAYER)
		{
			return movement.scale(ScaleUtils.getMotionScale((Entity) (Object) this));
		}
		
		return movement;
	}
	
	@WrapOperation(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
	private void pehkui$pushSelfAwayFrom$other(Entity obj, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true) Entity other)
	{
		final float otherScale = ScaleUtils.getMotionScale(other);
		
		if (otherScale != 1.0F)
		{
			x *= otherScale;
			z *= otherScale;
		}
		
		original.call(obj, x, y, z);
	}
	
	@WrapOperation(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
	private void pehkui$pushSelfAwayFrom$self(Entity obj, double x, double y, double z, Operation<Void> original)
	{
		final float ownScale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		if (ownScale != 1.0F)
		{
			x *= ownScale;
			z *= ownScale;
		}
		
		original.call(obj, x, y, z);
	}
	
	@Inject(at = @At("HEAD"), method = "spawnSprintParticle", cancellable = true)
	private void pehkui$spawnSprintingParticles(CallbackInfo info)
	{
		if (ScaleUtils.getMotionScale((Entity) (Object) this) < 1.0F)
		{
			info.cancel();
		}
	}
	
	@Override
	public boolean pehkui_isFirstUpdate()
	{
		return this.firstTick;
	}
	
	@Override
	public boolean pehkui_getOnGround()
	{
		return this.onGround;
	}
	
	@Override
	public void pehkui_setOnGround(boolean onGround)
	{
		this.onGround = onGround;
	}

	@Override
	public RuleApplicationState pehkui_getRuleState()
	{
		return pehkui_ruleState;
	}

	@Override
	public void pehkui_setRuleState(RuleApplicationState state)
	{
		pehkui_ruleState = state;
	}

	@ModifyArg(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;D)V"))
	private double pehkui$fall$fallDistance(double distance)
	{
		final float scale = ScaleUtils.getFallingScale((Entity) (Object) this);

		if (scale != 1.0F)
		{
			if (PehkuiConfig.COMMON.scaledFallDamage.get())
			{
				return distance * scale;
			}
		}

		return distance;
	}

	@ModifyExpressionValue(method = "applyMovementEmissionAndPlaySound", at = @At(value = "CONSTANT", args = "floatValue=0.6F"))
	private float pehkui$applyMovementEmissionAndPlaySound$distance(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		if (scale != 1.0F)
		{
			return value / scale;
		}

		return value;
	}

	@ModifyArg(method = "getPassengerRidingPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPassengerAttachmentPoint(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityDimensions;F)Lnet/minecraft/world/phys/Vec3;"))
	private float pehkui$getPassengerRidingPos$getPassengerAttachmentPos(float value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		return scale == 1.0F ? value : value * scale;
	}

	@ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
	private double pehkui$getFinalGravity(double original)
	{
		if (original == 0.0D)
		{
			return 0.0D;
		}

		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		return scale != 1.0F ? original : original * scale;
	}

	@ModifyExpressionValue(method = "checkSupportingBlock", at = @At(value = "CONSTANT", args = "doubleValue=1.0E-6"))
	private double pehkui$updateSupportingBlockPos$offset(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale < 1.0F ? value * scale : value;
	}
}
