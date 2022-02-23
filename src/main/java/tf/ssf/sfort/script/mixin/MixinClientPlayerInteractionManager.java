package tf.ssf.sfort.script.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.script.extended.mixin.interfaces.LivingEntityInjected;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {
    @Inject(at=@At("HEAD"), method= "attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V")
    public void wasDamaged(PlayerEntity player, Entity target, CallbackInfo ci){
        if (player instanceof LivingEntityInjected)
            ((LivingEntityInjected)player).fscript$setAttack();
    }
}
