package cz.alenkacz.gradle.jsonvalidator

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JacksonUtils
import com.github.fge.jsonschema.cfg.ValidationConfiguration
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Recursively search for all files in 'schemaFolder' and validates that the file is syntactically valid json schema
 * If invalid json schema found, it prints out the name of that file and the whole task ends with a failure
 */
class ValidateJsonSchemaSyntaxTask extends DefaultTask {
    private static final ObjectMapper MAPPER = JacksonUtils.newMapper();
    private static final def validator = new SyntaxValidator(ValidationConfiguration.byDefault())

    PluginExtension pluginExtension

    @TaskAction
    def validateSchema() {
        if (pluginExtension.schemaFolder == null) {
            throw new IllegalArgumentException("You need to provide 'schemaFolder' property, otherwise the plugin cannot validate your json files.")
        }
        File schemaFolder = new File(pluginExtension.schemaFolder)
        if (!schemaFolder.exists() || !schemaFolder.isDirectory()) {
            throw new IllegalArgumentException("Provided schema folder '${pluginExtension.schemaFolder}' does not exist or is not a directory")
        }
        def schemaFiles = []
        schemaFolder.eachFileRecurse (FileType.FILES) { file ->
            schemaFiles << file
        }
        def invalidSchemaFiles = []
        schemaFiles.each { sf ->
            def rootNode = MAPPER.readTree(sf)
            if (!validator.schemaIsValid(rootNode)) {
                invalidSchemaFiles << sf.toString()
            }
        }
        if (!invalidSchemaFiles.empty) {
            throw new JsonSchemaValidationException(invalidSchemaFiles)
        } else {
            logger.info("All schema files are valid.")
        }
    }
}
