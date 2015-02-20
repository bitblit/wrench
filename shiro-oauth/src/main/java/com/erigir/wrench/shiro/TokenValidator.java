package com.erigir.wrench.shiro;


public interface TokenValidator {

    /**
     * Attempts to validate a ticket for the provided service.
     *
     * @param ticket the ticket to attempt to validate.
     * @param service the service this ticket is valid for.
     * @return an assertion from the ticket.
     * @throws TokenValidationException if the ticket cannot be validated.
     *
     */
    //Assertion validate(String ticket, String service) throws TicketValidationException;
}