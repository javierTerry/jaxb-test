package net.valdo.jaxb;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.input.CharSequenceReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.valdo.cfdi.Comprobante;
import net.valdo.cfdi.Comprobante.Complemento;
import net.valdo.cfdi.tfd.TimbreFiscalDigital;

public class ImplJaxb {
	
	Comprobante cfdi = null;
	TimbreFiscalDigital tfd = null;
	//Comprobante.Complemento complemento = null;
	 public ImplJaxb() {
		// TODO Auto-generated constructor stub
		 
	}

	public void xmlParse(final Path path) {
	     try {
	    	 
	         	JAXBContext context = JAXBContext.newInstance(Comprobante.class);
        	
	         	File xmlFile = path.toFile();
	        	
	        	System.out.println("<!---------------Generating the Java objects from XML Input-------------->");
				// UnMarshalling [Generate JAVA from XML]
				
				BOMInputStream inputStream = new BOMInputStream(FileUtils.openInputStream(xmlFile), false);
		        String fileContent;
		        if (inputStream.hasBOM()) {
		            fileContent = IOUtils.toString(inputStream, inputStream.getBOMCharsetName());
		        } else {
		            fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		        }
		        
		        
		        Reader xmlreader = new CharSequenceReader(fileContent);
				// Instantiate Unmarshaller via context
				Unmarshaller um = context.createUnmarshaller();

		        cfdi = (Comprobante) um.unmarshal(xmlreader);
		        //complemento = (Complemento) cfdi.getComplemento();
		        
		        
		        JAXBContext tfdContext = JAXBContext.newInstance(TimbreFiscalDigital.class);
		        //tfd = (TimbreFiscalDigital) tfdContext.createUnmarshaller().unmarshal( (Node) cfdi.getComplemento().get(0).getAny().get(0) );
		        /*
		        cfdi.getComplemento()
		        	.forEach(
		        		complemento -> {
		        			try {
		        				System.out.println(complemento);
		        			} catch ( Exception e ) {
		        				
		        			}
		        		});
		        
		        /*Element elment = (Element) cfdi.getComplemento().get(0).getAny().get(0);
		        System.out.println(elment.getLocalName());
		        //elment.getChildNodes().
		        NodeList list = elment.getChildNodes();
				for (int i=0; i<list.getLength(); i++) {
					System.out.println( list.item(i) );
				}
		        /*for (nodo : elment.getChildNodes()) {
		        	
		        }
		        */
		        for(Comprobante.Complemento complemento : cfdi.getComplemento()){
		    		System.out.println(complemento.getAny());
		    		for(Object object : complemento.getAny() ) {
		    			System.out.println(object);
		    		}
		    	}
		    	
		        
		        
				xmlreader.close();
				
				
				
				
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
	
	public String getlineCsv() {
		String csvFormat="%s,%s,%s, %s, %s, %s,";
		String line = String.format(csvFormat,"","Vigente", cfdi.getVersion(), cfdi.getVersion(),"factura",cfdi.getFecha());
		return line;
		
	}

}
