package tf.ssf.sfort.script.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityExtended {
    @Invoker("isSleepingInBed")
    boolean fscript$isSleepingInBed();
}