package cz.alenkacz.gradle.jsonvalidator

import groovy.io.FileType
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.json.JSONArray
import org.json.JSONException
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
    @Optional
    Boolean onlyWithJsonExtension

    @TaskAction
    def validateJson() {
        // check files exist
        if (onlyWithJsonExtension != null && targetJsonDirectory == null) {
            throw new IllegalArgumentException("Cannot use onlyWithJsonExtension property without using targetJsonDirectory.")
        }
        if (onlyWithJsonExtension == null) {
            onlyWithJsonExtension = false
        }
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
                    if (schema instanceof ArraySchema) {
                        schema.validate(new JSONArray(new FileInputStream(it).getText()))
                    } else {
                        schema.validate(new JSONObject(new FileInputStream(it).getText()))
                    }
                } catch (ValidationException e) {
                    violations << new ValidationError(jsonFilePath: it.absolutePath, violations: getViolations(e))
                } catch (JSONException e) {
                    if (!onlyWithJsonExtension || hasJsonExtension(it)) {
                        // invalid json file is considered a violation only when onlyWithJsonExtension is not used or if the file does not have json extension
                        violations << new ValidationError(jsonFilePath: it.absolutePath, violations: ["File is not valid json: ${e.message}"])
                    }
                }
            }
            if (!violations.empty) {
                throw new InvalidJsonException(violations, jsonSchema.absolutePath)
            }
        }
    }

    def hasJsonExtension(File file) {
        file.name.contains(".") && file.name.take(file.name.lastIndexOf('.')) == "json"
    }

    List<String> getViolations(ValidationException e) {
        if (e.causingExceptions.empty) {
            [e.message]
        } else {
            e.causingExceptions.stream().map{ ex -> ex.message }.collect(Collectors.toList())
        }
    }
}
