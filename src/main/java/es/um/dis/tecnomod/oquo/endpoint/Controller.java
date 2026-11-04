package es.um.dis.tecnomod.oquo.endpoint;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.um.dis.tecnomod.oquo.dto.ObservationsInfoDTO;
import es.um.dis.tecnomod.oquo.service.InstanceCreator;

@RestController
public class Controller {
	@PostMapping(value="/toOquoObservation", produces = "text/turtle")
	public String toOquoObservation (@RequestBody ObservationsInfoDTO observationsInfo,
			@RequestParam(name = "dataNamespace", required = false) String dataNamespace) {

		if (dataNamespace != null && !dataNamespace.isBlank()) {
	        validateNamespace(dataNamespace);
	    }

		Model rdfModel = InstanceCreator.createObservations(observationsInfo, dataNamespace);
		StringWriter writer = new StringWriter();
		rdfModel.write(writer, "TURTLE");
		return writer.toString();
	}

	private static void validateNamespace(String ns) {
	    try {
	        URI uri = new URI(ns);
	        boolean http = "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
	        boolean validEnd = ns.endsWith("/") || ns.endsWith("#");
	        if (uri.isAbsolute() && http && validEnd && !ns.matches(".*[\\s<>\"{}|\\\\^`].*")) {
	            return;
	        }
	    } catch (URISyntaxException e) {
	        // fall through
	    }
	    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataNamespace must be an absolute http(s) IRI ending in '/' or '#'");
	}
}
