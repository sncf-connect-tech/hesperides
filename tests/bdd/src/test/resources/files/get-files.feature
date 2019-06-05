Feature: Get instance or module files

  Background:
    Given an authenticated user

  Scenario: get files of an instance
    Given a techno template to create
    And an existing techno with this template
    And a module template to create
    And an existing module with this template and this techno
    And an existing platform with this module and an instance
    When I get the instance files
    Then the files are successfully retrieved

  Scenario: get files of a deployed module
    Given a techno template to create
    And an existing techno with this template
    And a module template to create
    And an existing module with this template and this techno
    And an existing platform with this module
    When I get the module files
    Then the files are successfully retrieved

  Scenario: get files of a deployed module that has multiple templates
    Given an existing module
    And a template to create with name "template-1" with filename "template-1.json" with location "/etc-1"
    And I add this template to the module
    And a template to create with name "template-2" with filename "template-2.json" with location "/etc-2"
    And I add this template to the module
    And an existing platform with this module
    When I get the module files
    Then the files are successfully retrieved

  Scenario: get files of an instance that doesn't exist
    Given an existing module
    And an existing platform with this module
    When I try to get the instance files
    Then the resource is not found

  Scenario: get files of a deployed module that doesn't exist
    Given an existing platform
    When I try to get the module files
    Then the resource is not found

  Scenario: get files with properties and global properties in filename and location
    Given a template to create with filename "{{ filename }}-{{ global-filename }}.json" with location "/{{ location }}-{{ global-location }}"
    And an existing module with this template
    And an existing platform with this module and global properties and filename and location values
    When I get the module files
    Then the files are successfully retrieved

  #issue-452
  Scenario: get files with global property used in filename and location's valued property
    Given a template to create with filename "{{ filename }}.json" with location "/{{ location }}"
    And an existing module with this template
    And an existing platform with this module and global properties and filename and location values
    When I get the module files
    Then the files are successfully retrieved

  #issue-452
  Scenario: get files with instance property used in filename and location
    Given a template to create with filename "{{ module-foo }}.json" with location "/{{ module-foo }}"
    And an existing module with this template
    And an existing platform with this module and an instance and instance properties
    When I get the instance files
    Then the files are successfully retrieved

  #issue-452
  Scenario: get files with global property used in instance property used in filename and location
    Given a template to create with filename "{{ module-foo }}.json" with location "/{{ module-foo }}"
    And an existing module with this template
    And an existing platform with this module and an instance and instance properties and global properties as instance values
    When I get the instance files
    Then the files are successfully retrieved

  Scenario: get files with module property using a similarly named instance property in location
    Given a template to create with location "{{ module-foobar }}"
    And an existing module with this template
    And an existing platform with this module and an instance and an instance value named "module-foobar"
    And the platform has instance properties with the same name as the module property that it's declared in
    When I get the instance files
    Then the files are successfully retrieved
    And their location contains no mustaches

  #issue-457
  Scenario: get files with predefined properties used in filename and location
    Given a template to create with filename "{{hesperides.application.name}}{{hesperides.application.version}}{{hesperides.platform.name}}{{hesperides.module.name}}{{hesperides.module.version}}{{hesperides.module.path.full}}{{hesperides.instance.name}}" with location "{{hesperides.application.name}}{{hesperides.application.version}}{{hesperides.platform.name}}{{hesperides.module.name}}{{hesperides.module.version}}{{hesperides.module.path.full}}{{hesperides.instance.name}}"
    And an existing module with this template
    And an existing platform with this module
    When I get the module files
    Then the files are successfully retrieved

  #issue-467
  Scenario: get files with properties with the same name but a different comment used in filename and location
    Given a template to create with filename "{{filename | some-comment}}{{filename | another-comment}}" with location "{{location | some-comment}}{{location | another-comment}}"
    And an existing module with this template
    And an existing platform with this module and filename and location values
    When I get the module files
    Then the file location is "etcetc/confconf"

  #issue-474
  Scenario: get files using predefined property module-path of a module deployed in multiple logical group of a platform
    Given a template to create with filename "{{hesperides.module.path.full}}"
    And an existing module with this template
    And an existing platform with this module in logical group "group-1"
    And I update this platform, adding this module in logical group "group-2"
    When I get the module files in the logical group "group-2"
    Then the files are successfully retrieved

  #issue-662
  Scenario: get files should not escape HTML in the location URLs
    Given a module template to create
    And an existing module with this template
    And an existing platform with this module
    When I try to get the module files
    Then the JSON output does not contain escaped characters
