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

/**
 * Created by william_montaz on 09/04/14.
 */

public class YamlImporter {

    def importYAML(def yaml) {
        def instances = new HashSet()
        instances.addAll(this.importAMQ(yaml["AMQ"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        instances.addAll(this.importATO(yaml["ATO"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        instances.addAll(this.importESB(yaml["ESB"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        instances.addAll(this.importHAP(yaml["HAP"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        instances.addAll(this.importHTTP(yaml["HTTP"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        instances.addAll(this.importSGBD(yaml["SGBD"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        instances.addAll(this.importTUX(yaml["TUX"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        instances.addAll(this.importWAS(yaml["WAS"], yaml["SGBD"], yaml["APPLICATION"], yaml["ENVIRONNEMENT"], yaml["DEF"]?.APPLICATION, yaml["DEF"]?.VERSION, yaml["DEF"]?.URL, yaml["CLIENT"]))
        return instances;
    }

    def importAMQ(def AMQ, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        AMQ?.each { amq ->
            amq.HOSTS?.each { host ->
                host.INSTANCES?.each { inst ->
                    def instance = [:]
                    instance.name = inst.NAME
                    instance.application = application
                    instance.platform = platform
                    instance.application_fullname = appfullname
                    instance.application_version = appversion.toString()
                    instance.application_url = appurl
                    instance.client = appclient
                    instance.type = "AMQ"
                    instance.hostname = host.NAME
                    instance.component = amq.NAME
                    instance.amq_type = inst.AMQ_TYPE
                    instance.ip = inst.IP
                    instance.queue = inst.QUEUE
                    instance.target_dir = inst.TARGETDIR
                    instance.logical_name = inst.LOGICAL_NAME
                    instance.discovery_url = inst.DISCOVERY_URL
                    instance.master = inst.MASTER

                    instance.user = amq.USER
                    instance.home = amq.HOME

                    instance.bins = importBins(amq.bins)
                    instance.ports = importPorts(inst.PORTS)
                    instance.links = importLinks(inst.USELINK)

                    instances << instance
                }

            }

        }

        return instances
    }

    def importATO(def ATO, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        ATO?.each { ato ->
            ato.HOSTS?.each { host ->
                def instance = [:]
                instance.name = ato.NAME
                instance.application = application
                instance.platform = platform
                instance.application_fullname = appfullname
                instance.application_version = appversion.toString()
                instance.application_url = appurl
                instance.client = appclient
                instance.type = "ATO"
                instance.home = ato.HOME
                instance.user = ato.USER
                instance.bins = importBins(ato.BINS)
                instance.ports = importPorts(ato.PORTS)
                instance.hostname = host.NAME
                instance.component = ato.NAME
                instances << instance
            }

        }
        return instances
    }

    def importESB(def ESB, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        ESB?.each { esb ->
            esb.HOSTS?.each { host ->
                host.INSTANCES?.each { inst ->
                    def instance = [:]
                    instance.name = inst.NAME
                    instance.application = application
                    instance.platform = platform
                    instance.application_fullname = appfullname
                    instance.application_version = appversion.toString()
                    instance.application_url = appurl
                    instance.client = appclient
                    instance.type = "ESB"
                    instance.component = esb.NAME

                    instance.ip = inst.IP
                    instance.ports = importPorts(inst.PORTS)
                    instance.log_level = inst.LOGLEVEL

                    instance.jvm_options = importJVMOptions(esb.JVM_OPTS_SURCHARGE)
                    if (inst.JVM_OPTS_SURCHARGE != null) instance.jvm_options = importJVMOptions(inst.JVM_OPTS_SURCHARGE)
                    instance.links = importLinks(inst.USELINK)

                    instance.hostname = host.NAME

                    instance.modules = []
                    esb.CONTEXTS?.each { m ->
                        def module = [:]
                        module.name = m.NAME
                        module.war = m.WAR
                        module.context = m.MODULE
                        instance.modules << module
                    }

                    instance.bins = importBins(esb.BINS)
                    instance.puppet_template_path = esb.PUPPET_TEMPLATE_PATH
                    instance.target_dir = esb.TARGETDIR
                    instance.user = esb.USER
                    instance.home = esb.HOME

                    instances << instance
                }
            }

        }

        return instances;

    }

    def importHAP(def HAP, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        HAP?.each { hap ->
            hap.HOSTS?.each { host ->
                host.INSTANCES?.each { inst ->
                    def instance = [:]
                    instance.name = inst.NAME;
                    instance.application = application;
                    instance.application_fullname = appfullname
                    instance.application_version = appversion.toString()
                    instance.application_url = appurl
                    instance.client = appclient
                    instance.type = "HAP";
                    instance.platform = platform;
                    instance.hostname = host.NAME;
                    instance.ip = inst.IP;
                    instance.target_dir = inst.TARGETDIR
                    instance.user = hap.USER
                    instance.home = hap.HOME
                    instance.ports = importPorts(inst.PORTS)
                    instance.links = importLinks(inst.USELINK)
                    instance.component = hap.NAME
                    instances << instance
                }
            }
        }
        return instances
    }

    def importHTTP(
            def HTTP, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        HTTP?.each { http ->
            http.HOSTS?.each { host ->
                host.INSTANCES?.each { inst ->
                    def instance = [:]
                    instance.name = inst.NAME
                    instance.application = application
                    instance.platform = platform
                    instance.application_fullname = appfullname
                    instance.application_version = appversion.toString()
                    instance.application_url = appurl
                    instance.client = appclient
                    instance.type = "HTTP"

                    instance.hostname = host.NAME
                    instance.ip = inst.IP

                    instance.nb_echec = inst.NB_ECHEC
                    instance.method_test = inst.METHOD_TEST
                    instance.target_dir = inst.TARGETDIR
                    instance.user = http.USER
                    instance.home = http.HOME

                    instance.component = http.NAME
                    instance.ports = importPorts(inst.PORTS)
                    instance.links = importLinks(inst.USELINK)
                    instances << instance
                }
            }
        }
        return instances
    }

    def importSGBD(
            def SGBD, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        SGBD?.each { sgbd ->
            sgbd.HOSTS?.each { host ->
                host.INSTANCES?.each { inst ->
                    inst.SCHEMAS?.each { schema ->
                        def instance = [:]
                        instance.type = "SGBD";
                        instance.application = application;
                        instance.platform = platform
                        instance.application_fullname = appfullname
                        instance.application_version = appversion.toString()
                        instance.application_url = appurl
                        instance.client = appclient
                        instance.user = sgbd.USER
                        instance.home = sgbd.HOME
                        instance.type_techno = sgbd.TYPE_TECHNO
                        instance.component = sgbd.NAME
                        instance.name = inst.NAME
                        instance.hostname = host.NAME
                        instance.fqdn = host.FQDN
                        instance.ports = importPorts(inst.PORTS)
                        instance.schema = schema.NAME
                        instance.login = schema.LOGIN
                        instance.password = schema.PASSWORD

                        instances << instance
                    }
                }
            }
        }
        return instances
    }

    def importTUX(def TUX, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        TUX?.each { tux ->
            tux.HOSTS?.each { host ->
                host.INSTANCES?.each { inst ->
                    def instance = [:]
                    instance.name = inst.NAME
                    instance.application = application
                    instance.platform = platform
                    instance.application_fullname = appfullname
                    instance.application_version = appversion.toString()
                    instance.application_url = appurl
                    instance.client = appclient
                    instance.type = "TUX"
                    instance.component = tux.NAME
                    instance.hostname = host.NAME
                    instance.ip = inst.IP
                    instance.ports = importPorts(inst.PORTS)

                    instances << instance
                }
            }
        }
        return instances
    }

    def importWAS(
            def WAS,
            def SGBD, def application, def platform, def appfullname, def appversion, def appurl, def appclient) {
        def instances = []
        WAS?.each { was ->
            was.HOSTS?.each { host ->
                host.INSTANCES?.each { inst ->
                    def instance = [:]
                    instance.type = "WAS";
                    instance.application = application;
                    instance.application_fullname = appfullname;
                    instance.application_version = appversion.toString()
                    instance.application_url = appurl;
                    instance.platform = platform;
                    instance.client = appclient;
                    instance.component = was.NAME;
                    instance.user = was.USER;
                    instance.home = was.HOME;
                    instance.targetdir = was.TARGETDIR
                    instance.puppet_template_version = was.PUPPET_TEMPLATE_VERION
                    instance.puppet_template_path = was.PUPPET_TEMPLATE_PATH
                    instance.puppet_master = was.PUPPET_MASTER
                    instance.multicast = was.MULTICAST
                    instance.consamq = was.CONSAMQ
                    instance.name = inst.NAME;
                    instance.hostname = host.NAME;
                    instance.ip = inst.IP;
                    instance.log_level = inst.LOGLEVEL
                    instance.bins = importBins(was.BINS)
                    instance.modules = []
                    was.MODULES?.each { m ->
                        def module = [:]
                        module.name = m.NAME
                        module.war = m.WAR
                        module.context = m.CONTEXT
                        instance.modules << module
                    }

                    if (inst.MODULES != null) {
                        /* Replace by the instance ones */
                        instance.modules = []
                        inst.MODULES.each { m ->
                            def module = [:]
                            module.name = m.NAME
                            module.war = m.WAR
                            module.context = m.CONTEXT
                            instance.modules << module
                        }
                    }

                    instance.ports = importPorts(inst.PORTS)

                    instance.links = []
                    was.JDBC?.each { jdbc ->
                        def link = [:]
                        link.type = "JDBC"
                        link.key = [:]
                        /* Find the SGBD instance matching schema */
                        def sgbdInstances = SGBD?.collect { it.HOSTS }.collectNested {
                            it.INSTANCES
                        }.flatten() //All SGBD instances
                        def mapInstanceToSchema = sgbdInstances?.collect({ Map sgbd ->
                            sgbd.SCHEMAS.collect({ Map schema ->
                                [instanceName: sgbd.NAME, schema: schema.NAME]
                            })
                        }).flatten()
                        link.key.name = mapInstanceToSchema.find({ tuple -> tuple.schema.equals(jdbc.SCHEMA) })?.instanceName

                        link.key.schema = jdbc.SCHEMA
                        link.configuration = [:]
                        link.configuration.name = jdbc.NAME
                        link.configuration.pool_min = jdbc.POOL.MIN.toString()
                        link.configuration.pool_max = jdbc.POOL.MAX.toString()
                        instance.links << link
                    }

                    host.JOLT?.HOSTS?.each { h ->
                        host.JOLT?.GATEWAY?.each { g ->
                            def link = [:]
                            link.type = "JOLT"
                            link.key = [:]
                            link.key.host = h
                            link.key.gateway = g
                            link.configuration = [:]
                            link.configuration.pool_min = host.JOLT.POOL.MIN.toString()
                            link.configuration.pool_max = host.JOLT.POOL.MAX.toString()
                            instance.links << link
                        }
                    }

                    //IMPORTANT TO MAKE JMS BEFORE USELINK BECAUSE WE WILL FIND JMS RFERENCES IN USELINK
                    was.JMS?.each { jms ->
                        def link = [:]
                        link.key = [:]
                        link.key.name = jms.NAME
                        link.key.instance = jms.INSTANCE
                        link.type = "JMS"

                        instance.links << link
                    }


                    inst.USELINK?.each { l ->
                        l.INSTANCES?.each { uselinkInstance ->
                            Map link = instance.links.find { existingLink -> existingLink.key.instance.equals(uselinkInstance) }
                            if (link != null) {
                                link.configuration = [:]
                                link.configuration.shortName = l.NAME
                                link.configuration.type = l.TYPE
                            } else {
                                link = [:]
                                link.type = l.TYPE
                                link.key = [:]
                                link.key.name = l.NAME
                                link.key.instance = uselinkInstance
                                link.key
                                link.configuration = [:]
                                instance.links << link
                            }
                        }
                    }

                    instance.jvm_options = importJVMOptions(was.JVM_OPTS_SURCHARGE)
                    if (inst.JVM_OPTS_SURCHARGE != null) instance.jvm_options = importJVMOptions(inst.JVM_OPTS_SURCHARGE)

                    instances << instance
                }
            }
        }

        return instances
    }


    def importLinks(def links) {
        def linksYaml = []
        links?.each { l ->
            l.INSTANCES.each { li ->
                def link = [:]
                link.type = l.TYPE
                link.key = [:]
                link.key.name = l.NAME
                link.key.instance = li
                link.key
                link.configuration = [:]
                linksYaml << link
            }
        }
        return linksYaml
    }

    def importBins(def bins) {
        def binsYaml = []
        bins?.each { b ->
            def bin = [:]
            bin.name = b.NAME
            bin.version = b.VERSION
            bin.home = b.HOME
            binsYaml << bin
        }
        return binsYaml
    }

    def importJVMOptions(def jvmOptions) {
        def jvmOptionsYaml = [:]
        if (jvmOptions != null) {
            jvmOptionsYaml.tuning = jvmOptions.TUNING
            jvmOptionsYaml.system = jvmOptions.SYSTEM
        }
        return jvmOptionsYaml
    }

    def importPorts(def ports) {
        def yamlPorts = []
        ports?.each { p ->
            def port = [:]
            port.name = p.NAME
            port.number = p.NUMBER
            yamlPorts << port
        }
        return yamlPorts;
    }


}