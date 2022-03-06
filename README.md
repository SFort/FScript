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
	modImplementation("tf.ssf.sfort:fscript:VERSION") {
		transitive = false
	}
}
```

MC Version / Latest Available FScript:

1.18.2          : `2.1.2`
1.17   - 1.18.1 : `2.1.1`
1.15.2 - 1.16.5 : `1.1.5-1.16`

