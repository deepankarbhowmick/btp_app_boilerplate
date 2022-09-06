package com.sap

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

@JdbcRepository(dialect = Dialect.ORACLE)
interface SFlightRepository extends CrudRepository<SFlightEntity, Integer> {
    //no need to define additional methods. The methods of standard API CrudRepository is sufficient for requirement.
}