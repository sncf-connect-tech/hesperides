package org.hesperides.domain.technos.queries;


import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;

@Value
public class TechnoView {

    String name;
    String filename;
    String location;
    String content;
    @SerializedName("version_id")
    Long versionId;
    Template.Rights rights;
}
