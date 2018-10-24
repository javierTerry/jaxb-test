package hello;

import java.io.File;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;
import net.valdo.cfdi.Comprobante;
import net.valdo.cfdi.Comprobante.Emisor;
import net.valdo.jaxb.ImplJaxb;
import net.valdo.utils.UnzipFile;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    private final String XML_FILE = "3.3";
    private final String SOURCE_FILES_UNZIP = System.getProperty("user.home") + "unzip/";
    
    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    
    @GetMapping("/")
    public String index() {
    	return "redirect:/loading/index";
    }
    
    @GetMapping("/loading/index")
    public String loading(RedirectAttributes redirectAttributes) {
    	return "uploadForm";
    }


    @SuppressWarnings("resource")
	@GetMapping("/download/{sourceName}")
    public void listUploadedFiles(@PathVariable("sourceName") String sourceName, HttpServletResponse response) {
    	
    	response.addHeader("Content-Type", "application/csv");
		response.addHeader("Content-Disposition", "attachment; filename=cfdi_details.csv");
		response.setCharacterEncoding("UTF-8");
		
    	
		try {
			PrintWriter out = response.getWriter();
			Stream<Path> path;
			
			
			out.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56");
			out.write("\n");
			out.write("Verificado ó Asoc.,Estado SAT,Version,Tipo,Fecha Emision,Fecha Timbrado,EstadoPago,FechaPago,Serie,Folio,UUID,UUID Relacion,"
					+ "RFC Emisor,Nombre Emisor,LugarDeExpedicion,RFC Receptor,Nombre Receptor,ResidenciaFiscal,NumRegIdTrib,UsoCFDI,SubTotal,Descuento"
					+ ",Total IEPS,IVA 16%,Retenido IVA,Retenido ISR,ISH,Total,TotalOriginal,Total Trasladados,Total Retenidos,Total LocalTrasladado,Total LocalRetenido"
					+ ",Complemento,Moneda,Tipo De Cambio,FormaDePago,Metodo de Pago,NumCtaPago,Condicion de Pago,Conceptos,Combustible"
					+ ",IEPS 3%,IEPS 6%,IEPS 7%,IEPS 8%,IEPS 9%,IEPS 26.5%,IEPS 30%,IEPS 53%,IEPS 160%,Archivo XML,Direccion Emisor,Localidad Emisor,Direccion Receptor"
					+ ",Localidad Receptor");
			
			path = Files.walk(Paths.get(SOURCE_FILES_UNZIP + sourceName));
					
			path
        	.filter(Files::isRegularFile)
        	.filter(filePath -> filePath.toString().endsWith(".xml"))
        	.forEach(filePath -> {
                try {
                	System.out.println(filePath.toString());
                	ImplJaxb implJaxb = new ImplJaxb();
                	implJaxb.xmlParse(filePath);
                	
                	out.write( implJaxb.getlineCsv() );
        			out.write("\n");
                	
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
			
			out.flush();
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
        
        
        //return "uploadForm";
    }
    
    

    @SuppressWarnings("static-access")
	@PostMapping("/loading/zip")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
    	System.out.println("Post recibido");
    	Stream<Path> path;
    	String FileNameZip = file.getOriginalFilename();
    	String sourceName = FileNameZip.replaceFirst("[.][^.]+$", "");
    	try {
			UnzipFile unzipFile = new UnzipFile();
		
			unzipFile.unzip(file.getInputStream(), SOURCE_FILES_UNZIP);
			
			storageService.store(file);
			redirectAttributes.addFlashAttribute("message",
			        "You successfully uploaded " + FileNameZip + "!");
			
			redirectAttributes.addFlashAttribute("sourceName", sourceName);
	    	System.out.println("fin Post");

    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
			
        return "redirect:/loading/index";
    }

    
    @GetMapping("/cfdi")
    public String cfdiForm(Model model) {
    	
    	
    	
    	Emisor emisor = new Comprobante.Emisor();
		emisor.setNombre("christian Hernandez");
		
		Comprobante comprobante = new Comprobante();
		
		comprobante.setVersion(XML_FILE);
		comprobante.setSerie("AA");
		comprobante.setEmisor(emisor);
	
		
		// create JAXB context
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Comprobante.class);
			
			
			System.out.println("<!----------Generating the XML Output-------------->");
			// Marshalling [Generate XML from JAVA]
			// create Marshaller using JAXB context
			Marshaller m = context.createMarshaller();
			// To format the [to be]generated XML output
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			// Marshall it and write output to System.out or to a file
			m.marshal(comprobante, System.out);
			m.marshal(comprobante, new File(XML_FILE));
			
			StringWriter sw = new StringWriter();
		    m.marshal(comprobante, sw);

		    String result = sw.toString();
			
			
			
			
			/*
			System.out.println("<!---------------Generating the Java objects from XML Input-------------->");
			// UnMarshalling [Generate JAVA from XML]
			// Instantiate Unmarshaller via context
			Unmarshaller um = context.createUnmarshaller();
			// Unmarshall the provided XML into an object
			Comprobante cfdi = (Comprobante) um.unmarshal(new FileReader(XML_FILE));
			System.out.println("Serie :" + cfdi.getSerie());
			*/
			
		   
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		 model.addAttribute("comprobante", comprobante);
	        return "cfdi";
    	
        
    }

    @PostMapping("cfdi")
    public String cfdiSubmit(Model model) {
    	
    	// create JAXB context
    	JAXBContext context;
		try {
			context = JAXBContext.newInstance(Comprobante.class);
			System.out.println("<!---------------Generating the Java objects from XML Input-------------->");
			// UnMarshalling [Generate JAVA from XML]
			// Instantiat	e Unmarshaller via context
			Unmarshaller um = context.createUnmarshaller();
			
			//String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><cfdi:Comprobante xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd http://www.sat.gob.mx/Pagos http://www.sat.gob.mx/sitio_internet/cfd/Pagos/Pagos10.xsd\"  Serie=\"WEB\" Folio=\"278987\" Fecha=\"2018-09-11T12:39:50\" Sello=\"CO8rcGsgsgsKPbZSCGfsdGsbubZRNYcLUNmZPJzotMfOmNdaWL++U3VisK/V4xp3lbBQdjyBnzVGXGw5oeLf0EfoKofz/C3KClFWPMJixAjLu6ccklL9H9Bu/xyV2No5mj2eqx7Aul9cOt6nisiVZDLmCcihgH7A64xG5bhMvh1jq3XlaHsmv/WVpoAhyGgumJ++F8ikDgHr4ukoRGUv/waPKOHwVcFo+sqaP78e25e0LSpccQ/sRyLgLA1VHKczgTkdVPepLu/T/fPKB1SzYRQ36q5Qfooif7lnk4AY+edE6XOXOnF5MWlaUUYrfEUcDmo9q46+2mF4SvwUX3gldg==\" FormaPago=\"01\" NoCertificado=\"00001000000405280811\" Certificado=\"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\" SubTotal=\"259.48\" Moneda=\"MXN\" Total=\"300.00\" TipoDeComprobante=\"I\" MetodoPago=\"PUE\" LugarExpedicion=\"01180\" xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"><cfdi:Emisor Rfc=\"SSP530529PB8\" Nombre=\"SERVICIO SAN PEDRO SA DE CV\" RegimenFiscal=\"601\" /><cfdi:Receptor Rfc=\"MCO000823CK3\" Nombre=\"MASNEGOCIO COM SAPI DE CV\" UsoCFDI=\"G03\" /><cfdi:Conceptos><cfdi:Concepto ClaveProdServ=\"15101506\" Cantidad=\"15.306120\" ClaveUnidad=\"LTR\" Unidad=\"LITROS\" Descripcion=\"MAGNA-32011\" ValorUnitario=\"16.95\" Importe=\"259.48\"><cfdi:Impuestos><cfdi:Traslados><cfdi:Traslado Base=\"253.250000\" Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"40.52\" /></cfdi:Traslados></cfdi:Impuestos></cfdi:Concepto></cfdi:Conceptos><cfdi:Impuestos TotalImpuestosTrasladados=\"40.52\"><cfdi:Traslados><cfdi:Traslado Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"40.52\" /></cfdi:Traslados></cfdi:Impuestos><cfdi:Complemento><tfd:TimbreFiscalDigital xmlns:tfd=\"http://www.sat.gob.mx/TimbreFiscalDigital\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sat.gob.mx/TimbreFiscalDigital http://www.sat.gob.mx/sitio_internet/cfd/TimbreFiscalDigital/TimbreFiscalDigitalv11.xsd\" Version=\"1.1\" UUID=\"e7ed599b-99a5-4357-a1a9-10afa31db653\" FechaTimbrado=\"2018-09-11T16:39:51\" RfcProvCertif=\"AUR100128NN3\" SelloSAT=\"KjJBKY8cDJi/oOv1fe8eyhuG0l65STsWTawGneuaktnGDP9bYyNvugOM3gMoDiK0qA2o+W/quNG7knlxWu6MC+87KLYMVvx9YqnDsQiKJtPvnqBa57F/IitcKnO0gPOdNbYRh8aTQ/JX9xApBDsKDy2ZJkafRWwTs76JGJKFRpjule/LeuIbRbggUXmckIQhuw4z5s7+76Pn0g8UjZDB8oIjKnUMKSCaSB5GZIvZBTWxqmWyxqM/iUPNdIAWJyAoc4HaSCYEzVKWUP30Y/eFTp8n7mADOBh56MEabnx7V2iZ9SoBhIu9LY9XswnzCBNb6FRA4+yLf//w04AbU1+lBQ==\" SelloCFD=\"CO8rcGsgsgsKPbZSCGfsdGsbubZRNYcLUNmZPJzotMfOmNdaWL++U3VisK/V4xp3lbBQdjyBnzVGXGw5oeLf0EfoKofz/C3KClFWPMJixAjLu6ccklL9H9Bu/xyV2No5mj2eqx7Aul9cOt6nisiVZDLmCcihgH7A64xG5bhMvh1jq3XlaHsmv/WVpoAhyGgumJ++F8ikDgHr4ukoRGUv/waPKOHwVcFo+sqaP78e25e0LSpccQ/sRyLgLA1VHKczgTkdVPepLu/T/fPKB1SzYRQ36q5Qfooif7lnk4AY+edE6XOXOnF5MWlaUUYrfEUcDmo9q46+2mF4SvwUX3gldg==\" NoCertificadoSAT=\"00001000000404624465\" /></cfdi:Complemento></cfdi:Comprobante>";
			
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><cfdi:Comprobante xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd http://www.sat.gob.mx/Pagos http://www.sat.gob.mx/sitio_internet/cfd/Pagos/Pagos10.xsd\"  Serie=\"WEB2\" Folio=\"2789872\" Fecha=\"2018-09-11T12:39:50\" FormaPago=\"01\" NoCertificado=\"00001000000405280811\" Certificado=\"MIIGKjCCBBKgAwIBAgIUMDAwMDEwMDAwMDA0MDUyODA4MTEwDQYJKoZIhvcNAQELBQAwggGyMTgwNgYDVQQDDC9BLkMuIGRlbCBTZXJ2aWNpbyBkZSBBZG1pbmlzdHJhY2nDs24gVHJpYnV0YXJpYTEvMC0GA1UECgwmU2VydmljaW8gZGUgQWRtaW5pc3RyYWNpw7NuIFRyaWJ1dGFyaWExODA2BgNVBAsML0FkbWluaXN0cmFjacOzbiBkZSBTZWd1cmlkYWQgZGUgbGEgSW5mb3JtYWNpw7NuMR8wHQYJKoZIhvcNAQkBFhBhY29kc0BzYXQuZ29iLm14MSYwJAYDVQQJDB1Bdi4gSGlkYWxnbyA3NywgQ29sLiBHdWVycmVybzEOMAwGA1UEEQwFMDYzMDAxCzAJBgNVBAYTAk1YMRkwFwYDVQQIDBBEaXN0cml0byBGZWRlcmFsMRQwEgYDVQQHDAtDdWF1aHTDqW1vYzEVMBMGA1UELRMMU0FUOTcwNzAxTk4zMV0wWwYJKoZIhvcNAQkCDE5SZXNwb25zYWJsZTogQWRtaW5pc3RyYWNpw7NuIENlbnRyYWwgZGUgU2VydmljaW9zIFRyaWJ1dGFyaW9zIGFsIENvbnRyaWJ1eWVudGUwHhcNMTcwMjI0MTYxODM1WhcNMjEwMjI0MTYxODM1WjCByjEkMCIGA1UEAxMbU0VSVklDSU8gU0FOIFBFRFJPIFNBIERFIENWMSQwIgYDVQQpExtTRVJWSUNJTyBTQU4gUEVEUk8gU0EgREUgQ1YxJDAiBgNVBAoTG1NFUlZJQ0lPIFNBTiBQRURSTyBTQSBERSBDVjElMCMGA1UELRMcU1NQNTMwNTI5UEI4IC8gT0lCUjU0MTAyNEVNNjEeMBwGA1UEBRMVIC8gT0lCUjU0MTAyNEhERlJSRjA0MQ8wDQYDVQQLEwZVTklEQUQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDa6wQGbYKi3kqpeCYwGIhBAs8uSbtt0qQjqt6JH51ZPBGjdZQg34/7w+F07jDOVDD6e1zMK8wCKSCxbadpeMumnfwgxRqJSP4UJSrq34Vx7eHLaGZKvx1ouFriToLHZN4z1xg/c5/zYI/wexQXWOuSYqX52jhl1Z+O40oxzhboUNVsPkvLORA+lng4XdGKkarZtbhk3n4ODKIdebEN4bmy/E3+yCu7oDsPnRw8Iov6VF/2nwL75M185vcjIdkjmzxfEcKPOT7VCZoOyay5x+AsXHn6yF6NAZOgxdS+rCpNK8gI6arxV32DftR6BFg9PIjp5Vb/4xcuYiWhILEoan3nAgMBAAGjHTAbMAwGA1UdEwEB/wQCMAAwCwYDVR0PBAQDAgbAMA0GCSqGSIb3DQEBCwUAA4ICAQCKsvMHMTPYqgKr4IXhKxDPS8ttEm6J5yr3b/dt6a1vT7+qzvWc3HTWWaxcLx15JAo0lwJBOYk2VgE7rA02gxk8Hp4pfXO1edIoOvGw4kABGl+C69NAcMrvUjwJZY0iZ6hWSJOoxZVdOKdN1k+kiNZeiohooIn2FTdzrs650+nAEv82yDkU5q1Y38sktSQHfHMEy+UgsoIAamEUGW4lfxE7FRZDSrcvHVuzGosENUt4Eb6YbmSb/4+Qq4xWJDflJsj2+uCBNN6c9DX2JRtJBhr/Mp104cYOiG/QUVRSuxmwziWoM6brU35iw/+MrC0p+44BwrfLfZmzW4DNr8WHIOHThK4JDwVdLq0y4FGbbII2Xvu/XzVQGk776+HSWNjp5zSta/gbeWE9TvDQYvElkMyw392Bjv9awWPgQgixtz9AuQKDmez4McFAsIm+tdxv3BbE7XrKBM5jL4uRBJurlRLRPBeNlGNDxUxvDPjKoJQXX/bnrrzA00UuwSxez4RhhOTAFFdYQRXQ/q9a66dXHJ/TEhgV7eRCE2mXHc9DQqrpKd5az+7i5tuKU1jHV4BfohUBGczDYk1vKm7INZAx5oal17DjwvT1w1KFloxmGftaWUTwwsA/pbmgwSsGbB3lNIyaFQG8uDchhZ+12vDYSP+7sMmISvfrZ+e5qePqfvFU6Q==\" SubTotal=\"259.48\" Moneda=\"MXN\" Total=\"300.00\" TipoDeComprobante=\"I\" MetodoPago=\"PUE\" LugarExpedicion=\"01180\" xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"><cfdi:Emisor Rfc=\"SSP530529PB8\" Nombre=\"SERVICIO SAN PEDRO SA DE CV\" RegimenFiscal=\"601\" /><cfdi:Receptor Rfc=\"MCO000823CK3\" Nombre=\"MASNEGOCIO COM SAPI DE CV\" UsoCFDI=\"G03\" /><cfdi:Conceptos><cfdi:Concepto ClaveProdServ=\"15101506\" Cantidad=\"15.306120\" ClaveUnidad=\"LTR\" Unidad=\"LITROS\" Descripcion=\"MAGNA-32011\" ValorUnitario=\"16.95\" Importe=\"259.48\"><cfdi:Impuestos><cfdi:Traslados><cfdi:Traslado Base=\"253.250000\" Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"40.52\" /></cfdi:Traslados></cfdi:Impuestos></cfdi:Concepto></cfdi:Conceptos><cfdi:Impuestos TotalImpuestosTrasladados=\"40.52\"><cfdi:Traslados><cfdi:Traslado Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"40.52\" /></cfdi:Traslados></cfdi:Impuestos><cfdi:Complemento><tfd:TimbreFiscalDigital xmlns:tfd=\"http://www.sat.gob.mx/TimbreFiscalDigital\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.sat.gob.mx/TimbreFiscalDigital http://www.sat.gob.mx/sitio_internet/cfd/TimbreFiscalDigital/TimbreFiscalDigitalv11.xsd\" Version=\"1.1\" UUID=\"e7ed599b-99a5-4357-a1a9-10afa31db653\" FechaTimbrado=\"2018-09-11T16:39:51\" RfcProvCertif=\"AUR100128NN3\" SelloSAT=\"KjJBKY8cDJi/oOv1fe8eyhuG0l65STsWTawGneuaktnGDP9bYyNvugOM3gMoDiK0qA2o+W/quNG7knlxWu6MC+87KLYMVvx9YqnDsQiKJtPvnqBa57F/IitcKnO0gPOdNbYRh8aTQ/JX9xApBDsKDy2ZJkafRWwTs76JGJKFRpjule/LeuIbRbggUXmckIQhuw4z5s7+76Pn0g8UjZDB8oIjKnUMKSCaSB5GZIvZBTWxqmWyxqM/iUPNdIAWJyAoc4HaSCYEzVKWUP30Y/eFTp8n7mADOBh56MEabnx7V2iZ9SoBhIu9LY9XswnzCBNb6FRA4+yLf//w04AbU1+lBQ==\" SelloCFD=\"CO8rcGsgsgsKPbZSCGfsdGsbubZRNYcLUNmZPJzotMfOmNdaWL++U3VisK/V4xp3lbBQdjyBnzVGXGw5oeLf0EfoKofz/C3KClFWPMJixAjLu6ccklL9H9Bu/xyV2No5mj2eqx7Aul9cOt6nisiVZDLmCcihgH7A64xG5bhMvh1jq3XlaHsmv/WVpoAhyGgumJ++F8ikDgHr4ukoRGUv/waPKOHwVcFo+sqaP78e25e0LSpccQ/sRyLgLA1VHKczgTkdVPepLu/T/fPKB1SzYRQ36q5Qfooif7lnk4AY+edE6XOXOnF5MWlaUUYrfEUcDmo9q46+2mF4SvwUX3gldg==\" NoCertificadoSAT=\"00001000000404624465\" /></cfdi:Complemento></cfdi:Comprobante>";
			StringReader reader = new StringReader(xml);
			
			// Unmarshall the provided XML into an object
			//Comprobante cfdi = (Comprobante) um.unmarshal(new FileReader(XML_FILE));
			Comprobante comprobante = (Comprobante) um.unmarshal( reader );
			System.out.println("Serie :" + comprobante.getSerie());
			System.out.println("Version :" + comprobante.getVersion());
			System.out.println("xml :" + xml);
			
			model.addAttribute("comprobante", comprobante);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.out.println();
		}
    	
    	
		 
        return "cfdiresult";
    }
    
    
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
    

}