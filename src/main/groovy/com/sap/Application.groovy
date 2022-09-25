package com.sap

import io.micronaut.context.env.PropertySource
import io.micronaut.runtime.Micronaut
import groovy.transform.CompileStatic
import io.pivotal.cfenv.core.CfEnv
import io.pivotal.cfenv.core.CfService

@CompileStatic
class Application {
    static void main(String[] args) {
//        Micronaut.run(Application, args)
        String userName, passWord, url
        CfService cfService
        Micronaut micronaut = Micronaut.build(args)
        try {
            cfService = new CfEnv().findServiceByLabel('hana')
            passWord = cfService.getCredentials().getPassword()
            userName = cfService.getCredentials().getUsername()
            url = cfService.getCredentials().getMap().get('url')
            micronaut.propertySources(PropertySource.of(['datasources.default.url'            : url,
                                                         'datasources.default.driverClassName': 'com.sap.db.jdbc.Driver',
                                                         'datasources.default.username'       : userName,
                                                         'datasources.default.password'       : passWord,
                                                         'datasources.default.schema-generate': 'CREATE_DROP'] as Map<String, Object>)).start()
        }
        catch (Exception exception) {
            micronaut.propertySources(PropertySource.of(['datasources.default.url'            : 'jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE',
                                                         'datasources.default.driverClassName': 'org.h2.Driver',
                                                         'datasources.default.username'       : 'sa',
                                                         'datasources.default.password'       : '',
                                                         'datasources.default.schema-generate': 'CREATE_DROP',
                                                         'datasources.default.dialect'        : 'H2'] as Map<String, Object>)).start()
        }
    }
}
