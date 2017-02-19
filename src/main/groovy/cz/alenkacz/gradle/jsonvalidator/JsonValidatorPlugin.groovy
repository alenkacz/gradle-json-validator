package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.Project
import org.gradle.api.Plugin

class JsonValidatorPlugin implements Plugin<Project> {
    void apply(Project target) {
        target.task('validateJson', type: ValidateJsonTask)
        ValidateJsonSchemaSyntaxTask validateSchemaTask = target.task('validateJsonSchema', type: ValidateJsonSchemaSyntaxTask)
        PluginExtension extension = target.extensions.create('jsonSchema', PluginExtension)

        validateSchemaTask.setPluginExtension(extension)
    }
}
