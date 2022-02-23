package tf.ssf.sfort.script.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.script.extended.mixin.interfaces.LivingEntityInjected;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    @Inject(at=@At("HEAD"), method="applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V")
    public void wasDamaged(DamageSource source, float amount, CallbackInfo ci){
        Object self = this;
        if (source.getAttacker() != null && self instanceof LivingEntityInjected && amount != 0.0F)
            ((LivingEntityInjected)self).fscript$setAttacked();
    }
}
