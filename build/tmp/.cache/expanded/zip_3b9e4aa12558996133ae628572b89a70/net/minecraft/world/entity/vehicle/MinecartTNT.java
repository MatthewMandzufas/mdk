package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class MinecartTNT extends AbstractMinecart {
    private static final byte EVENT_PRIME = 10;
    private int fuse = -1;

    public MinecartTNT(EntityType<? extends MinecartTNT> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public MinecartTNT(Level pLevel, double pX, double pY, double pZ) {
        super(EntityType.TNT_MINECART, pLevel, pX, pY, pZ);
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.TNT;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.fuse > 0) {
            this.fuse--;
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.fuse == 0) {
            this.explode(this.getDeltaMovement().horizontalDistanceSqr());
        }

        if (this.horizontalCollision) {
            double d0 = this.getDeltaMovement().horizontalDistanceSqr();
            if (d0 >= 0.01F) {
                this.explode(d0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getDirectEntity() instanceof AbstractArrow abstractarrow && abstractarrow.isOnFire()) {
            DamageSource damagesource = this.damageSources().explosion(this, pSource.getEntity());
            this.explode(damagesource, abstractarrow.getDeltaMovement().lengthSqr());
        }

        return super.hurt(pSource, pAmount);
    }

    @Override
    public void destroy(DamageSource pSource) {
        double d0 = this.getDeltaMovement().horizontalDistanceSqr();
        if (!damageSourceIgnitesTnt(pSource) && !(d0 >= 0.01F)) {
            this.destroy(this.getDropItem());
        } else {
            if (this.fuse < 0) {
                this.primeFuse();
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }
        }
    }

    @Override
    protected Item getDropItem() {
        return Items.TNT_MINECART;
    }

    protected void explode(double pRadiusModifier) {
        this.explode(null, pRadiusModifier);
    }

    protected void explode(@Nullable DamageSource pDamageSource, double pRadiusModifier) {
        if (!this.level().isClientSide) {
            double d0 = Math.sqrt(pRadiusModifier);
            if (d0 > 5.0) {
                d0 = 5.0;
            }

            this.level()
                .explode(
                    this,
                    pDamageSource,
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    (float)(4.0 + this.random.nextDouble() * 1.5 * d0),
                    false,
                    Level.ExplosionInteraction.TNT
                );
            this.discard();
        }
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (pFallDistance >= 3.0F) {
            float f = pFallDistance / 10.0F;
            this.explode((double)(f * f));
        }

        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    @Override
    public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
        if (pReceivingPower && this.fuse < 0) {
            this.primeFuse();
        }
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 10) {
            this.primeFuse();
        } else {
            super.handleEntityEvent(pId);
        }
    }

    public void primeFuse() {
        this.fuse = 80;
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)10);
            if (!this.isSilent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public int getFuse() {
        return this.fuse;
    }

    public boolean isPrimed() {
        return this.fuse > -1;
    }

    @Override
    public float getBlockExplosionResistance(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower) {
        return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS)
            ? super.getBlockExplosionResistance(pExplosion, pLevel, pPos, pBlockState, pFluidState, pExplosionPower)
            : 0.0F;
    }

    @Override
    public boolean shouldBlockExplode(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, float pExplosionPower) {
        return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS)
            ? super.shouldBlockExplode(pExplosion, pLevel, pPos, pBlockState, pExplosionPower)
            : false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("TNTFuse", 99)) {
            this.fuse = pCompound.getInt("TNTFuse");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("TNTFuse", this.fuse);
    }

    @Override
    boolean shouldSourceDestroy(DamageSource pSource) {
        return damageSourceIgnitesTnt(pSource);
    }

    private static boolean damageSourceIgnitesTnt(DamageSource pSource) {
        return pSource.is(DamageTypeTags.IS_FIRE) || pSource.is(DamageTypeTags.IS_EXPLOSION);
    }
}