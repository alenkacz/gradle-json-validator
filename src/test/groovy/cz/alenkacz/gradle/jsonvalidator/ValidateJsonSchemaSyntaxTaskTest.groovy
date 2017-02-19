package cz.alenkacz.gradle.jsonvalidator

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ValidateJsonSchemaSyntaxTaskTest extends Specification {
    def "say that the schema is valid"() {
        given:
        def schemaRoot = ProjectMother.validSchemaProject()
        def project = ProjectBuilder.builder().build()
        project.plugins.apply 'scala'
        project.plugins.apply 'json-validator'
        def extension = (PluginExtension) project.extensions.findByName('jsonSchema')
        extension.setSchemaFolder(schemaRoot.absolutePath)

        when:
        project.tasks.validateJsonSchema.validateSchema()

        then:
        noExceptionThrown()
    }

    def "say that the schema is invalid"() {
        given:
        def schemaRoot = ProjectMother.invalidSchemaProject()
        def project = ProjectBuilder.builder().build()
        project.plugins.apply 'scala'
        project.plugins.apply 'json-validator'
        def extension = (PluginExtension) project.extensions.findByName('jsonSchema')
        extension.setSchemaFolder(schemaRoot.absolutePath)

        when:
        project.tasks.validateJsonSchema.validateSchema()

        then:
        thrown JsonSchemaValidationException
    }
}
