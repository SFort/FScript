package tf.ssf.sfort.script.mixin;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.class)
public interface BiomeExtended {
    @Invoker("getCategory")
    Biome.Category fscript$getCategory();
}