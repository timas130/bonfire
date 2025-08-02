import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.apollographql.apollo3").version("3.8.6")
}

group = "sh.sit.bonfire"

apollo {
    service("melior") {
        packageName.set("sh.sit.schema")
        generateApolloMetadata.set(true)
        generateFragmentImplementations.set(true)
        alwaysGenerateTypesMatching.set(listOf(
            "LoginResultSuccess", "Query", "Mutation", "User", "Ok", "Badge"
        ))

        introspection {
            endpointUrl.set("http://localhost:8000")
            schemaFile.set(file("src/main/graphql/sh/sit/bonfire/schema/schema.graphqls"))
        }

        mapScalar("JSONObject", "kotlin.String", "sh.sit.bonfire.schema.JsonAdapter")
        mapScalar("JSON", "kotlin.String", "sh.sit.bonfire.schema.JsonAdapter")
        mapScalar("DateTime", "sh.sit.bonfire.schema.DateTime", "sh.sit.bonfire.schema.DateTime.adapter")
        mapScalar("NaiveDate", "sh.sit.bonfire.schema.NaiveDate", "sh.sit.bonfire.schema.NaiveDate.adapter")
    }
}
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.6")
    implementation("com.apollographql.apollo3:apollo-normalized-cache-incubating:3.8.6")
    implementation("joda-time:joda-time:2.12.6")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
