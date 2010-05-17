package com.interact.listen.marshal.converter;

/**
 * Interface representing a mechanism for converting an {@code Object} to and from a {@code String}. Typically used when
 * marshalling/unmarshalling {@link Resource}s.
 */
public interface Converter
{
    /**
     * Converts the provided value to a {@code String}.
     * 
     * @param value value to marshal
     * @return {@code String} representation of provided value
     */
    public String marshal(Object value);

    /**
     * Converts the provided {@code String} to its {@code Object} value. This method should cast the unmarshalled value
     * to the type being converted to.
     * 
     * @param value value to ummarshal
     * @return {@code Object} instance for unmarshalled value
     * @throws ConversionException if an error occurs unmarshalling the value into the {@code Object} type
     */
    public Object unmarshal(String value) throws ConversionException;
}
