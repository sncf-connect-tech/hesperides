package org.hesperides.core.application.files;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class FileUseCasesTest {

    private final FileUseCases fileUseCases;

    public FileUseCasesTest() {
        this.fileUseCases = new FileUseCases(null, null);
    }

    @Test
    public void getValuedTemplateContent_withoutSpacesTest() {

        //prepare
        StringBuilder template = new StringBuilder();
        template.append("key1=hard_coded_value ");
        template.append("simple_key={{value0}} ");
        template.append("required_key={{value1|@required}} ");
        template.append("key_with_comment={{value2|@comment \"some comment to explain the key\"}} ");
        template.append("key_with_default_value={{value3|@default \"false\"}} ");
        template.append("key_password={{value4|@password}} ");
        template.append("key_with_pattern={{value5|@pattern \"[a-z]*\"}} ");

        Map<String, String> propertiesKeyValueMap = new HashMap<>();
        propertiesKeyValueMap.put("value0", "Saturne");
        propertiesKeyValueMap.put("value1", "Mercure");
        propertiesKeyValueMap.put("value2", "Terre");
        propertiesKeyValueMap.put("value3", "Mars");
        propertiesKeyValueMap.put("value4", "Jupiter");
        propertiesKeyValueMap.put("value5", "Neptune");

        StringBuilder templateValued = new StringBuilder();
        templateValued.append("key1=hard_coded_value ");
        templateValued.append("simple_key=Saturne ");
        templateValued.append("required_key=Mercure ");
        templateValued.append("key_with_comment=Terre ");
        templateValued.append("key_with_default_value=Mars ");
        templateValued.append("key_password=Jupiter ");
        templateValued.append("key_with_pattern=Neptune ");

        //execute & test
        Assert.assertEquals(templateValued.toString(), fileUseCases.getValuedTemplateContent(template.toString(), propertiesKeyValueMap));
    }

    @Test
    public void getValuedTemplateContent_withSpacesTest() {

        //prepare
        StringBuilder template = new StringBuilder();
        template.append("key1=hard_coded_value ");
        template.append("required_key={{ value1|@required }} ");
        template.append("key_with_comment={{ value2|@comment \"some comment to explain the key\"}} ");
        template.append("key_with_default_value={{value3|@default \"false\" }} ");
        template.append("key_password={{value4|@password}} ");
        template.append("key_with_pattern={{ value5|@pattern \"[a-z]*\" }} ");
        template.append("key_with_spaces={{property with spaces inside}} ");
        template.append("key_with_dots={{http.proxy.name| Format : [protocol://][user:password@]proxyhost[:port] @contratpro.fr [chaine] @admin sysadmin}} ");
        template.append("key_prop_Name_At_The_end={{ Format : [protocol://][user:password@]proxyhost[:port] @contratpro.fr [chaine] @admin sysadmin | end property}}");

        Map<String, String> propertiesKeyValueMap = new HashMap<>();
        propertiesKeyValueMap.put("value1", "Mercure");
        propertiesKeyValueMap.put("value2", "Terre");
        propertiesKeyValueMap.put("value3", "Mars");
        propertiesKeyValueMap.put("value4", "Jupiter");
        propertiesKeyValueMap.put("value5", "Neptune");
        propertiesKeyValueMap.put("property with spaces inside", "les espaces des spaces");
        propertiesKeyValueMap.put("http.proxy.name", "http://user:password@proxyhost:port");
        propertiesKeyValueMap.put("end property", "property à la fin de Mustache");

        StringBuilder templateValued = new StringBuilder();
        templateValued.append("key1=hard_coded_value ");
        templateValued.append("required_key=Mercure ");
        templateValued.append("key_with_comment=Terre ");
        templateValued.append("key_with_default_value=Mars ");
        templateValued.append("key_password=Jupiter ");
        templateValued.append("key_with_pattern=Neptune ");
        templateValued.append("key_with_spaces=les espaces des spaces ");
        templateValued.append("key_with_dots=http://user:password@proxyhost:port ");
        templateValued.append("key_prop_Name_At_The_end=property à la fin de Mustache");

        //execute & test
        Assert.assertEquals(templateValued.toString(), fileUseCases.getValuedTemplateContent(template.toString(), propertiesKeyValueMap));
    }
}
