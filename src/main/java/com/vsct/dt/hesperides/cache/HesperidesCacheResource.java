package com.vsct.dt.hesperides.cache;

import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by emeric_martineau on 13/06/2016.
 */
@Path("/cache")
public class HesperidesCacheResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesCacheResource.class);

    private TemplatePackagesAggregate templatePackagesAggregate;
    private ModulesAggregate modulesAggregate;
    private ApplicationsAggregate applicationsAggregate;

    public HesperidesCacheResource(final TemplatePackagesAggregate templatePackagesAggregate,
                                   final ModulesAggregate modulesAggregate,
                                   final ApplicationsAggregate applicationsAggregate) {
        this.templatePackagesAggregate = templatePackagesAggregate;
        this.modulesAggregate = modulesAggregate;
        this.applicationsAggregate = applicationsAggregate;
    }

    @DELETE
    @Path("/application/{application_name}/{application_version}")
    public Response clearApplicationCache(@Auth final User user,
                                          @PathParam("application_name") final String applicationName,
                                          @PathParam("application_version") final String applicationVersion) {
        LOGGER.info("Remove application {}/{} from memory cache by {}.", applicationName, applicationVersion,
                user.getUsername());

        this.applicationsAggregate.removeFromCache(applicationName, applicationVersion);

        return Response.ok().build();
    }

    @DELETE
    @Path("/applications")
    public Response clearApplicationsCache(@Auth final User user) {
        LOGGER.info("Remove all application from memory cache by {}.", user.getUsername());

        this.applicationsAggregate.removeAllCache();

        return Response.ok().build();
    }

    @DELETE
    @Path("/module/{module_name}/{module_version}/workingcopy")
    public Response clearModuleWorkingcopyCache(@Auth final User user,
                                                @PathParam("module_name") final String moduleName,
                                                @PathParam("module_version") final String moduleVersion) {
        LOGGER.info("Remove module in workingcopy {}/{} from memory cache by {}.", moduleName, moduleVersion,
                user.getUsername());

        this.modulesAggregate.removeFromCache(moduleName, moduleVersion, true);

        return Response.ok().build();
    }

    @DELETE
    @Path("/modules")
    public Response clearModulesCache(@Auth final User user) {
        LOGGER.info("Remove all modules from memory cache by {}.", user.getUsername());

        this.modulesAggregate.removeAllCache();

        return Response.ok().build();
    }

    @DELETE
    @Path("/module/{module_name}/{module_version}/release")
    public Response clearModuleReleaseCache(@Auth final User user,
                                                @PathParam("module_name") final String moduleName,
                                                @PathParam("module_version") final String moduleVersion) {
        LOGGER.info("Remove module in release {}/{} from memory cache.", moduleName, moduleVersion);

        this.modulesAggregate.removeFromCache(moduleName, moduleVersion, false);

        return Response.ok().build();
    }

    @DELETE
    @Path("/template/package/{template_name}/{template_version}/workingcopy")
    public Response clearTemplatePackageWorkingcopyCache(@Auth final User user,
                                                @PathParam("template_name") final String templateName,
                                                @PathParam("template_version") final String templateVersion) {
        LOGGER.info("Remove template package in workingcopy {}/{} from memory cache by {}.",
                templateName, templateVersion, user.getUsername());

        this.templatePackagesAggregate.removeFromCache(templateName, templateVersion, true);

        return Response.ok().build();
    }

    @DELETE
    @Path("/template/package/{template_name}/{template_version}/release")
    public Response clearTemplatePackageReleaseCache(@Auth final User user,
                                                     @PathParam("template_name") final String templateName,
                                                     @PathParam("template_version") final String templateVersion) {
        LOGGER.info("Remove template package in release {}/{} from memory cache by {}.",
                templateName, templateVersion, user.getUsername());

        this.templatePackagesAggregate.removeFromCache(templateName, templateVersion, false);

        return Response.ok().build();
    }

    @DELETE
    @Path("/templates/packages")
    public Response clearTemplatesPackagesCache(@Auth final User user) {
        LOGGER.info("Remove templates packages from memory cache by {}.",
                user.getUsername());

        this.templatePackagesAggregate.removeAllCache();

        return Response.ok().build();
    }
}
