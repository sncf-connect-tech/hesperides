package org.hesperides.application.modules;

import org.hesperides.domain.modules.commands.ModuleCommands;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.domain.modules.queries.ModuleQueries;
import org.hesperides.domain.security.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ModuleUseCasesTest.Config.class)
public class ModuleUseCasesTest {
    @TestConfiguration
    @ComponentScan
    static class Config {
    }

    @Autowired
    ModuleUseCases useCases;
    @MockBean
    ModuleQueries moduleQueries;
    @MockBean
    ModuleCommands moduleCommands;

    @Test(expected = DuplicateModuleException.class)
    public void createWorkingCopy_should_fail_when_working_copy_already_exists() {

        Module.Key key = new Module.Key("x", "1", Module.Type.workingcopy);
        Module module = new Module(key, Collections.emptyList(), Collections.emptyList(), 1L);

        given(moduleQueries.moduleExist(any())).willReturn(true);
        given(moduleCommands.createModule(any(), any())).willReturn(key);

        useCases.createWorkingCopy(module, new User("robert"));
    }

    @Test
    public void createWorkingCopy_should_pass_when_working_copy_do_not_exists() {

        Module.Key key = new Module.Key("x", "1", Module.Type.workingcopy);
        Module module = new Module(key, Collections.emptyList(), Collections.emptyList(), 1L);

        given(moduleQueries.moduleExist(any())).willReturn(false);
        given(moduleCommands.createModule(any(), any())).willReturn(key);

        useCases.createWorkingCopy(module, new User("robert"));
    }

}