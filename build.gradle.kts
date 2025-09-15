plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.openapi.generator") version "7.15.0"
    `maven-publish`
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.polyglots"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.36")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

openApiGenerate {
    generatorName.set("kotlin-spring")                  // Kotlin + Spring generator
    inputSpec.set("$rootDir/src/main/resources/specs/user-spec-client-api.yaml") // main spec file
    outputDir.set("$buildDir/generated")               // where code will be generated
    apiPackage.set("com.polyglots.userclientapi.api")  // package for interfaces
    modelPackage.set("com.polyglots.userclientapi.model") // package for models/DTOs

    // Prevent test stubs if you don’t need them
    generateApiTests.set(false)               // optional
    generateModelTests.set(false)             // optional

    globalProperties.set(
        mapOf(
            "models" to "",      // generate models
            "apis" to ""         // generate api interfaces
            // no "supportingFiles" → nothing else
        )
    )

    // Key option: generate interfaces only
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useTags" to "true", // optional: groups controllers by tag
            "serializableModel" to "true", // generates plain models without extra funky annotations
            "useBeanValidation" to "true",         // makes models cleaner
            "annotationLibrary" to "SWAGGER2"       // ensures only @JsonProperty on fields
        )
    )
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.Polyglots-Network"
            artifactId = "user-client-api"
            version = "1.0.0"

            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
// 1. Make the main Kotlin compilation depend on the generated sources
tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

// 2. Ensure the JAR task includes the compiled generated code
tasks.named<Jar>("jar") {
    dependsOn("openApiGenerate")
}
kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        kotlin {
            srcDir("$buildDir/generated/src/main/kotlin")
        }
    }
}