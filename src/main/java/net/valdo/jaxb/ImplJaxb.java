package net.valdo.jaxb;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.valdo.cfdi.Comprobante;

public class ImplJaxb {
	
	 public ImplJaxb() {
		// TODO Auto-generated constructor stub
	}

	public void xmlParse(final Path path) {
	     try {
	    	 
	    	 	//SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
	         	//Schema schema = sf.newSchema(new File("/home/javier/Documents/workspace-sts393/CFDi/cfdi33.xsd"));
	         	JAXBContext context = JAXBContext.newInstance(Comprobante.class);
        	
	        	File xmlFile = path.toAbsolutePath().toFile();
	        	
	        	System.out.println("<!---------------Generating the Java objects from XML Input-------------->");
				// UnMarshalling [Generate JAVA from XML]
				// Instantiate Unmarshaller via context
				Unmarshaller um = context.createUnmarshaller();
				//um.setSchema(schema);
				//um.setEventHandler(new MyValidationEventHandler());
				// Unmarshall the provided XML into an object
				Comprobante cfdi = (Comprobante) um.unmarshal(new FileReader(xmlFile));
				System.out.println("Serie :" + cfdi.getSerie());
				System.out.println("Folio :" + cfdi.getFolio());
            
	        } catch (JAXBException e) {
				// TODO Auto-generated catch block
	        	e.printStackTrace();
				System.out.println(e.getMessage());
	        } catch (final IOException e) {
	           System.out.println(e.getMessage());
	        } catch (Exception e) {
                System.out.println(e.getMessage());
            }
    }

}
