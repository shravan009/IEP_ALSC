/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.bean;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * @author David Heitzer
 * 
 * This class is a generic XML validator that can be used
 * to validate any XML payload against any XSD schema file
 * that resides on the classpath.
 * 
 * This class maintains a map of Schema objects to .xsd 
 * schema files to allow for completely dynamic validation
 * while avoiding instantiating a new Schema object for 
 * each request. 
 */
public class XmlValidator {

	// Not a thread-safe object, so should only be used in a synchronized method/block
	private final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	// This Map stores instantiated Schema values using schemaFilename Strings
	private final Map<String, Schema> schemaMap = new HashMap<String, Schema>();

	/*
	 * This method accepts and XML message along with a corresponding
	 * schema file path as parameters to complete validation.  The 
	 * schema file path be located on the classpath so that it can be
	 * resolved.  This method uses this schemaFilePath variable as a 
	 * key in the map to retrieve/store the Schema object.
	 */
	public void validate(final String xmlMessage, final String schemaFilePath) throws Exception, SAXException {
		final Schema schema = this.getSchema(schemaFilePath);
		// Validator object is not thread-safe, so we need one for each request
		final Validator validator = schema.newValidator();
		try {
			validator.validate(new StreamSource(new StringReader(xmlMessage)));
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}

	/*
	 * This method retrieves a stored Schema object from the schemaMap
	 * or instantiates a new one based on the schemaFilePath passed in.  
	 * This method is synchronized to prevent a race-condition on the 
	 * schemaMap.
	 */
	private synchronized Schema getSchema(final String schemaFilePath) throws SAXException {
		Schema schema = this.schemaMap.get(schemaFilePath);
		if (schema == null) {
			final URL url = XmlValidator.class.getClassLoader().getResource(schemaFilePath);
			schema = this.schemaFactory.newSchema(url);
			this.schemaMap.put(schemaFilePath, schema);
		}
		return schema;
	}

}
