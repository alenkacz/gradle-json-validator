package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.GradleException

class InvalidJsonException extends GradleException {
    public InvalidJsonException(List<ValidationError> validationErrors, String jsonSchemaPath) {
        super(getMessage(validationErrors, jsonSchemaPath))
    }

    static getMessage(List<ValidationError> validationErrors, String jsonSchemaPath) {
        def errorMessage = new StringBuilder("One or more validation errors found against schema '$jsonSchemaPath': ${System.getProperty("line.separator")}")
        errorMessage.append(validationErrors.collect {
            "File '$it.jsonFilePath' has ${it.violations.size()} violations: ${System.getProperty("line.separator")}${it.violations.join(System.getProperty("line.separator"))}"
        }.join(System.getProperty("line.separator")))
        errorMessage.toString()
    }
}

class ValidationError {
    String jsonFilePath
    List<String> violations
}
