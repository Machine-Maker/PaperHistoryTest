plugins {
    `java-library`
    `maven-publish`
}

java {
    withSourcesJar()
    withJavadocJar()
}

val adventureVersion = "4.11.0"
val apiAndDocs: Configuration by configurations.creating {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}
configurations.api {
    extendsFrom(apiAndDocs)
}

dependencies {
    // api dependencies are listed transitively to API consumers
    api("com.google.guava:guava:31.0.1-jre")
    api("com.google.code.gson:gson:2.8.9")
    api("net.md-5:bungeecord-chat:1.16-R0.4-deprecated+build.6") // Paper
    api("org.yaml:snakeyaml:1.30")
    // Paper start
    api("com.googlecode.json-simple:json-simple:1.1.1") {
        isTransitive = false // includes junit
    }
    api("it.unimi.dsi:fastutil:8.5.6")
    apiAndDocs(platform("net.kyori:adventure-bom:$adventureVersion"))
    apiAndDocs("net.kyori:adventure-api")
    apiAndDocs("net.kyori:adventure-text-minimessage")
    apiAndDocs("net.kyori:adventure-text-serializer-gson")
    apiAndDocs("net.kyori:adventure-text-serializer-legacy")
    apiAndDocs("net.kyori:adventure-text-serializer-plain")
    apiAndDocs("net.kyori:adventure-text-logger-slf4j")
    api("org.apache.logging.log4j:log4j-api:2.17.1")
    api("org.slf4j:slf4j-api:1.8.0-beta4")

    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    // Paper end

    compileOnly("org.apache.maven:maven-resolver-provider:3.8.5")
    compileOnly("org.apache.maven.resolver:maven-resolver-connector-basic:1.7.3")
    compileOnly("org.apache.maven.resolver:maven-resolver-transport-http:1.7.3")
    compileOnly("com.google.code.findbugs:jsr305:1.3.9") // Paper

    val annotations = "org.jetbrains:annotations:23.0.0" // Paper - we don't want Java 5 annotations...
    compileOnly(annotations)
    testCompileOnly(annotations)

    // Paper start - add checker
    val checkerQual = "org.checkerframework:checker-qual:3.21.0"
    compileOnlyApi(checkerQual)
    testCompileOnly(checkerQual)
    // Paper end

    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testImplementation("org.ow2.asm:asm-tree:9.3")
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

val generateApiVersioningFile by tasks.registering {
    inputs.property("version", project.version)
    val pomProps = layout.buildDirectory.file("pom.properties")
    outputs.file(pomProps)
    val projectVersion = project.version
    doLast {
        pomProps.get().asFile.writeText("version=$projectVersion")
    }
}

tasks.jar {
    from(generateApiVersioningFile.map { it.outputs.files.singleFile }) {
        into("META-INF/maven/${project.group}/${project.name}")
    }
    manifest {
        attributes(
            "Automatic-Module-Name" to "org.bukkit"
        )
    }
}

tasks.withType<Javadoc> {
    val options = options as StandardJavadocDocletOptions
    options.overview = "src/main/javadoc/overview.html"
    options.use()
    options.isDocFilesSubDirs = true
    options.links(
        "https://guava.dev/releases/31.0.1-jre/api/docs/",
        "https://javadoc.io/doc/org.yaml/snakeyaml/1.30/",
        "https://javadoc.io/doc/org.jetbrains/annotations/23.0.0/", // Paper - we don't want Java 5 annotations
        // Paper start
        //"https://javadoc.io/doc/net.md-5/bungeecord-chat/1.16-R0.4/", // don't link to bungee chat
        "https://jd.adventure.kyori.net/api/$adventureVersion/",
        "https://jd.adventure.kyori.net/text-minimessage/$adventureVersion/",
        "https://jd.adventure.kyori.net/text-serializer-gson/$adventureVersion/",
        "https://jd.adventure.kyori.net/text-serializer-legacy/$adventureVersion/",
        "https://jd.adventure.kyori.net/text-serializer-plain/$adventureVersion/",
        // Paper end
    )

    inputs.files(apiAndDocs).ignoreEmptyDirectories().withPropertyName(apiAndDocs.name + "-configuration")
    doFirst {
        options.addStringOption(
            "sourcepath",
            apiAndDocs.resolvedConfiguration.files.joinToString(separator = File.pathSeparator, transform = File::getPath)
        )
    }

    // workaround for https://github.com/gradle/gradle/issues/4046
    inputs.dir("src/main/javadoc").withPropertyName("javadoc-sourceset")
    doLast {
        copy {
            from("src/main/javadoc") {
                include("**/doc-files/**")
            }
            into("build/docs/javadoc")
        }
    }
}

// Paper start
val scanJar = tasks.register("scanJarForBadCalls", io.papermc.paperweight.tasks.ScanJarForBadCalls::class) {
    badAnnotations.add("Lio/papermc/paper/annotation/DoNotUse;")
    jarToScan.set(tasks.jar.flatMap { it.archiveFile })
    classpath.from(configurations.compileClasspath)
}
tasks.check {
    dependsOn(scanJar)
}
// Paper end
