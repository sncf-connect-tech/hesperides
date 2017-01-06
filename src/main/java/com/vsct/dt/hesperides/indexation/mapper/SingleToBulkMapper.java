package com.vsct.dt.hesperides.indexation.mapper;

/**
 * Created by jordan_kergoat on 10/10/2016.
 */
public final class SingleToBulkMapper {

    private final Integer id;
    private final String body;

    public SingleToBulkMapper(Integer id, String body) {
        this.id = id;
        this.body = body;
    }

    @Override
    public String toString() {
        String action = "{\"index\": {\"_id\": \"".concat(this.id.toString()).concat("\"}}\n");
        String body = this.body.concat("\n");
        return action.concat(body);
    }
}
