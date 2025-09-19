rootProject.name = "loopers-donghee"

include(
    ":apps:commerce-api",
    ":apps:pg-simulator",
    ":apps:commerce-collector",
    ":modules:jpa",
    ":modules:redis",
    ":modules:kafka",
    ":modules:common-events",
    ":modules:common-support",
    ":supports:jackson",
    ":supports:logging",
    ":supports:monitoring",
)

// configurations
pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
            }
        }
    }
}
include(":modules:redis")
findProject(":modules:redis")?.name = "redis"
include("modules:kafka")
findProject(":modules:kafka")?.name = "kafka"
include("modules:kafka")
findProject(":modules:kafka")?.name = "kafka"
include("apps:commerce-collector")
findProject(":apps:commerce-collector")?.name = "commerce-collector"
include("modules:common-events")
findProject(":modules:common-events")?.name = "common-events"
include("modules:common-support")
findProject(":modules:common-support")?.name = "common-support"
include("apps:commerce-batch")
findProject(":apps:commerce-batch")?.name = "commerce-batch"
