package com.interact.listen.api;

import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.BadRequestServletException;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetPhoneNumberServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final String DELIMITER = ";";
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        String number = request.getParameter("phoneNumber");
        if(number == null)
        {
            throw new BadRequestServletException("Missing required parameter [phoneNumber]");
        }

        Set<String> validProtocols = new HashSet<String>();
        validProtocols.add("PSTN");
        validProtocols.add("VOIP");

        if(!number.contains(DELIMITER) || number.split(DELIMITER).length != 2 || !validProtocols.contains(number.split(DELIMITER)[0]))
        {
            throw new BadRequestServletException("Property [phoneNumber] must be in the format 'PROTOCOL;NUMBER', "
                                                 + "where PROTOCOL = [PSTN|VOIP]");
        }

        Configuration.set(Property.Key.PHONE_NUMBER, number);
    }
}
