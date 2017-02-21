package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ValidateJsonTaskTest extends Specification  {
    @Rule final TemporaryFolder targetProjectDir = new TemporaryFolder()
    File gradleBuildFile
    File jsonSchemaFile
    File targetJsonFile

    def setup() {
        gradleBuildFile = targetProjectDir.newFile('build.gradle')
        def pluginClasspath = sourceCodeOfThisPluginClasspath()
        gradleBuildFile << """
            buildscript {
                dependencies {
                    classpath files($pluginClasspath)
                }
            }
            apply plugin: 'cz.alenkacz.gradle.jsonvalidator'

            import cz.alenkacz.gradle.jsonvalidator.ValidateJsonTask

            task validateCustomJson(type: ValidateJsonTask) {
                targetJsonFile = project.file("target.json")
                jsonSchema = project.file("schema.json")
            }
        """

        jsonSchemaFile = targetProjectDir.newFile('schema.json')
        targetJsonFile = targetProjectDir.newFile('target.json')
    }

    def "succeed on valid json"() {
        given:
        jsonSchemaFile << getJsonSchema()
        targetJsonFile << getCorrectJson()
        when:
        def actual = GradleRunner.create()
                .withProjectDir(targetProjectDir.root)
                .withArguments(':validateCustomJson', '--stacktrace')
                .build()

        then:
        actual.task(":validateCustomJson").outcome == TaskOutcome.SUCCESS
    }

    def "fail on invalid json"() {
        given:
        jsonSchemaFile << getJsonSchema()
        targetJsonFile << getInvalidJson()
        when:
        def actual = GradleRunner.create()
                .withProjectDir(targetProjectDir.root)
                .withArguments(':validateCustomJson', '--stacktrace')
                .buildAndFail()

        then:
        println(actual.output)
        actual.task(":validateCustomJson").outcome == TaskOutcome.FAILED
    }

    def getCorrectJson() {
        '''
            {
                "id": 1,
                "name": "A green door",
                "price": 12.50,
                "tags": ["home", "green"]
            }
        '''
    }

    def getInvalidJson() {
        '''
            {
                "name": 1,
                "price": 12.50,
                "tags": ["home", "green"]
            }
        '''
    }

    def getJsonSchema() {
        '''
            {
                "$schema": "http://json-schema.org/draft-04/schema#",
                "title": "Product",
                "description": "A product from Acme's catalog",
                "type": "object",
                "properties": {
                    "id": {
                        "description": "The unique identifier for a product",
                        "type": "integer"
                    },
                    "name": {
                        "description": "Name of the person",
                        "type": "string"
                    }
                },
                "required": ["id"]
            }
        '''
    }
/*
    This is needed to get the current plugin to the classpath. See https://docs.gradle.org/current/userguide/test_kit.html
     */
    def sourceCodeOfThisPluginClasspath() {
        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspathResource.readLines()
                .collect { it.replace('\\', '\\\\') } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(", ")
    }
}
