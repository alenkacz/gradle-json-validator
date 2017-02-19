package cz.alenkacz.gradle.jsonvalidator

import java.nio.file.Files
import java.nio.file.Paths

class ProjectMother {
    static def File validSchemaProject() {
        def projectRoot = null
        File.createTempDir().with {
            deleteOnExit()
            def srcFile = Files.createFile(Paths.get(absoluteFile.absolutePath, "schema.json"))
            srcFile.write """{
"title": "Example Schema",
"type": "object",
"properties": {
"firstName": {
"type": "string"
},
"lastName": {
"type": "string"
},
"age": {
"description": "Age in years",
"type": "integer",
"minimum": 0
}
},
"required": ["firstName", "lastName"]
}""".stripMargin()
            projectRoot = absoluteFile
        }
        return projectRoot
    }

    static def File invalidSchemaProject() {
        def projectRoot = null
        File.createTempDir().with {
            deleteOnExit()
            def srcFile = Files.createFile(Paths.get(absoluteFile.absolutePath, "schema.json"))
            srcFile.write """{
"title": "Example Schema",
"type": "object",
"properties": {
"firstName": {
"type": 1
}
},
"required": ["firstName", "lastName"]
}""".stripMargin()
            projectRoot = absoluteFile
        }
        return projectRoot
    }
}
