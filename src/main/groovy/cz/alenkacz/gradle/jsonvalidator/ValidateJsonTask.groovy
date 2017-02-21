package cz.alenkacz.gradle.jsonvalidator

import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFile
import org.json.JSONObject
import org.json.JSONTokener

import java.util.stream.Collectors

class ValidateJsonTask extends DefaultTask {
    @InputFile
    File jsonSchema
    @InputFile
    File targetJsonFile

    @TaskAction
    def validateJson() {
        // check files exist
        new FileInputStream(jsonSchema).withStream {
            JSONObject rawSchema = new JSONObject(new JSONTokener(it))
            Schema schema = SchemaLoader.load(rawSchema)
            try {
                schema.validate(new JSONObject(new FileInputStream(targetJsonFile).getText()))
            } catch (ValidationException e) {
                throw new InvalidJsonException(targetJsonFile.absolutePath, jsonSchema.absolutePath, getViolations(e))
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
