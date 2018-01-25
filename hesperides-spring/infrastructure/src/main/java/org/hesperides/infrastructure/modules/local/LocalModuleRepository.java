package org.hesperides.infrastructure.modules.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.hesperides.domain.ModuleSearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class LocalModuleRepository implements ModuleSearchRepository {

    private static final Map<String, String> MODULE_MAP = Maps.newHashMap();

    public List<String> getModulesNames() {
        return ImmutableList.copyOf(MODULE_MAP.values());
    }

}
