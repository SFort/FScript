package tf.ssf.sfort.script.instance;

import net.minecraft.entity.projectile.FishingBobberEntity;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;

import java.util.function.Predicate;

public class FishingBobberEntityScript extends AbstractExtendablePredicateProvider<FishingBobberEntity> {

    public FishingBobberEntityScript() {
        help.put("bobber_in_open_water is_bobber_in_open_water","Require a fishing bobber in open water");
    }

    @Override
    public Predicate<FishingBobberEntity> getLocalPredicate(String in){
        switch (in){
            case "is_bobber_in_open_water": case  "bobber_in_open_water" : return FishingBobberEntity::isInOpenWater;
            default : return null;
        }
    }

}
