package com.interact.listen.marshal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.marshal.xml.XmlMarshaller;

import org.junit.Test;

public class MarshallerTest
{
    @Test
    public void test_createMarshaller_contentTypeNull_throwsMarshallerNotFoundExceptionWithMessage()
    {
        try
        {
            Marshaller.createMarshaller(null);
            fail("Expected MarshallerNotFoundException for null content type");
        }
        catch(MarshallerNotFoundException e)
        {
            assertEquals("Marshaller not found for content type [null]", e.getMessage());
        }
    }

    @Test
    public void test_createMarshaller_contentTypeStartsWithApplicationXml_returnsXmlMarshaller()
        throws MarshallerNotFoundException
    {
        Marshaller marshaller = Marshaller.createMarshaller("application/xml");
        assertTrue(marshaller instanceof XmlMarshaller);
    }

    @Test
    public void test_createMarshaller_contentTypeStartsWithApplicationJson_returnsJsonMarshaller()
        throws MarshallerNotFoundException
    {
        Marshaller marshaller = Marshaller.createMarshaller("application/json");
        assertTrue(marshaller instanceof JsonMarshaller);
    }

    @Test
    public void test_createMarshaller_contentTypeUnrecognized_throwsMarshallerNotFoundExceptionWithMessage()
        throws MarshallerNotFoundException
    {
        final String contentType = "turdferguson";

        try
        {
            Marshaller.createMarshaller(contentType);
            fail("Expected MarshallerNotFoundException for " + contentType + " content type");
        }
        catch(MarshallerNotFoundException e)
        {
            assertEquals("Marshaller not found for content type [" + contentType + "]", e.getMessage());
        }
    }
}
