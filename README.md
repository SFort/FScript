# FScript

Allows easily making minecraft scriptable

This was mostly made for [SurvivalFlight](https://github.com/sfort/MC-SurvivalFlight),
ergo it's currently focused on making the player scriptable.
despite that it easy to extend and apply to any other mod.

Maven:
```
repositories {
	maven {
		url = 'https://maven.ssf.tf/'
	}
}
dependencies {
	modImplementation("tf.ssf.sfort:fscript:1.1.4") {
		transitive = false
	}
}
```
