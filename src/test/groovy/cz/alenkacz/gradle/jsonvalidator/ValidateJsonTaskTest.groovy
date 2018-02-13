package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ValidateJsonTaskTest extends Specification  {
    @Rule final TemporaryFolder targetProjectDir = new TemporaryFolder()
    String pluginClasspath
    File gradleBuildFile
    File jsonSchemaFile
    File targetJsonFile
    File targetJsonFolder

    def setup() {
        gradleBuildFile = targetProjectDir.newFile('build.gradle')
        pluginClasspath = sourceCodeOfThisPluginClasspath()
        gradleBuildFile << getSingleFileGradleFile()

        jsonSchemaFile = targetProjectDir.newFile('schema.json')
        targetJsonFile = targetProjectDir.newFile('target.json')

        targetJsonFolder = targetProjectDir.newFolder("json")
        final FileTreeBuilder treeBuilder = new FileTreeBuilder(targetJsonFolder)
        treeBuilder.file("test.json", getInvalidJson())
        treeBuilder.file("test2.json", getInvalidJson())
    }

    def "succeed on valid json"() {
        given:
        gradleBuildFile.text = getSingleFileGradleFile()
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
        gradleBuildFile.text = getSingleFileGradleFile()
        jsonSchemaFile << getJsonSchema()
        targetJsonFile << getInvalidJson()
        when:
        def actual = GradleRunner.create()
                .withProjectDir(targetProjectDir.root)
                .withArguments(':validateCustomJson', '--stacktrace')
                .buildAndFail()

        then:
        actual.output.contains("One or more validation errors found")
        actual.task(":validateCustomJson").outcome == TaskOutcome.FAILED
    }

    def "accept also folder with json files and fail on invalid file in that folder"() {
        given:
        gradleBuildFile.text = getFolderGradleFile()
        jsonSchemaFile << getJsonSchema()

        when:
        def actual = GradleRunner.create()
                .withProjectDir(targetProjectDir.root)
                .withArguments(':validateCustomJson', '--stacktrace')
                .buildAndFail()

        then:
        actual.output.contains("One or more validation errors found")
        actual.output.contains("test.json")
        actual.output.contains("test2.json")
        actual.task(":validateCustomJson").outcome == TaskOutcome.FAILED
    }

    def "fail on non-json file"() {
        given:
        final FileTreeBuilder treeBuilder = new FileTreeBuilder(targetProjectDir.newFolder("non-json-folder"))
        treeBuilder.file("test.json", getInvalidJson())
        treeBuilder.file("nonjson.txt", "randomtext")
        gradleBuildFile.text = getFolderGradleFile("non-json-folder")
        jsonSchemaFile << getJsonSchema()

        when:
        def actual = GradleRunner.create()
                .withProjectDir(targetProjectDir.root)
                .withArguments(':validateCustomJson', '--stacktrace')
                .buildAndFail()

        then:
        actual.output.contains("File is not valid json")
        actual.output.contains("nonjson.txt")
        actual.task(":validateCustomJson").outcome == TaskOutcome.FAILED
    }

    def "not fail on non-json file when using onlyWithJsonExtension"() {
        given:
        final FileTreeBuilder treeBuilder = new FileTreeBuilder(targetProjectDir.newFolder("non-json-folder"))
        treeBuilder.file("test.json", getCorrectJson())
        treeBuilder.file("nonjson.txt", "randomtext")
        gradleBuildFile.text = getFolderGradleFile("non-json-folder", true)
        jsonSchemaFile << getJsonSchema()

        when:
        def actual = GradleRunner.create()
                .withProjectDir(targetProjectDir.root)
                .withArguments(':validateCustomJson', '--stacktrace')
                .build()

        then:
        print(actual.output)
        actual.task(":validateCustomJson").outcome == TaskOutcome.SUCCESS
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

    def getSingleFileGradleFile() {
        """
            buildscript {
                dependencies {
                    classpath files($pluginClasspath)
                }
            }
            apply plugin: 'cz.alenkacz.gradle.jsonvalidator'

            import cz.alenkacz.gradle.jsonvalidator.ValidateJsonTask

            task validateCustomJson(type: ValidateJsonTask) {
                targetJsonFile = file("target.json")
                jsonSchema = file("schema.json")
            }
        """
    }

    def getFolderGradleFile(folderName = "json", onlyWithJsonExtension = false) {
        """
            buildscript {
                dependencies {
                    classpath files($pluginClasspath)
                }
            }
            apply plugin: 'cz.alenkacz.gradle.jsonvalidator'

            import cz.alenkacz.gradle.jsonvalidator.ValidateJsonTask

            task validateCustomJson(type: ValidateJsonTask) {
                targetJsonDirectory = file("$folderName")
                jsonSchema = project.file("schema.json")
                onlyWithJsonExtension = ${onlyWithJsonExtension ? "true" : "false"}
            }
        """
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
