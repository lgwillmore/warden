val projectVersion: String by project

group = "codes.laurence.warden"
version = projectVersion

allprojects {
    repositories {
        mavenCentral()
    }
}
