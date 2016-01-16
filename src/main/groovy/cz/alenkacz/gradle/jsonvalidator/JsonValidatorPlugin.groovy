package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.Project
import org.gradle.api.Plugin

class JsonValidatorPlugin implements Plugin<Project> {
    void apply(Project target) {
        target.task('validateJson', type: ValidateJsonTask)
    }
}
