package com.github.jdill.glowinc.entity.projectile;

import com.github.jdill.glowinc.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class GlowBallEntity extends ProjectileItemEntity implements IRendersAsItem {

    public static final String ID = "glow_ball_entity";

    private SoundEvent soundEvent = SoundEvents.BLOCK_SLIME_BLOCK_BREAK;

    public GlowBallEntity(LivingEntity livingEntityIn, World worldIn) {
        super(Registry.SAP_ENTITY.get(), livingEntityIn, worldIn);
    }

    public GlowBallEntity(EntityType<GlowBallEntity> entityType, World worldIn) {
        super(entityType, worldIn);
    }

    public GlowBallEntity(EntityType<? extends ProjectileItemEntity> type, double x, double y, double z,
        World worldIn) {
        super(type, x, y, z, worldIn);
    }

    @Override
    protected Item getDefaultItem() {
        return Registry.GLOW_BALL_ITEM.get();
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.BLOCKS;
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult result) {
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.GLOWING, 200));
        }
    }

    @Override
    protected void func_230299_a_(BlockRayTraceResult result) {
        super.func_230299_a_(result);
        if (!this.world.isRemote) {
            BlockPos hitBlockPos = result.getPos();
            BlockState hitBlockState = this.world.getBlockState(hitBlockPos);
            Direction direction = result.getFace();
            BlockPos maybeBlockPos = hitBlockPos.offset(direction);
            BlockState blockState = this.world.getBlockState(maybeBlockPos);
            if (hitBlockState.isSolidSide(this.world, hitBlockPos, direction) &&
                (blockState.isAir() || Blocks.WATER.equals(blockState.getBlock()))
            ) {
                BlockState state = Registry.GLOW_BALL_BLOCK.get().getDefaultState();
                BlockState alteredBlockState = state.with(BlockStateProperties.FACING, direction);
                this.world.setBlockState(maybeBlockPos, alteredBlockState);
                soundEvent = SoundEvents.BLOCK_SLIME_BLOCK_PLACE;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private IParticleData makeParticle() {
        return new ItemParticleData(ParticleTypes.ITEM, this.getItem());
    }

    @OnlyIn(Dist.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            int numParticles = 5;
            for (int i = 0; i < numParticles; ++i) {
                this.world
                    .addParticle(makeParticle(), this.getPosX(), this.getPosY(), this.getPosZ(),
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D,
                        ((double) this.rand.nextFloat() - 0.1D) * 0.08D,
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        super.onImpact(result);
        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte) 3);
            this.playSound(soundEvent, 0.8f, 0.8f);
            this.remove();
        }

    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
