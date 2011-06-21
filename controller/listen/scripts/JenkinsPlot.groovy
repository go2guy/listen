/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Gant script which generates stats for a Grails project.
 * 
 * @author Glen Smith
 *
 */
includeTargets << grailsScript("_GrailsSettings")

target ('default': "Generates basic stats for a Grails project") {

    outputDir = "${basedir}/target/jenkins-plot"
    ant.mkdir(dir: outputDir)

    def pathToInfo = [
        new Expando(name: "Controllers",       shortname: 'controllers',      filetype: ".groovy", path: "controllers"),
        new Expando(name: "Domain Classes",    shortname: 'domainclasses',    filetype: ".groovy", path: "domain"),
        new Expando(name: "Jobs",              shortname: 'jobs',             filetype: ".groovy", path: "jobs"),
        new Expando(name: "Services",          shortname: 'services',         filetype: ".groovy", path: "services"),
        new Expando(name: "Tag Libraries",     shortname: 'taglibraries',     filetype: ".groovy", path: "taglib"),
        new Expando(name: "Groovy Helpers",    shortname: 'groovyhelpers',    filetype: ".groovy", path: "src.groovy"),
        new Expando(name: "Java Helpers",      shortname: 'javahelpers',      filetype: ".java",   path: "src.java"),
        new Expando(name: "Unit Tests",        shortname: 'unittests',        filetype: ".groovy", path: "test.unit"),
        new Expando(name: "Integration Tests", shortname: 'integrationtests', filetype: ".groovy", path: "test.integration"),      
    ]
    
    
    new File(basedir).eachFileRecurse { file ->
        def match = pathToInfo.find { expando -> 
            file.path =~ expando.path && 
                file.path.endsWith(expando.filetype) 
        }
        if (match && file.isFile() ) {
                
            if (file.path.toLowerCase() =~ /web-inf/ || file.path.toLowerCase() =~ /plugins/) {
                // println "Skipping $file.path in WEB-INF or plugins dir"
            } else {
                match.filecount = match.filecount ? match.filecount+1 : 1
                // strip whitespace
                def loc = file.readLines().findAll { line -> !(line ==~ /^\s*$/) }.size()
                match.loc = match.loc ? match.loc + loc : loc
            }
        }
    }
        
        
    def totalFiles = 0
    def totalLOC = 0
        
    pathToInfo.each { info ->
                
        def thisfilecount = info.filecount ?: 0
        def thisloc = info.loc ?: 0
    
        def filename = "files-${info.shortname}.properties"
        new File(outputDir, filename).withWriter { out ->
            out.writeLine("YVALUE=${thisfilecount}")
        }
    
        filename = "lines-${info.shortname}.properties"
        new File(outputDir, filename).withWriter { out ->
            out.writeLine("YVALUE=${thisloc}")
        }
    
        totalFiles += thisfilecount
        totalLOC += thisloc
    }
    
    new File(outputDir, 'files-total.properties').withWriter { out ->
        out.writeLine("YVALUE=${totalFiles}")
    }
    
    new File(outputDir, 'lines-total.properties').withWriter { out ->
        out.writeLine("YVALUE=${totalLOC}")
    }
}
