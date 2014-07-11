package org.oasis.datacore.rest.api.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.commons.io.IOUtils;
import org.oasis.datacore.rest.api.DCResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.NQuadRDFParser;
import com.github.jsonldjava.utils.JsonUtils;


/*
 * Media Types defined in DatacoreApi
 */

@javax.ws.rs.ext.Provider
@javax.ws.rs.Consumes(DatacoreMediaType.APPLICATION_NQUADS)
@javax.ws.rs.Produces(DatacoreMediaType.APPLICATION_NQUADS) 
public class JsonLdJavaRdfProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
   
   /** wired by Spring XML ; should normally be DatacoreObjectMapper */
   private ObjectMapper objectMapper;
   
   @Override
   public boolean isWriteable(Class<?> type,
         java.lang.reflect.Type genericType, Annotation[] annotations,
         MediaType mediaType) {
      if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
         // default
         return false;
      }

      return mediaType.isCompatible(MediaType.valueOf("application/json+ld")) // TODO constant
            || mediaType.isCompatible(DatacoreMediaType.APPLICATION_NQUADS_TYPE);
   }


   @Override
   public long getSize(Object t, Class<?> type,
         java.lang.reflect.Type genericType, Annotation[] annotations,
         MediaType mediaType) {
      return -1;
   }


   @Override
   public void writeTo(Object t, Class<?> type,
         java.lang.reflect.Type genericType, Annotation[] annotations,
         MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
         OutputStream entityStream) throws IOException, WebApplicationException {
      
      /*String acceptedMediaTypeString = (String) httpHeaders.getFirst(HttpHeaders.ACCEPT);
      MediaType acceptedMediaType = acceptedMediaTypeString == null || acceptedMediaTypeString.isEmpty()
            ? MediaType.APPLICATION_JSON_TYPE : MediaType.valueOf(acceptedMediaTypeString);*/
      if (!isWriteable(type, genericType, annotations, mediaType)) {
         // should never be reached
         throw new WebApplicationException(Response.serverError().entity(
               "JsonLdJavaRdfProvider can't handle media type " + mediaType).build());
      }
      
      /*
      if (acceptedMediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
         // default
         objectMapper.writer().writeValue(entityStream, t);
         return;
      }
      
      if (!(acceptedMediaType.isCompatible(MediaType.valueOf("application/json+ld"))
            || acceptedMediaType.isCompatible(DatacoreMediaType.APPLICATION_NQUADS_TYPE))) {
         throw new WebApplicationException(Response.serverError().entity(
               "JsonLdJavaRdfProvider can't handle "
               + "accepted media type " + acceptedMediaTypeString).build());
      }
      */
      
      try {
         // TODO TODOOOOOOOOOOOOOOOOOOOOOOOO improve that !!
         String json = objectMapper.writeValueAsString(t)
               .replaceAll("\"l\"", "\"@language\"")
               .replaceAll("\"v\"", "\"@value\"");
         
         Object jsonObject = JsonUtils.fromInputStream(new ByteArrayInputStream(json.getBytes()));
         
         // Create a context JSON map containing prefixes and definitions
         Map context = new HashMap();
         // Customise context...
         context.put("dc", "http://dc");
         context.put("i18n:name", "{\"@container\": \"@language\"}");
         // Create an instance of JsonLdOptions with the standard JSON-LD options
         JsonLdOptions options = new JsonLdOptions();
         ///options.set
         // Customise options...
         Object res = jsonObject;

         String format = mediaType.getParameters().get("format");
         if("compact".equals(format)) {
            res = JsonLdProcessor.compact(jsonObject, context, options);
            //System.out.println(JsonUtils.toPrettyString(compact));
         } else if("flatten".equals(format)) {
            res = JsonLdProcessor.flatten(jsonObject, context, options);
         } else if("expand".equals(format)) {
            res = JsonLdProcessor.expand(jsonObject, options);
         } else if("frame".equals(format)) {
            res = JsonLdProcessor.frame(jsonObject, context, options);
         } else if(format == null|| "text/plain".equals(format) || "nquads".equals(format)
               || "nq".equals(format) || "nt".equals(format)
               || "ntriples".equals(format)) {
            //System.out.println("Generating Nquads Report");
            options.format = "application/nquads";
            res = JsonLdProcessor.toRDF(jsonObject, options);
         } else if("text/turtle".equals(format) || "turtle".equals(format)
               || "ttl".equals(format)) {
            
            options.format = "text/turtle";
            res = JsonLdProcessor.toRDF(jsonObject, options);
         }
         InputStream rdfStream = IOUtils.toInputStream(res.toString());
         IOUtils.copy(rdfStream, entityStream);
         
      } catch(IOException | JsonLdError ioe) {
         //Problem with json ld fall back to normal execution
         objectMapper.writer().writeValue(entityStream, t);
         return;
      }
      
      
      
/*
      String json = objectMapper.writeValueAsString(t);
      Object jsonObject = JsonUtils.fromInputStream(new ByteArrayInputStream(json.getBytes()));
      // Create a context JSON map containing prefixes and definitions
      Map<String, String> context = new HashMap<String, String>();
      context.put("dc", "http://dc");
      context.put("i18n:name", "{\"@container\": \"@language\"}");
      // Create an instance of JsonLdOptions with the standard JSON-LD options
      JsonLdOptions options = new JsonLdOptions();
      //options.setExpandContext(context);
      options.format = "application/nquads";

      String nquadsRdf = null;
      try {
         nquadsRdf = (String) JsonLdProcessor.toRDF(jsonObject, options);
      } catch (JsonLdError e) {
         e.printStackTrace();
      }

      InputStream rdfStream = IOUtils.toInputStream(nquadsRdf);
      IOUtils.copy(rdfStream, entityStream);
      */
   }


   @Override
   public boolean isReadable(Class<?> type, java.lang.reflect.Type genericType,
         Annotation[] annotations, MediaType mediaType) {
      if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
         // default
         return false;
      }

      return mediaType.isCompatible(MediaType.valueOf("application/json+ld")) // TODO constant
            || mediaType.isCompatible(DatacoreMediaType.APPLICATION_NQUADS_TYPE);
   }


   
   @Override
   public Object readFrom(Class<Object> type,
         java.lang.reflect.Type genericType, Annotation[] annotations,
         MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
         InputStream entityStream) throws IOException, WebApplicationException {

      if (!isWriteable(type, genericType, annotations, mediaType)) {
         // should never be reached
         throw new WebApplicationException(Response.serverError().entity(
               "JsonLdJavaRdfProvider can't handle accepted type " + mediaType).build());
      }
      
      JsonLdOptions options = new JsonLdOptions();
      //options.format = "application/nquads";
      String rdfString = IOUtils.toString(entityStream);
      Object jsonObject = null;
      
      try {
         NQuadRDFParser nquadParser = new NQuadRDFParser();
         //Object nquad = nquadParser.parse(rdfString);
         //jsonObject = JsonLdProcessor.fromRDF(nquad, options);
         options.outputForm = "compacted";
         options.setUseNativeTypes(true);
         jsonObject = JsonLdProcessor.fromRDF(rdfString, options, nquadParser);
      } catch (JsonLdError e) {
         e.printStackTrace();
      }
      
      if(type.toString().contains("DCResource")) {
         return parseAndBuildResource(jsonObject);
      } else {
         ArrayList<Object> resourceList = new ArrayList<Object>();
         //Test if single resource or already resource list.
         LinkedHashMap<?,?> map = (LinkedHashMap<?,?>) jsonObject;
         if(map.containsKey("@graph")) {
            ArrayList<?> tempList = (ArrayList<?>) map.get("@graph");
            for(int i = 0; i < tempList.size(); i++) {
               resourceList.add(parseAndBuildResource(tempList.get(i)));
            }
         } else {
            resourceList.add(parseAndBuildResource(jsonObject));
         }
         
         return resourceList;
      }
   }
   
   public Object parseAndBuildResource(Object jsonObject) throws IOException {
      String json = objectMapper.writeValueAsString(jsonObject);
      return objectMapper.reader(DCResource.class) // TODO rather type but if list after handling single value array like in interceptor
            .readValue(json);
   }

   public ObjectMapper getObjectMapper() {
      return objectMapper;
   }


   public void setObjectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
   }

}
