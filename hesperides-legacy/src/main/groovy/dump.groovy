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
cli.output(args: 1, argName: 'path', 'file path of the backup. Ex: ./backup.json, /home/dir/backup.json. default dump.json')
cli.size(args: 1, argName: 'size', 'number of documents to dump. default 1000.')
cli.h('help')

def options = cli.parse(args)
if (options.h) {
    cli.usage()
    return
}


def eshost = options.eshost ?: "localhost"
def esport = options.esport ?: "9200"
def esindex = options.esindex ?: "hesperides"
def size = options.size ?: 1000
def output = options.output ?: "dump.json"

def http = new HTTPBuilder("http://${eshost}:${esport}")
http.request(Method.POST, ContentType.JSON) { req ->
    uri.path = "/$esindex/_search"
    requestContentType = ContentType.JSON
    body = [
            query: [
                    match_all: []
            ],
            size : size
    ]

    response.success = { resp, json ->
        println json
        def jsonPretty = new JsonBuilder(json).toPrettyString()
        println jsonPretty
        new File(output).write(jsonPretty)
    }

    response.failure = { resp ->
        println "Failed to dump elasticsearch on $eshost:$esport/$esindex with max $size elements on $output"
        println resp.statusLine
        println resp.data
    }
}