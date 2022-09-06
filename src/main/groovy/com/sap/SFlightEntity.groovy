package com.sap

import io.micronaut.data.annotation.MappedEntity
import jakarta.persistence.Column
import jakarta.persistence.Id

@MappedEntity('SFLIGHT')
class SFlightEntity {
    @Id
    @Column(name = 'ID')
    Integer flightId

    @Column(name = 'FLIGHTNAME')
    String flightName

    @Column(name = 'FLIGHTFROM')
    String flightFrom

    @Column(name = 'FLIGHTTO')
    String flightTo

    @Column(name = 'FLIGHTDATE')
    Date flightDate
}
