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
      | name                      | value             |
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

  Scenario: get file with property valorized with another valued property twice
    Given an existing module with this template content
      """
      {{ property-a }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value            |
      | property-a | {{ property-b }} |
      | property-b | {{ property-c }} |
      | property-c | a-value          |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      a-value
      """

  Scenario: get file using a set delimiter
    Given an existing module with this template content
      """
      {{=<% %>=}}
      <%property-a%>
      ${{NOT_SUBSTITUTED}}
      <%={{ }}=%>
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value            |
      | property-a | {{ property-b }} |
      | property-b | a-value          |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """

      a-value
      ${{NOT_SUBSTITUTED}}

      """

  #issue-540
  Scenario: get file with property valorized with another valued property that has an annotation
    Given an existing module with this template content
      """
      {{ property-a | a comment }}
      {{ property-b }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value            |
      | property-a | a-value          |
      | property-b | {{ property-a }} |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      a-value
      a-value
      """

  Scenario: get file with instance properties created by a module property that references itself
    Given an existing module with this template content
      """
      {{ some-property | a comment}}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name          | value               |
      | some-property | {{ some-property }} |
    And the platform has these instance properties
      | name          | value          |
      | some-property | instance-value |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      instance-value
      """

  Scenario: get file with instance properties created by a module property that references itself and a global property with same name
    Given an existing module with this template content
      """
      {{ some-property | a comment}}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name          | value               |
      | some-property | {{ some-property }} |
    And the platform has these valued properties
      | name          | value               |
      | some-property | {{ some-property }} |
    And the platform has these instance properties
      | name          | value          |
      | some-property | instance-value |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      instance-value
      """

  Scenario: a global property overrides a valued one with the same name
    Given an existing module with this template content
      """
      {{ property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name     | value        |
      | property | MODULE_VALUE |
    And the platform has these global properties
      | name     | value        |
      | property | GLOBAL_VALUE |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      GLOBAL_VALUE
      """

  Scenario: a property referencing itself must disappear without any corresponding instance property defined
    Given an existing module with this template content
      """
      {{ property }}
      {{property}}
      {{property|@required}}
      {{property |@password}}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name     | value          |
      | property | {{ property }} |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """




      """

  Scenario: file from a template containing 2 properties with same name and comment but different mustache content
    Given an existing module with this template content
      """
    <logger level="{{logging.level|@comment "some comments" @default INFO}}">
    <logger level="{{logging.level|some comments @default INFO}}">
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name          | value |
      | logging.level | DEBUG |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
    <logger level="DEBUG">
    <logger level="DEBUG">
      """

  #issue-547
  Scenario: an iterable property is not overridden by a global property with the same name even if it is not valorized
    Given an existing module with this template content
      """
      {{#iterable-property}}
        {{ global-property-a }}
        {{ global-property-b }}
        {{ simple-property }}
      {{/iterable-property}}
      """
    And an existing platform with this module
    And the platform has these iterable properties
      | iterable          | bloc   | name              | value   |
      | iterable-property | bloc-1 | global-property-a | value-1 |
      | iterable-property | bloc-1 | simple-property   | value-2 |
    And the platform has these global properties
      | name              | value          |
      | global-property-a | GLOBAL_VALUE_A |
      | global-property-b | GLOBAL_VALUE_B |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
        value-1
        &nbsp;
        value-2

      """

  #issue-547
  Scenario: get file with global properties used by iterable properties
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
        &nbsp;
        global-value

      """

  Scenario: get file with a property with the same name but 2 different default values
    Given an existing module with this template content
    """
    {{ simple-property | @default 10}}
    {{ simple-property | @default 5 }}
    """
    And an existing platform with this module
    When I get the module template file
    Then the file is successfully retrieved and contains
    """
    10
    10
    """

  Scenario: property values are not trimmed
    Given an existing module with this template content
      """
      property:{{ property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name     | value       |
      | property | &nbsp;value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      property: value
      """
