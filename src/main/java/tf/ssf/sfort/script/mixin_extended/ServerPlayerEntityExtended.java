package tf.ssf.sfort.script.mixin_extended;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityExtended {
    @Accessor("seenCredits")
    boolean fscript$seenCredits();
}