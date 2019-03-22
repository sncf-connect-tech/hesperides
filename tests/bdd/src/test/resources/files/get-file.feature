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

  Scenario: get file with property valorized with another valued property
    Given an existing module with this template content
      """
      {{ property-a }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value            |
      | property-a | {{ property-b }} |
      | property-b | FINAL VALUE      |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      FINAL VALUE
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
      {{#a}}{{ some-property | a comment}}{{/a}}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name          | value               |
      | some-property | {{ some-property }} |
    And the platform has these valued properties
      | name          | value               |
      | some-property | {{ some-property }} |
    And the platform has these iterable properties
      | iterable | bloc   | name          | value               |
      | a        | bloc-1 | some-property | {{ some-property }} |
    And the platform has these instance properties
      | name          | value          |
      | some-property | instance-value |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      instance-value
      instance-value
      """

  Scenario: global properties override instance ones
    Given an existing module with this template content
      """
      {{ instance.user.home }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name               | value                    |
      | instance.user.home | {{ instance.user.home }} |
    And the platform has these instance properties
      | name               | value |
      | instance.user.home |       |
    And the platform has these global properties
      | name               | value      |
      | instance.user.home | /home/toto |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      /home/toto
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

  #issue-547 Les propriétés itérables ne sont pas écrasées par les propriétés globales
  # Note (Lucas 2019/03/22): dans le legacy "GLOBAL_VALUE_B" n'apparait pas dans le fichier final
  Scenario: an iterable property is not overridden by a global property with the same name if it's valorized
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
        GLOBAL_VALUE_B
        value-2

      """

  Scenario: get file with global and instance properties used in and by iterable properties
    Given an existing module with this template content
      """
      {{#a}}
        {{ global-property }}
        {{ instance-property }}
        {{ hesperides.application.name }}
        {{ will-be-replaced-by-global-value }}
        {{ will-be-replaced-by-instance-value }}
        {{ will-be-replaced-by-predefined-value }}
      {{/a}}
      {{ property }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    And the platform has these valued properties
      | name     | value                   |
      | property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value          |
      | instance-property | instance-value |
    And the platform has these iterable properties
      | iterable | bloc   | name                                 | value                             |
      | a        | bloc-1 | will-be-replaced-by-global-value     | {{ global-property }}             |
      | a        | bloc-1 | will-be-replaced-by-instance-value   | {{ instance-property }}           |
      | a        | bloc-1 | will-be-replaced-by-predefined-value | {{ hesperides.application.name }} |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
        global-value
        instance-value
        test-application
        global-value
        instance-value
        test-application
      instance-value
      """

  Scenario: get file with a property with the same name but 2 different default values
    Given an existing module with this template content
    """
    {{ simple-property | @default 10}}
    {{ simple-property | @default 5}}
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

  Scenario: predefined properties override instance properties
    Given an existing module with this template content
      """
      {{ property-a }}
      {{ property-b }}
      {{ hesperides.module.path.full }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value                             |
      | property-a | {{ hesperides.application.name }} |
      | property-b | {{ hesperides.module.path.full }} |
    And the platform has these instance properties
      | name                        | value |
      | hesperides.application.name | PROUT |
      | hesperides.module.path.full |       |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      test-application
      /GROUP
      /GROUP
      """

  Scenario: get file with an iterable property with the same name but 2 different default values
    Given an existing module with this template content
    """
    {{#a}}
    {{ simple-property | @default 10}}
    {{ simple-property | @default 5 }}
    {{/a}}
    """
    And an existing platform with this module
    And the platform has these iterable properties
      | iterable | bloc   | name | value |
      | a        | bloc-1 |      |       |
    When I get the module template file
    Then the file is successfully retrieved and contains
    """
    10
    5

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

  Scenario: get file with an iterable-ception
    Given an existing module with this template content
    """
    {{#a}}
      {{#b}}
        {{#c}}
          {{valued_in_a}}-{{valued_in_b}}-{{valued_in_c}}-{{valued_in_d}}
        {{/c}}
      {{/b}}
      {{#d}}
        {{valued_in_a}}-{{valued_in_b}}-{{valued_in_c}}-{{valued_in_d}}
      {{/d}}
    {{/a}}
    """
    And an existing platform with this module
    And the platform has iterable-ception
    When I get the module template file
    Then the file is successfully retrieved and contains
    """
          value_a-value_b-value_c-
        value_a---value_d

    """

  Scenario: an module property refering to an empty instance property and another module property that have been removed from the template should not take the module value
    Given an existing module with this template content
    """
    {{#a}}
    {{ it-property }}
    {{/a}}
    {{ property }}
    """
    And an existing platform with this module
    And the platform has these valued properties
      | name              | value                 |
      | instance-property | module-value          |
      | property          | {{instance-property}} |
    And the platform has these iterable properties
      | iterable | bloc   | name        | value                 |
      | a        | bloc-1 | it-property | {{instance-property}} |
    And the platform has these instance properties
      | name              | value |
      | instance-property |       |
    When I get the instance template file
    Then the file is successfully retrieved and contains
    """
    module-value
    module-value
    """

  Scenario: cross-referencing properties should not crash the application
    Given an existing module with this template content
      """
      {{ property-a }}
      {{ property-b }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name     | value          |
      | property-a | {{ property-b }} |
      | property-b | {{ property-a }} |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      {{ property-b }}
      {{ property-a }}
      """