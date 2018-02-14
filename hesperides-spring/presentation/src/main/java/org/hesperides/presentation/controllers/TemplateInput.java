package org.hesperides.presentation.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.hesperides.domain.modules.entities.Template;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"id", "name", "namespace", "filename", "location", "content", "rights"})
public class TemplateInput  {

    @NotNull
    private final String name;
    @NotNull
    private final String filename;
    @NotNull
    private final String location;
    @NotNull
    private final String content;
    @NotNull
    private final Template.TemplateRights rights;

    @JsonCreator
    public TemplateInput(
                    @JsonProperty("name") final String name,
                    @JsonProperty("filename") final String filename,
                    @JsonProperty("location") final String location,
                    @JsonProperty("content") final String content,
                    @JsonProperty("rights") final Template.TemplateRights rights
                    ) {
        this.name = name;
        this.location = location;
        this.filename = filename;
        this.content = content;
        this.rights = rights;
    }

    /*
    PUBLIC API
     */

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getFilename() {
        return filename;
    }

    public String getLocation() {
        return location;
    }

    public Template.TemplateRights getRights() {
        return rights;
    }

    public Template toDomainInstance() {
        return new Template( name, filename, location, content, rights);
    }
}
