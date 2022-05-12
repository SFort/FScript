package tf.ssf.sfort.script.instance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class DamageSourceScript extends AbstractExtendablePredicateProvider<DamageSource> {

	public DamageSourceScript() {
		help.put("projectile is_projectile","Damage source must be a projectile");
		help.put("is_explosive explosive", "Damage source must be explosive");
		help.put("bypasses_armor", "Damage source bypasses armor");
		help.put("is_out_of_world out_of_world", "Damage source must be out of world (void damage)");
		help.put("is_unblockable unblockable", "Damage source cannot be blocked");
		help.put("is_fire fire", "Damage source must be fire");
		help.put("is_source_creative_player source_creative_player", "Damage source must be from a creative player");
		help.put("is_thorns thorns", "Damage source must be thorns");
		help.put("name damage_source_name:DamageSourceID", "Damage source must have a matching name");
		help.put("~source:ENTITY", "Damage source's source entity must match");
		help.put("~attacker:ENTITY", "Damage source's attacker must match");
	}

	@Override
	public Predicate<DamageSource> getLocalPredicate(String in, String val){
		switch (in){
			case "name": case "damage_source_name": return ds -> val.equals(ds.getName());
			default: return null;
		}
	}

	@Override
	public Predicate<DamageSource> getLocalEmbed(String in, String script){
		switch (in){
			case "source":
			case "attacker": {
				final Predicate<Entity> predicate = DefaultParsers.ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				switch (in) {
					case "source": return ds -> ds.getSource() != null && predicate.test(ds.getSource());
					case "attacker": return ds -> ds.getAttacker() != null && predicate.test(ds.getAttacker());
				}
			}
			default: return null;
		}
	}


	@Override
	public Predicate<DamageSource> getLocalPredicate(String in){
		switch (in){
			case "is_projectile": case  "projectile" : return DamageSource::isProjectile;
			case "is_explosive": case  "explosive" : return DamageSource::isExplosive;
			case "bypasses_armor": return DamageSource::bypassesArmor;
			case "is_out_of_world": case  "out_of_world" : return DamageSource::isOutOfWorld;
			case "is_unblockable": case  "unblockable" : return DamageSource::isUnblockable;
			case "is_fire": case  "fire" : return DamageSource::isFire;
			case "is_scaled_with_difficulty": case  "scaled_with_difficulty" : return DamageSource::isScaledWithDifficulty;
			case "is_magic": case  "magic" : return DamageSource::isMagic;
			case "is_source_creative_player": case  "source_creative_player" : return DamageSource::isSourceCreativePlayer;
			case "is_thorns": case "thorns": return ds -> ds instanceof EntityDamageSource && ((EntityDamageSource)ds).isThorns();
			default: return null;
		}
	}

}
