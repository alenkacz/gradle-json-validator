package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.GradleException

class JsonSchemaValidationException extends GradleException {
    public JsonSchemaValidationException(List<String> schemaFiles) {
        super("Invalid schema files: " + schemaFiles.join(","))
    }
}
