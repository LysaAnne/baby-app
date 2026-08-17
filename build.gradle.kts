plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
}

tasks.register<CheckCodeStyleTask>("checkCodeStyle") {
    group = "verification"
    description = "Checks source files for tabs and trailing whitespace."
    sourceFiles.from(
        fileTree(rootDir) {
            include("**/*.kt", "**/*.kts", "**/*.xml")
            exclude(".gradle/**", ".git/**", "**/build/**")
        },
    )
}
