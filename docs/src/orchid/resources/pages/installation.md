## Gradle
**Add the repo**
```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/lgwillmore/codes.laurence.warden" 
    }
}
```
**Add the dependency**
```groovy
compile 'codes.laurence.warden:warden-core-jvm:0.0.1'
```
---
## Maven
**Add the repo**
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    
    <profiles>
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-lgwillmore-codes.laurence.warden</id>
                    <name>bintray</name>
                    <url>https://dl.bintray.com/lgwillmore/codes.laurence.warden</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-lgwillmore-codes.laurence.warden</id>
                    <name>bintray-plugins</name>
                    <url>https://dl.bintray.com/lgwillmore/codes.laurence.warden</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
</settings>
```
**Add the dependency**
```xml
<dependency>
  <groupId>codes.laurence.warden</groupId>
  <artifactId>warden-core-jvm</artifactId>
  <version>0.0.1</version>
  <type>pom</type>
</dependency>
```