package cz.alenkacz.gradle.jsonvalidator

import groovy.io.FileType
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFile
import org.json.JSONObject
import org.json.JSONTokener

import java.util.stream.Collectors

class ValidateJsonTask extends DefaultTask {
    @InputFile
    File jsonSchema
    @InputFile
    @Optional
    File targetJsonFile
    @InputDirectory
    @Optional
    File targetJsonDirectory

    @TaskAction
    def validateJson() {
        // check files exist
        new FileInputStream(jsonSchema).withStream {
            JSONObject rawSchema = new JSONObject(new JSONTokener(it))
            Schema schema = SchemaLoader.load(rawSchema)
            List<File> targetJsonFilesList = []
            if (targetJsonDirectory != null) {
                targetJsonDirectory.eachFileRecurse (FileType.FILES) { file ->
                    targetJsonFilesList << file
                }
            } else {
                targetJsonFilesList << targetJsonFile
            }
            List<ValidationError> violations = []
            targetJsonFilesList.each {
                try {
                    println(it.absolutePath)
                    schema.validate(new JSONObject(new FileInputStream(it).getText()))
                } catch (ValidationException e) {
                    violations << new ValidationError(jsonFilePath: it.absolutePath, violations: getViolations(e))
                }
            }
            if (!violations.empty) {
                throw new InvalidJsonException(violations, jsonSchema.absolutePath)
            }
        }
    }

    List<String> getViolations(ValidationException e) {
        if (e.causingExceptions.empty) {
            [e.message]
        } else {
            e.causingExceptions.stream().map{ ex -> ex.message }.collect(Collectors.toList())
        }
    }
}
