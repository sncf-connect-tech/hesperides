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

package com.vsct.dt.hesperides.resources;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.api.client.ClientResponse;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.indexation.search.TemplateSearch;
import com.vsct.dt.hesperides.indexation.search.TemplateSearchResponse;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackages;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 11/07/14.
 */
@Path("/templates")
@Api("/templates")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesTemplateResource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesTemplateResource.class);

    private final TemplatePackages templatePackages;
    private final TemplateSearch   templateSearch;

    public HesperidesTemplateResource(final TemplatePackages templatePackages, final TemplateSearch templateSearch) {
        this.templatePackages = templatePackages;
        this.templateSearch = templateSearch;
    }

    @Path("/packages/{package_name}/{package_version}/workingcopy/templates")
    @GET
    @Timed
    @ApiOperation("Get all templates bundled in a package of a version workingcopy")
    public List<TemplateListItem> getAllTemplatesInWorkingCopy(@Auth final User user,
                                                               @PathParam("package_name") final String packageName,
                                                               @PathParam("package_version") final String packageVersion) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        TemplatePackageKey packageInfo = new TemplatePackageKey(
                packageName,
                WorkingCopy.of(packageVersion)
        );

        return templatePackages.getAllTemplates(packageInfo)
                .stream().map(template -> new TemplateListItem(template))
                .collect(Collectors.toList());

    }

    @Path("/packages/{package_name}/{package_version}/release/templates")
    @GET
    @Timed
    @ApiOperation("get all templates bundled in a package of a version release")
    public List<TemplateListItem> getAllTemplatesInRelease(@Auth final User user,
                                                           @PathParam("package_name") final String packageName,
                                                           @PathParam("package_version") final String packageVersion) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        TemplatePackageKey packageInfo = new TemplatePackageKey(
                packageName,
                Release.of(packageVersion)
        );

        return templatePackages.getAllTemplates(packageInfo)
                .stream().map(template -> new TemplateListItem(template))
                .collect(Collectors.toList());
    }

    @Path("/packages/{package_name}/{package_version}/workingcopy")
    @DELETE
    @Timed
    @ApiOperation("Delete a template package working copy")
    public Response deleteWorkingCopy(@Auth final User user,
                                      @PathParam("package_name") final String packageName,
                                      @PathParam("package_version") final String packageVersion) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        TemplatePackageKey packageInfo = new TemplatePackageKey(
                packageName,
                WorkingCopy.of(packageVersion)
        );
        templatePackages.delete(packageInfo);
        return Response.ok().build();
    }

    @Path("/packages/{package_name}/{package_version}/release")
    @DELETE
    @Timed
    @ApiOperation("Delete a template package release")
    public Response deleteRelease(@Auth final User user,
                                      @PathParam("package_name") final String packageName,
                                      @PathParam("package_version") final String packageVersion) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        TemplatePackageKey packageInfo = new TemplatePackageKey(
                packageName,
                Release.of(packageVersion)
        );
        templatePackages.delete(packageInfo);
        return Response.ok().build();
    }

    @Path("/packages/{package_name}/{package_version}/release/model")
    @GET
    @Timed
    @ApiOperation("get properties model for a release")
    public Response getReleaseModel(@Auth final User user,
                                    @PathParam("package_name") final String packageName,
                                    @PathParam("package_version") final String packageVersion) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        LOGGER.debug("Get properties model for package release {}/{}", packageName, packageVersion);
        HesperidesPropertiesModel model = templatePackages.getModel(packageName, packageVersion, false);
        return Response.status(200).entity(model).build();
    }

    @Path("/packages/{package_name}/{package_version}/workingcopy/model")
    @GET
    @Timed
    @ApiOperation("Get properties model for a workingcopy")
    public Response getWorkingCopyModel(@Auth final User user,
                                        @PathParam("package_name") final String packageName,
                                        @PathParam("package_version") final String packageVersion) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        LOGGER.debug("Get properties model for package workingcopy {}/{}", packageName, packageVersion);
        HesperidesPropertiesModel model = templatePackages.getModel(packageName, packageVersion, true);
        return Response.status(200).entity(model).build();
    }

    @Path("/packages/perform_search")
    @POST
    @Timed
    @ApiOperation("Get technos by tokens")
    public List<TemplatePackageKey> searchPackages(@Auth final User user, @QueryParam("terms") final String tokens) {
        checkQueryParameterNotEmpty("terms", tokens);

        String[] aTokens = tokens.split("#");
        String[] terms = new String[aTokens.length + 1];
        terms[0] = "packages";
        for (int i = 0; i < aTokens.length; i++) {
            terms[i + 1] = "*" + aTokens[i] + "*";
        }

        return templateSearch.getTemplatesByNamespaceLike(terms).stream()
                .collect(Collectors.groupingBy(TemplateSearchResponse::getNamespace))
                .keySet().stream()
                .map(namespace -> {
                    String[] splits = namespace.split("#");
                    return new TemplatePackageKey(splits[1], splits[2], WorkingCopy.is(splits[3]));
                }).sorted((technoA, technoB) -> {
                    int compareName = technoA.getName().compareTo(technoB.getName());
                    if (compareName != 0) {
                        return compareName;
                    }
                    else {
                        return technoA.getVersion().getVersionName().compareTo(technoB.getVersion().getVersionName());
                    }
                }).collect(Collectors.toList());
    }

    @Path("/packages/{package_name}/{package_version}/workingcopy/templates/{template_name}")
    @GET
    @Timed
    @ApiOperation("Get template bundled in a package for a version workingcopy")
    public Template getTemplateInWorkingCopy(@Auth final User user,
                                               @PathParam("package_name") final String packageName,
                                               @PathParam("package_version") final String packageVersion,
                                               @PathParam("template_name") final String templateName) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);
        checkQueryParameterNotEmpty("template_name", templateName);

        TemplatePackageKey packageInfo = new TemplatePackageKey(
                packageName,
                WorkingCopy.of(packageVersion)
        );

        Template template = templatePackages.getTemplate(packageInfo, templateName)
                .orElseThrow(() -> new MissingResourceException("Could not find template in working copy " + packageName + "/" + packageVersion + "/" + templateName));
        return template;
    }

    @Path("/packages/{package_name}/{package_version}/release/templates/{template_name}")
    @GET
    @Timed
    @ApiOperation("Get template bundled in a package for a version release")
    public Template getTemplateInRelease(@Auth final User user,
                                           @PathParam("package_name") final String packageName,
                                           @PathParam("package_version") final String packageVersion,
                                           @PathParam("template_name") final String templateName) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);
        checkQueryParameterNotEmpty("template_name", templateName);

        TemplatePackageKey packageInfo = new TemplatePackageKey(
                packageName,
                Release.of(packageVersion)
        );

        Template template = templatePackages.getTemplate(packageInfo, templateName)
                .orElseThrow(() -> new MissingResourceException("Could not find template in release " + packageName + "/" + packageVersion + "/" + templateName));
        return template;
    }

    @Path("/packages/{package_name}/{package_version}/workingcopy/templates")
    @PUT
    @Timed
    @ApiOperation("Update template in the workingcopy of a package")
    public Template updateTemplateInWorkingCopy(@Auth final User user,
                                                  @PathParam("package_name") final String packageName,
                                                  @PathParam("package_version") final String packageVersion,
                                                  final Template template) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey(
                packageName,
                packageVersion
        );

        TemplateData templateData = TemplateData.withTemplateName(template.getName())
                .withFilename(template.getFilename())
                .withLocation(template.getLocation())
                .withContent(template.getContent())
                .withRights(template.getRights())
                .withVersionID(template.getVersionID())
                .build();

        LOGGER.debug("update template workingcopy in {}", packageInfo);
        return templatePackages.updateTemplateInWorkingCopy(packageInfo, templateData);
    }

    @Path("/packages/{package_name}/{package_version}/workingcopy/templates")
    @POST
    @Timed
    @ApiOperation("Create template in the workingcopy of a package")
    public Response createTemplateInWorkingCopy(@Auth final User user,
                                                @PathParam("package_name") final String packageName,
                                                @PathParam("package_version") final String packageVersion,
                                                final Template template) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey(
                packageName,
                packageVersion
        );

        TemplateData templateData = TemplateData.withTemplateName(template.getName())
                .withFilename(template.getFilename())
                .withLocation(template.getLocation())
                .withContent(template.getContent())
                .withRights(template.getRights())
                .build();

        LOGGER.debug("create template in the workingcopy of {}", packageInfo);
        Template created = templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @Path("/packages")
    @POST
    @Timed
    @ApiOperation("Create a working copy from a release (to create a new working copy, just add a template")
    public Response createWorkingCopy(@Auth final User user,
                                      @QueryParam("from_package_name") final String fromPackageName,
                                      @QueryParam("from_package_version") final String fromPackageVersion,
                                      @QueryParam("from_is_working_copy") final Boolean isFromWorkingCopy,
                                      @Valid final TemplatePackageKey templatePackage) {
        checkQueryParameterNotEmpty("from_package_name", fromPackageName);
        checkQueryParameterNotEmpty("from_package_version", fromPackageVersion);
        checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

        TemplatePackageKey fromPackageInfo = new TemplatePackageKey(
                fromPackageName,
                new HesperidesVersion(fromPackageVersion, isFromWorkingCopy)
        );

        LOGGER.debug("create working copy {} from {}", templatePackage, fromPackageInfo);

        TemplatePackageWorkingCopyKey workingCopy = new TemplatePackageWorkingCopyKey(templatePackage.getName(), templatePackage.getVersion().getVersionName());

        TemplatePackageKey wc = templatePackages.createWorkingCopyFrom(workingCopy, fromPackageInfo);
        return Response.status(Response.Status.CREATED).entity(wc).build();
    }

    @Path("/packages/{package_name}/{package_version}/workingcopy/templates/{template_name}")
    @DELETE
    @Timed
    @ApiOperation("Delete template in the working copy of a version")
    public Response deleteTemplateInWorkingCopy(@Auth final User user,
                                                @PathParam("package_name") final String packageName,
                                                @PathParam("package_version") final String packageVersion,
                                                @PathParam("template_name") final String templateName) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);
        checkQueryParameterNotEmpty("template_name", templateName);

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey(
                packageName,
                packageVersion
        );

        templatePackages.deleteTemplateInWorkingCopy(packageInfo, templateName);
        return Response.status(ClientResponse.Status.OK).build();
    }

    @Path("/packages/create_release")
    @POST
    @Timed
    @ApiOperation("Create a release from an existing workingcopy")
    public Response createRelease(@Auth final User user,
                                  @QueryParam("package_name") final String packageName,
                                  @QueryParam("package_version") final String packageVersion) {
        checkQueryParameterNotEmpty("package_name", packageName);
        checkQueryParameterNotEmpty("package_version", packageVersion);

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey(
                packageName,
                packageVersion
        );

        TemplatePackageKey infos = templatePackages.createRelease(packageInfo);

        TemplatePackageKey release = new TemplatePackageKey(infos.getName(), infos.getVersion().getVersionName(), infos.getVersion().isWorkingCopy());
        return Response.status(Response.Status.CREATED).entity(release).build();
    }

}
