package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.GradleException

class InvalidJsonException extends GradleException {
    public InvalidJsonException(String jsonFilePath, String jsonSchemaPath, List<String> violations) {
        super("Error while validating json file '$jsonFilePath' against '$jsonSchemaPath'. ${violations.size()} violations found: ${System.getProperty("line.separator")}${violations.join(System.getProperty("line.separator"))}")
    }
}
