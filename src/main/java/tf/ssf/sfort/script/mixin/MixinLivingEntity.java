package tf.ssf.sfort.script.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.script.extended.mixin.interfaces.LivingEntityInjected;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements LivingEntityInjected {

    @Shadow private int lastAttackTime;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    private int fscript$lastAttacked = 0;

    @Inject(at=@At("HEAD"), method="setAttacker(Lnet/minecraft/entity/LivingEntity;)V")
    public void wasDamaged(LivingEntity attacker, CallbackInfo ci){
        if (attacker != null) fscript$setAttacked();
    }

    @Override
    public void fscript$setAttacked() {
        fscript$lastAttacked = this.age;
    }

    @Override
    public void fscript$setAttack() {
        this.lastAttackTime = this.age;
    }

    @Override
    public boolean fscript$attacked(int i) {
        return this.age - fscript$lastAttacked > i || fscript$lastAttacked == 0;
    }
}
