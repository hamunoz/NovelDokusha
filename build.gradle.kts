// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
	repositories {
		google()
		jcenter()
		mavenCentral()
	}
	dependencies {
		classpath("com.android.tools.build:gradle:8.1.0")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
		classpath("com.google.dagger:hilt-android-gradle-plugin:2.45")
	}
}

plugins {
	id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
}

allprojects {
	repositories {
		google()
		jcenter()
		maven { setUrl("https://jitpack.io") }
	}
}

tasks.register("clean", Delete::class) {
	delete(rootProject.buildDir)
}