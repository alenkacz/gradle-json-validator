package cz.alenkacz.gradle.jsonvalidator

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class JsonValidatorPluginTest extends Specification {
    def "add tasks to the project"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.plugins.apply "cz.alenkacz.gradle.jsonvalidator"

        then:
        project.tasks.validateJson instanceof ValidateJsonTask
    }

    def "add tasks to the project for short plugin name"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.plugins.apply "json-validator"

        then:
        project.tasks.validateJson instanceof ValidateJsonTask
    }
}
