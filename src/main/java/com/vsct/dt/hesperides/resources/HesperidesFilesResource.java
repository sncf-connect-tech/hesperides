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
import com.vsct.dt.hesperides.files.Files;
import com.vsct.dt.hesperides.files.HesperidesFile;
import com.vsct.dt.hesperides.files.HesperidesFileRights;
import com.vsct.dt.hesperides.files.HesperidesRight;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/files")
@Consumes(MediaType.APPLICATION_JSON+ "; charset=utf-8")
@Api("/files")
public class HesperidesFilesResource extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HesperidesFilesResource.class);

    private final Files files;

    private final HesperidesModuleResource moduleResource;

    public HesperidesFilesResource(final Files files, final HesperidesModuleResource moduleResource) {
        this.files = files;
        this.moduleResource = moduleResource;
    }

    @Path("/applications/{application_name}/platforms/{platform_name}/{path}/{module_name}/{module_version}/instances/{instance_name}")
    @Produces(MediaType.APPLICATION_JSON+ "; charset=utf-8")
    @GET
    @Timed
    @ApiOperation("Get files list for given application, version, platefomr, unit and context")
    public Set<FileListItem> getFilesList(@Auth User user,
                                          @PathParam("application_name") final String applicationName,
                                          @PathParam("platform_name") final String platformName,
                                          @PathParam("path") final String path,
                                          @PathParam("module_name") final String moduleName,
                                          @PathParam("module_version") final String moduleVersion,
                                          @PathParam("instance_name") final String instanceName,
                                          @QueryParam("isWorkingCopy") final Boolean isWorkingCopy,
                                          @QueryParam("simulate") final Boolean simulate) throws Exception {

        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);
        checkQueryParameterNotEmpty("path", path);
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);
        checkQueryParameterNotEmpty("instance_name", instanceName);
        checkQueryParameterNotEmpty("isWorkingCopy", isWorkingCopy);

        Set<HesperidesFile> locations = files.getLocations(applicationName, platformName, path, moduleName, moduleVersion, isWorkingCopy, instanceName, simulate == null ? false : simulate);
        return locations.stream().map(file -> {
            String url = null;
            try {
                url = getContentLocation(applicationName, platformName, path, moduleName, moduleVersion, isWorkingCopy, instanceName, file.getTemplateName(), file.getTemplateNamespace());
            } catch (UnsupportedEncodingException e) {
                //Wrapping to allow exception to get out of the closure
                throw new RuntimeException(e);
            }

            return new FileListItem(file.getLocation()+"/"+file.getFilename(), url, convertRights(file.getRights()));
        }).collect(Collectors.toSet());
    }

    /**
     * Convert HesperidesFileRights to FileListItemRights
     * @param rights HesperidesFileRights
     * @return FileListItemRights
     */
    private static FileListItemRights convertRights(final HesperidesFileRights rights) {
        FileListItemRights flir = null;

        if (rights != null) {
            StringBuilder sb = new StringBuilder(3);

            String user = convertRight(rights.getUser(), sb);
            String group = convertRight(rights.getGroup(), sb);
            String other = convertRight(rights.getOther(), sb);

            flir = new FileListItemRights(user, group, other);
        }

        return flir;
    }

    /**
     * Convert HesperidesRight to String.
     *
     * @param currentRights right
     * @param sb string builder
     *
     * @return "rwx"
     */
    private static String convertRight(final HesperidesRight currentRights, final StringBuilder sb) {
        sb.setLength(0); // set length of buffer to 0
        sb.trimToSize(); // trim the underlying buffer

        if (currentRights != null) {
            convertSingleRight(currentRights.isRead(), 'r', sb);
            convertSingleRight(currentRights.isWrite(), 'w', sb);
            convertSingleRight(currentRights.isExecute(), 'x', sb);
        }

        return sb.toString();
    }

    /**
     * Convert a right into string.
     *
     * @param right right
     * @param flag char to set
     * @param sb string builder
     */
    private static void convertSingleRight(final Boolean right, final char flag, final StringBuilder sb) {
        if (right == null) {
            // Let default right
            sb.append(" ");
        } else if (right) {
            // Set right
            sb.append(flag);
        } else {
            // Remoe right
            sb.append("-");
        }
    }

    @Path("/applications/{application_name}/platforms/{platform_name}/{path}/{module_name}/{module_version}/instances/{instance_name}/{filename}")
    @Produces(MediaType.TEXT_PLAIN + "; charset=utf-8")
    @GET
    @Timed
    @ApiOperation("Get file for tempplate namespace and name, properties, context and name")
    public String getFile(@Auth User user,
                          @PathParam("application_name") final String applicationName,
                          @PathParam("platform_name") final String platformName,
                          @PathParam("path") final String path,
                          @PathParam("module_name") final String moduleName,
                          @PathParam("module_version") final String moduleVersion,
                          @PathParam("instance_name") final String instanceName,
                          @PathParam("filename") final String filename,
                          @QueryParam("isWorkingCopy") final Boolean isWorkingCopy,
                          @QueryParam("template_namespace") final String templateNamespace,
                          @QueryParam("simulate") final Boolean simulate) throws Exception {

        checkQueryParameterNotEmpty("application_name", applicationName);
        checkQueryParameterNotEmpty("platform_name", platformName);
        checkQueryParameterNotEmpty("path", path);
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);
        checkQueryParameterNotEmpty("instance_name", instanceName);
        checkQueryParameterNotEmpty("filename", filename);
        checkQueryParameterNotEmpty("isWorkingCopy", isWorkingCopy);
        checkQueryParameterNotEmpty("template_namespace", templateNamespace);

        HesperidesPropertiesModel model = !isWorkingCopy ? this.moduleResource.getReleaseModel(user, moduleName, moduleVersion) : this.moduleResource.getWorkingCopyModel(user, moduleName, moduleVersion);

        return files.getFile(applicationName, platformName, path, moduleName, moduleVersion, isWorkingCopy, instanceName, templateNamespace, filename, model, simulate == null ? false : simulate);
    }

    private String getContentLocation(final String applicationName,
                                      final String platformName,
                                      final String path,
                                      final String moduleName,
                                      final String moduleVersion,
                                      final boolean isWorkingCopy,
                                      final String instanceName,
                                      final String fileName,
                                      final String templateNamespace) throws UnsupportedEncodingException {
        return String.format("/rest/files/applications/%1$s/platforms/%2$s/%3$s/%4$s/%5$s/instances/%6$s/%7$s?isWorkingCopy=%8$s&template_namespace=%9$s",
                URLEncoder.encode(applicationName, "UTF-8").replace("+", "%20"),
                URLEncoder.encode(platformName, "UTF-8").replace("+", "%20"),
                URLEncoder.encode(path, "UTF-8").replace("+", "%20"),
                URLEncoder.encode(moduleName, "UTF-8").replace("+", "%20"),
                URLEncoder.encode(moduleVersion, "UTF-8").replace("+", "%20"),
                URLEncoder.encode(instanceName, "UTF-8").replace("+", "%20"),
                URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"),
                isWorkingCopy,
                URLEncoder.encode(templateNamespace, "UTF-8").replace("+", "%20")
        );
    }
}
