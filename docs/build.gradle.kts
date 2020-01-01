plugins {
    id("com.eden.orchidPlugin") version "0.18.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

val orchidVersion: String by project

dependencies {
    orchidRuntime("io.github.javaeden.orchid:OrchidAll:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidEditorial:$orchidVersion")
}

orchid{
    // Theme is required
    theme = "Editorial"

    // The following properties are optional
    version = "${project.version}"
    baseUrl = "http://localhost:8080"                   // a baseUrl prepended to all generated links. Defaults to '/'
//    srcDir  = "path/to/new/source/directory"      // defaults to 'src/orchid/resources'
//    destDir = "path/to/new/destination/directory" // defaults to 'build/docs/orchid'
    runTask = "build"
}