# gradle-json-validator

[![Build Status](https://travis-ci.org/alenkacz/gradle-json-validator.svg)](https://travis-ci.org/alenkacz/gradle-json-validator) [ ![Download](https://api.bintray.com/packages/alenkacz/maven/gradle-json-validator/images/download.svg) ](https://bintray.com/alenkacz/maven/gradle-json-validator/_latestVersion)

Provides json validation as a part of your gradle build pipeline.

This plugin implements a custom task type, [ValidateJsonTask](https://github.com/alenkacz/gradle-json-validator/blob/master/src/main/groovy/cz/alenkacz/gradle/jsonvalidator/ValidateJsonTask.groovy). This task expects two properties - *targetJsonFile* and *jsonSchemaFile*. If you need to validate more jsons as a part of one build, you will have to create as many custom tasks as the number of json schema files (see *validateCustomJson* in the example below).

Usage
====================

	buildscript {
		repositories {
			jcenter()
		}
		dependencies {
			classpath 'cz.alenkacz.gradle:json-validator:FILL_VERSION_HERE'
		}
	}

	apply plugin: 'json-validator'
	
    import cz.alenkacz.gradle.jsonvalidator.ValidateJsonTask
    
    task validateCustomJson(type: ValidateJsonTask) {
      targetJsonFile = file("target.json") // only one of targetJsonFile or targetJsonDirectory can be specified 
      targetJsonDirectory = file("directoryWithJsons") // only one of targetJsonFile or targetJsonDirectory can be specified
      jsonSchema = file("schema.json")
    }

JSON schema syntax check
====================
For some build pipelines it might be useful to be able to check schema files for syntax errors as a part of the build. To make that work, use the following setup and run the task **validateJsonSchema**:


	buildscript {
		repositories {
			jcenter()
		}
		dependencies {
			classpath 'cz.alenkacz.gradle:json-validator:FILL_VERSION_HERE'
		}
	}

	apply plugin: 'json-validator'
	
    jsonSchema {
        schemaFolder = PATH_TO_YOUR_FOLDER_WITH_JSON_SCHEMAS
    }
