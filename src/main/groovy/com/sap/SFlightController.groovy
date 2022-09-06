package com.sap

import io.micronaut.core.annotation.Nullable
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.views.View
import jakarta.inject.Inject

import java.security.Principal

@Controller('/home')
class SFlightController {

    SFlightRepository sFlightRepository

    @Inject
    SFlightController(SFlightRepository sFlightRepository) {
        this.sFlightRepository = sFlightRepository

        //bootstrap some data in the sflight table, if the table is not having any records.
        if (this.sFlightRepository.findAll().isEmpty()) {
            this.sFlightRepository.save(new SFlightEntity(flightId: 1,
                    flightName: 'Air France',
                    flightFrom: 'Paris',
                    flightTo: 'Amsterdam',
                    flightDate: new Date()))
            this.sFlightRepository.save(new SFlightEntity(flightId: 2,
                    flightFrom: 'New Delhi',
                    flightName: 'Air India',
                    flightTo: 'Frankfurt',
                    flightDate: new Date()))
            this.sFlightRepository.save(new SFlightEntity(flightId: 3,
                    flightName: 'Delta',
                    flightFrom: 'Los Angeles',
                    flightTo: 'Heathrow',
                    flightDate: new Date()))
        }
    }

    @Get('/')
    @Secured([SecurityRule.IS_ANONYMOUS, 'ADMIN'])
    @View('home')
    //everyone can read the list of the flight.
    Map<String, Object> getAll(@Nullable Principal principal) {
        return ['flightList': this.sFlightRepository.findAll(), 'user': principal.getName()]
    }

    @Post(value = '/post', consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Secured('ADMIN')
    @View('action')
    //only admin can perform the operations.
    Map create(@Body SFlightEntity sFlightEntity) {
        try {
            this.sFlightRepository.save(sFlightEntity)
            return ['message': 'Entry created', 'sFlight': sFlightEntity]
        }
        catch (Exception exception) {
            return ['message': exception.getMessage(), 'sFlight': sFlightEntity]
        }
    }

    @Post(value = '/put/{id}', consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Secured('ADMIN')
    @View('action')
    //only admin can perform the operations.
    Map update(@Body SFlightEntity sFlightEntity, @PathVariable('id') String id) {
        try {
            this.sFlightRepository.update(sFlightEntity)
            return ['message': "${id} Entry updated", 'sFlight': sFlightEntity]
        }
        catch (Exception exception) {
            return ['message': exception.getMessage(), 'sFlight': sFlightEntity]
        }
    }

    @Post(value = '/delete/{id}', consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Secured('ADMIN')
    @View('action')
    //only admin can perform the operations.
    Map delete(@Body SFlightEntity sFlightEntity, @PathVariable('id') String id) {
        try {
            this.sFlightRepository.deleteById(sFlightEntity.getFlightId())
            return ['message': "${id} Entry deleted", 'sFlight': sFlightEntity]
        }
        catch (Exception exception) {
            return ['message': exception.getMessage(), 'sFlight': sFlightEntity]
        }
    }

    @Get('/operation/{+name}')
    @Secured('ADMIN')
    @View('action')
    //only admin can perform the operations.
    Map<String, Object> operation(@Nullable @PathVariable('name') String name) {
        String operationName = (name.split('/')[0] as String).toUpperCase() //operation name
        Integer id
        SFlightEntity sFlightEntity
        switch (operationName) {
            case 'POST':
                return ['sFlight': new SFlightEntity(), 'operation': name]
            case 'PUT':
            case 'DELETE':
                id = name.split('/')[1] as Integer
                sFlightEntity = this.sFlightRepository.findById(id).get()
                return ['sFlight': sFlightEntity, 'operation': name]
        }
    }
}
