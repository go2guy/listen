includeTargets << grailsScript("Init")
includeTargets << grailsScript('_GrailsClean')
includeTargets << grailsScript('_GrailsWar')

warName = null

target(executableWar: "Builds a self-executing war file") {
    depends(clean, war)

    configureWarName()

    ant.delete(dir: 'target/war-stage')
    ant.mkdir(dir: 'target/war-stage')
    ant.unwar(src: warName, dest: 'target/war-stage')

    if(argsMap.containsKey('instrument')) {
        ant.taskdef(classpathRef: 'grails.test.classpath', resource: 'tasks.properties')
        ant.'cobertura-instrument'(datafile: "cobertura.ser") {
            fileset(dir: 'target/war-stage') {
                include(name: "**/*.class")
            }
        }
    }

    ant.unjar(dest: 'target/war-stage') {
        patternset {
            exclude(name: 'META-INF/**/*')
            exclude(name: 'images/**/*')
            exclude(name: '.options')
            exclude(name: 'about.html')
            exclude(name: 'jdtCompilerAdapter.jar')
            exclude(name: 'plugin*')
        }
        fileset(dir: 'lib-jetty', includes: '*.jar')
    }
    ant.copy(todir: 'target/war-stage') {
        fileset(dir: 'target/classes', includes: 'com/interact/listen/server/EmbeddedJettyServer.class')
    }

    manifestFile = 'target/war-stage/META-INF/MANIFEST.MF'
    ant.manifest(file: manifestFile, mode: 'update') {
        attribute(name: 'Main-Class', value: 'com.interact.listen.server.EmbeddedJettyServer')
    }
    ant.jar(destfile: warName, basedir: 'target/war-stage', manifest: manifestFile)
    ant.delete(dir: 'target/war-stage')
}

setDefaultTarget(executableWar)
