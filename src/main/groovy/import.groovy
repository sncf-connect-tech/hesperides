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

@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.ContentType
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.ContentType
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.ContentType
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')
import groovyx.net.http.ContentType
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
@Grab(group = 'org.jyaml', module = 'jyaml', version = '1.3')

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.sf.json.JSON
import org.ho.yaml.Yaml

def cli = new CliBuilder()
cli.eshost(args: 1, argName: 'host', 'elasticsearch host')
cli.esport(args: 1, argName: 'port', 'elasticsearch port')
cli.esindex(args: 1, argName: 'index', 'elasticsearch index')
cli.yamls(args: 1, argName: 'directory', 'Location of YAML Files')
cli.schema_only('Import only the schema in ES ?"')
cli.h('help')

def options = cli.parse(args)
if (options.h) {
    cli.usage()
    return
}


def eshost = options.eshost ?: "localhost"
def esport = options.esport ?: "9200"
def esindex = options.esindex ?: "hesperides"


def http = new HTTPBuilder("http://${eshost}:${esport}")

if (!options.schema_only) {
    http.request(Method.DELETE, JSON) { req ->
        uri.path = "/${esindex}"

        // response handler for a success response code:
        response.success = { resp ->
            println "Index $esindex deleted"
        }

        // handler for any failure status code:
        response.failure = { resp ->
            println "Cannot delete index $esindex"
        }
    }




    http.request(Method.PUT, JSON) { req ->
        uri.path = "/$esindex"
        requestContentType = ContentType.JSON
        body = """{
   "settings": {
      "index": {
         "number_of_shards": 1,
         "number_of_replicas": 0
      },
      "analysis": {
         "analyzer": {
             "my_analyzer": {
                 "filter":"lowercase",
                 "tokenizer": "my_ngram_tokenizer"
             },
             "namespace_analyzer": {
                 "type":"custom",
                 "tokenizer": "hashtag",
                 "filter": ["lowercase","trim"]
             }
         },
         "tokenizer": {
            "my_ngram_tokenizer": {
               "type": "nGram",
               "min_gram": "2",
               "max_gram": "3",
               "token_chars": [
                  "letter",
                  "digit"
               ]
            },
            "hashtag": {"type": "pattern", "pattern": "#"}
         }
      }
   }
}
"""

        // response handler for a success response code:
        response.success = { resp ->
            println "Index $esindex created"
        }

        // handler for any failure status code:
        response.failure = { resp ->
            println "Cannot create index $esindex"
        }
    }
}

/* properties mapping will change */
http.request(Method.PUT, JSON) { req ->
    uri.path = "/$esindex/evaluatedproperties/_mapping"
    requestContentType = ContentType.JSON
    body = """
{
   "properties": {
      "hesnamespace": {
          "type":"multi_field",
          "fields":{
              "namespace":{
                "type": "string",
                "analyzer": "namespace_analyzer"
              },
              "untouched":{
                "type": "string",
                "index": "not_analyzed"
              }
          }
      }
   }
}
"""

    // response handler for a success response code:
    response.success = { resp ->
        println "Mapping for properties added"
    }

    // handler for any failure status code:
    response.failure = { resp, reader ->
        println "Cannot put the mapping for properties"
        println "${resp.statusLine.statusCode} ${resp.statusLine.reasonPhrase} ${reader.text}"
    }
}

/* templates mapping */
http.request(Method.PUT, JSON) { req ->
    uri.path = "/$esindex/templates/_mapping"
    requestContentType = ContentType.JSON
    body = """
{
   "properties": {
      "hesnamespace": {
          "type":"multi_field",
          "fields":{
              "namespace":{
                "type": "string",
                "analyzer": "namespace_analyzer"
              },
              "untouched":{
                "type": "string",
                "index": "not_analyzed"
              }
          }
      },
      "name": {
          "type":"multi_field",
          "fields":{
              "name":{
                "type": "string",
                "analyzer": "my_analyzer"
              },
              "untouched":{
                "type": "string",
                "index": "not_analyzed"
              }
          }
      }
   }
}
"""

    // response handler for a success response code:
    response.success = { resp ->
        println "Mapping for templates added"
    }

    // handler for any failure status code:
    response.failure = { resp, reader ->
        println "Cannot put the mapping for templates"
        println "${resp.statusLine.statusCode} ${resp.statusLine.reasonPhrase} ${reader.text}"
    }
}

/* Application mapping */
http.request(Method.PUT, JSON) { req ->
    uri.path = "/$esindex/vsctapplications/_mapping"
    requestContentType = ContentType.JSON
    body = """
{
   "properties": {
      "version": {
          "type":"multi_field",
          "fields":{
              "version":{
                "type": "string",
                "analyzer": "my_analyzer"
              },
              "untouched":{
                "type": "string",
                "index": "not_analyzed"
              }
          }
      },
      "name": {
          "type":"multi_field",
          "fields":{
              "name":{
                "type": "string",
                "analyzer": "my_analyzer"
              },
              "untouched":{
                "type": "string",
                "index": "not_analyzed"
              }
          }
      }
   }
}
"""

    // response handler for a success response code:
    response.success = { resp ->
        println "Mapping for applications added"
    }

    // handler for any failure status code:
    response.failure = { resp, reader ->
        println "Cannot put the mapping for applications"
        println "${resp.statusLine.statusCode} ${resp.statusLine.reasonPhrase} ${reader.text}"
    }
}

/* Contexts mapping */
http.request(Method.PUT, JSON) { req ->
    uri.path = "/$esindex/vsctcontexts/_mapping"
    requestContentType = ContentType.JSON
    body = """
{
   "properties": {
      "hesnamespace": {
          "type":"multi_field",
          "fields":{
              "namespace":{
                "type": "string",
                "analyzer": "namespace_analyzer"
              },
              "untouched":{
                "type": "string",
                "index": "not_analyzed"
              }
          }
      },
      "name": {
          "type":"multi_field",
          "fields":{
              "name":{
                "type": "string",
                "analyzer": "my_analyzer"
              },
              "untouched":{
                "type": "string",
                "index": "not_analyzed"
              }
          }
      }
   }
}
"""

    // response handler for a success response code:
    response.success = { resp ->
        println "Mapping for contexts added"
    }

    // handler for any failure status code:
    response.failure = { resp, reader ->
        println "Cannot put the mapping for contexts"
        println "${resp.statusLine.statusCode} ${resp.statusLine.reasonPhrase} ${reader.text}"
    }
}


if (!options.schema_only) {
    YamlImporter importer = new YamlImporter()
    def instances = new HashSet()
    File yamlsDir = new File((String) options.yamls)
    for (File yaml : yamlsDir.listFiles()) {
        if (yaml.exists() && yaml.isFile() && yaml.name.endsWith(".yaml")) {
            println yaml.name
            def env = Yaml.load(yaml)
            instances.addAll(importer.importYAML(env))
        }
    }

    int i = 0;
    instances.each { instance ->
        i++;
        http.request(Method.PUT, JSON) { req ->
            uri.path = "/$esindex/instance/$i"
            requestContentType = ContentType.JSON
            body = instance

            response.success = { resp ->
                println "Added new instance $i"
            }

            response.failure = { resp ->
                println "Failed to import instance $i"
                println resp.data
                if (resp.data == null) {
                    println resp.statusLine.reasonPhrase
                    println instance.type
                    println instance.name
                }
            }
        }
    }
}
