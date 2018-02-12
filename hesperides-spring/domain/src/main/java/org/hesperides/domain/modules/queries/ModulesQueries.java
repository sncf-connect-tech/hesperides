package org.hesperides.domain.modules.queries;

import java.util.List;
import java.util.Optional;

public interface ModulesQueries {

    Optional<ModuleView> query(ModuleByIdQuery query);
    List<String> queryAllModuleNames(ModulesNamesQuery query);
    Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query);
}
