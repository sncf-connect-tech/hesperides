/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

#!/usr/bin/env groovy

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.URIBuilder

import java.nio.file.Files

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static java.lang.System.exit
import static java.lang.System.getenv

@GrabResolver(name='nexus', root='http://nexus.socrate.vsct.fr:50080/nexus')
@Grapes([
@Grab(group = 'org.apache.ant', module = 'ant-jsch', version = '1.9.4'),
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1'),
@Grab(group = 'org.eclipse.jetty', module = 'jetty-util', version = '9.2.2.v20140723'),
@GrabExclude('xerces:xercesImpl'),
@GrabConfig(systemClassLoader = true)
])

////////////////////////
// CLI configuration  //
////////////////////////

def cli = new CliBuilder(usage: './export.groovy [options]')
cli.width = 120
cli.ssh(longOpt: 'connexion-ssh', args: 1, argName: 'ssh connexion', 'connexion ssh to use (ex. user@host)', required: true)
cli.sshkey(longOpt: 'key-ssh', args: 1, argName: 'ssh key', "path to private ssh key. Ex. /path/to/.ssh/id_rsa . Default system property: SSH_KEY_PATH ")
cli.v(longOpt: 'verbose', 'verbose')
cli.hes(longOpt: 'hesperides-url', args: 1, argName: 'hesperides url', "Ex. http://daxmort:50000. Default system property: HESPERIDES_URL")
cli.application(args: 1, argName: 'application name', "ex. UNI.", required: true)
cli.version(args: 1, argName: 'version', "version of the application in hesperides (not the application number). Ex. 1.0", required: true)
cli.platform(args: 1, argName: 'platform', "version of the platform stored in hesperides. Ex. performance1", required: true)
cli.unit(args: 1, argName: 'unit', "name of the unit stored in hesperides. Ex. Realtime", required: true)
cli.instance(args: 1, argName: 'instance', "name of the instance stored in hesperides. Ex. UNIFIRE11REA", required: true)
cli.dry('dry run')
cli.h('help')

def options = cli.parse(args)

if (!options) exit 1
if (options.h) {
    cli.usage(); exit 0
}

// default options CLI
def ssh_connexion = (options.ssh ?: "visavsc@daftpunk.voyages-sncf.com")
def sshkey = (options.sshkey ?: getenv("SSH_KEY_PATH"))
def hesperidesUrl = (options.hes ?: getenv("HESPERIDES_URL"))

if (!sshkey && !hesperidesUrl) {
    println "one of argument are missing: key ($sshkey), hesperides url: ($hesperidesUrl) "
    exit 1
}

////////////////
// Main part  //
////////////////

// Variables
def http = new HTTPBuilder(hesperidesUrl)


println "Temporary files are wrote to ${System.getProperty('java.io.tmpdir')}"

/////////////
// Methods //
/////////////

/**
 * Closures for sending content to remote server
 */
def send_content = { File file, Closure closure -> closure.call(file) }
def send_content_to_remote_server = { String location, File file ->
    if (options.dry || options.v) {
        // log the contents of file
        println """
send to $ssh_connexion:$location :
${file.text}
"""
    }
    if (!options.dry) {
        def ant = new AntBuilder()
        ant.scp(file: file, verbose: options.v, todir: "$ssh_connexion:$location", keyfile: sshkey)
    }
}
def to_remote_target = { String location -> send_content_to_remote_server.curry(location) }

/**
 * Closures for retrieving content from hesperides with a given query path provided by first request to hesperides.
 */
def from_hesperides_with = { String queryPath ->
    def tmpFile = Files.createTempFile("hesperides-", "-tosend")
    tmpFile.toFile().deleteOnExit() // delete temporary files at the end of the script
    if (options.v) println "retrieve content from $hesperidesUrl/rest$queryPath"
    def builder = new URIBuilder("$hesperidesUrl/rest${queryPath.replaceAll(" ", "%20").replaceAll("#","%23")}")
    http.get(path: builder.path, contentType: TEXT, query: builder.query) { resp, reader ->
        tmpFile << reader
    }
    tmpFile.toFile()
}

//////////
// Main //
//////////

def hesperidesPathFiles = "/rest/files/${options.application}/${options.version}/${options.platform}/${options.unit}/${options.instance}"
println "http get on : $hesperidesUrl$hesperidesPathFiles"
http.get(path: hesperidesPathFiles,
        contentType: JSON) { resp, json ->
    json.each { target ->
        String target_path = target.location
        String query_path = target.url
        println "export to ${target_path} from $hesperidesUrl/rest${query_path}"

        send_content from_hesperides_with(query_path), to_remote_target(target_path)  // DSL rocks !

    }

}




