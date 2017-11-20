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

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.HTTPBuilder
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.HTTPBuilder
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.HTTPBuilder
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.HTTPBuilder
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.sf.json.JSON

def cli = new CliBuilder()
cli.eshost(args: 1, argName: 'host', 'elasticsearch host. default localhost.')
cli.esport(args: 1, argName: 'port', 'elasticsearch port. default 9200')
cli.esindex(args: 1, argName: 'index', 'elasticsearch index. default hesperides')
cli.backup(args: 1, argName: 'path', 'file path of the backup. Ex: ./backup.json, /home/dir/backup.json. default dump.json')
cli.dry('dry run')
cli.h('help')

def options = cli.parse(args)
if (options.h) {
    cli.usage()
    return
}


def eshost = options.eshost ?: "localhost"
def esport = options.esport ?: "9200"
def esindex = options.esindex ?: "hesperides"
def backup = options.backup ?: "dump.json"

def http = new HTTPBuilder("http://${eshost}:${esport}")

def slurper = new JsonSlurper().parse(new File(backup))
slurper["hits"]["hits"].each { element ->
    def content = new JsonBuilder(element["_source"])
    println element['_type']
    println content

    if (options.dry) {
        println "dry run: post json to /$esindex/${element['_type']}/${element['_id']} with body $content"
    } else {
        http.request(Method.POST, JSON) { req ->
            uri.path = "/$esindex/${element['_type']}/${element['_id']}"
            requestContentType = ContentType.JSON
            body = content.toPrettyString()

            response.success = { resp ->
                println "Added new element ${element["_source"]._id}"
            }

            response.failure = { resp ->
                println "Failed to import element ${element["id"]}"
                println resp.statusLine
                println resp.data
                if (resp.data == null) {
                    println resp.statusLine.reasonPhrase
                    println element._type
                    println element._source.name
                }
            }
        }
    }
}