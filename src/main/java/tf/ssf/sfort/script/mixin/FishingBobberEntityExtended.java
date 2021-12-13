package tf.ssf.sfort.script.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityExtended {
    @Accessor("caughtFish")
    boolean fscript$caughtFish();
}
