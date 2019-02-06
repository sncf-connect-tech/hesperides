Feature: Get file

  Background:
    Given an authenticated user

  Scenario: get file with valued properties
    Given an existing module with this template content
      """
      {{ simple-property }}
      {{ another-property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name             | value        |
      | simple-property  | first-value  |
      | another-property | second-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      first-value
      second-value
      """

  Scenario: get file with default values
    Given an existing module with this template content
      """
      {{ default-property | @default default-value }}
      {{ default-overwritten-value | @default default-value }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name               | value       |
      | default-overwritten-value | overwritten-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      default-value
      overwritten-value
      """

  Scenario: get file with iterable and default values
    Given an existing module with this template content
      """
      {{#a}}
        {{ simple-property }}
        {{ default-property | @default default-value }}
        {{ default-with-value | @default default-value }}
      {{/a}}
      """
    And an existing platform with this module
    And the platform has these iterable properties
      | iterable | bloc   | name               | value     |
      | a        | bloc-1 | simple-property    | value-1   |
      | a        | bloc-2 | simple-property    | value-2   |
      | a        | bloc-1 | default-with-value | default-1 |
      | a        | bloc-2 | default-with-value | default-2 |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
        value-1
        default-value
        default-1
        value-2
        default-value
        default-2

      """

  Scenario: get file with global properties
    Given an existing module with this template content
      """
      {{ global-property }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      global-value
      """

  Scenario: get file with instance properties
    Given an existing module with this template content
      """
      {{ some-property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name          | value                   |
      | some-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value          |
      | instance-property | instance-value |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      instance-value
      """

  Scenario: get file with global properties used in property values
    Given an existing module with this template content
      """
      {{ some-property }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    And the platform has these valued properties
      | name          | value                 |
      | some-property | {{ global-property }} |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      global-value
      """

  Scenario: get file with global properties used in iterable properties
    Given an existing module with this template content
      """
      {{#a}}
        {{ global-property }}
        {{ will-be-replaced-by-global-value }}
      {{/a}}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    And the platform has these iterable properties
      | iterable | bloc   | name                             | value                 |
      | a        | bloc-1 | will-be-replaced-by-global-value | {{ global-property }} |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
        global-value
        global-value

      """

  #issue-453
  Scenario: get file with global properties used in instance property values
    Given an existing module with this template content
      """
      {{ some-property }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    And the platform has these valued properties
      | name          | value                   |
      | some-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value                 |
      | instance-property | {{ global-property }} |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      global-value
      """

  Scenario: get file with iterable-ception properties
    Given an existing module with this template content
      """
      {{#module-foo}}{{#module-bar}}{{module-foobar}}{{/module-bar}}{{/module-foo}}
      """
    And an existing platform with this module and iterable-ception
    When I get the module template file
    Then the file is successfully retrieved and contains
    """
    module-foobar-val-1module-foobar-val-2module-foobar-val-3module-foobar-val-4
    """

  #issue-457
  Scenario: get file with predefined properties
    Given an existing module with this template content
      """
      hesperides.application.name={{hesperides.application.name}}
      hesperides.application.version={{hesperides.application.version}}
      hesperides.platform.name={{hesperides.platform.name}}

      hesperides.module.name={{hesperides.module.name}}
      hesperides.module.version={{hesperides.module.version}}
      hesperides.module.path={{hesperides.module.path}}
      hesperides.module.path.full={{hesperides.module.path.full}}
      hesperides.module.path.0={{hesperides.module.path.0}}
      hesperides.module.path.1={{hesperides.module.path.1}}
      hesperides.module.path.2={{hesperides.module.path.2}}
      hesperides.module.path.3={{hesperides.module.path.3}}

      hesperides.instance.name={{hesperides.instance.name}}
      """
    And an existing platform with this module in logical group "a#b#c" and an instance
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      hesperides.application.name=test-application
      hesperides.application.version=1.0
      hesperides.platform.name=test-platform

      hesperides.module.name=test-module
      hesperides.module.version=1.0.0
      hesperides.module.path=
      hesperides.module.path.full=/a/b/c
      hesperides.module.path.0=a
      hesperides.module.path.1=b
      hesperides.module.path.2=c
      hesperides.module.path.3=

      hesperides.instance.name=instance-foo-1
      """

  #issue-457
  Scenario: get file with predefined properties without instance
    Given an existing module with this template content
      """
      hesperides.instance.name={{hesperides.instance.name}}
      """
    And an existing platform with this module
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      hesperides.instance.name=anything
      """

  #issue-467
  Scenario: get file with valued properties with the same name but a different comment
    Given an existing module with this template content
      """
      {{ a-property | some-comment }}
      {{ a-property | another-comment }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value   |
      | a-property | a-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      a-value
      a-value
      """

  #issue-476
  Scenario: get file with valued properties with the same name but different case
    Given an existing module with this template content
      """
      {{ a-property }}
      {{ A-PROPERTY }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value   |
      | a-property | a-value |
      | A-PROPERTY | A-VALUE |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      a-value
      A-VALUE
      """