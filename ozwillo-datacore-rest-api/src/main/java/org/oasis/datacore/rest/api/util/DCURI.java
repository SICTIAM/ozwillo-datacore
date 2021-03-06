package org.oasis.datacore.rest.api.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.oasis.datacore.rest.api.DatacoreApi;


/**
 * Helper to build URIs (along with UriHelper), but does not belong to model.
 * NB. beware, there's another different DCURI in -core's entity package. TODO merge them ??
 * TODO (type-relative) id or iri (but does not contain type) ? 
 * 
 * Datacore Resource URI, for now also works as Social Graph Resource URI.
 * TODO LATER refactor SCURI out of it for Social Graph ?
 * 
 * WARNING not thread safe (save if rendered at init)
 * 
 * @author mdutoo
 *
 */
public class DCURI {

   /** Container base URL ex. http://data.ozwillo.com/ . Protocol is assumed to be HTTP
    * (if HTTPS, there must be a redirection) */
   private URI containerUrl;
   /** String version of container base URL ex. http://data.ozwillo.com/ . Protocol is assumed to be HTTP
    * (if HTTPS, there must be a redirection) */
   private String container;
   /** Base Model type ex. "city.sample.city".
    * Corresponds to a type of use and a data governance configuration.
    * For Social Graph ex. user (account), organization
    * null for external URIs ex. */
   // TODO alternate extending type SCURI with ex. "account" instead
   private String type;
   /** ID / IRI ex. Lyon, London, Torino. Can't change (save by data migration operations).
    * For Social Graph ex. email */
   private String id;
   /** lazy */
   private String encodedId = null;
   private boolean isRelativeUri;
   private boolean isExternalDatacoreUri;
   private boolean isExternalWebUri;
   private boolean isExternalUri;

   /** encoded */
   private String cachedStringUri = null;
   private String cachedUnencodedStringUri = null;
   /** normalized */
   private URI cachedUri = null;
   /** unencoded ; i.e. /dc/type/[type]/[id] to help working with Java URIs */
   private String cachedPath = null;
   
   
   public DCURI() {
      
   }
   /**
    * Creates a new URI (Datacore or external)
    * @param container must not be null
    * @param type null for (unknown) external Web URI 
    * @param id unencoded
    * @param isRelativeUri
    * @param isExternalDatacoreUri
    * @param isExternalWebUri
    * @throws URISyntaxException 
    */
   public DCURI(String container, String type, String id,
         boolean isRelativeUri, boolean isExternalDatacoreUri, boolean isExternalWebUri) throws URISyntaxException {
      this(new URI(container), type, id, isRelativeUri, isExternalDatacoreUri, isExternalWebUri);
   }
   /**
    * Creates a new URI (Datacore or external)
    * @param container must not be null
    * @param type null for (unknown) external Web URI 
    * @param id unencoded
    * @param isRelativeUri
    * @param isExternalDatacoreUri
    * @param isExternalWebUri
    * @throws URISyntaxException 
    */
   public DCURI(URI containerUrl, String type, String id,
         boolean isRelativeUri, boolean isExternalDatacoreUri, boolean isExternalWebUri) {
      this.containerUrl = containerUrl;
      this.container = containerUrl.toString();
      this.type = type;
      this.id = id;
      this.isRelativeUri = isRelativeUri;
      this.isExternalDatacoreUri = isExternalDatacoreUri;
      this.isExternalWebUri = isExternalWebUri;
      this.isExternalUri = this.isExternalDatacoreUri || this.isExternalWebUri;
   }
   /**
    * Creates a new Datacore URI
    * @param container must not be null
    * @param type null for (unknown) external Web URI 
    * @param id unencoded
    * @param isRelativeUri
    * @param isExternalDatacoreUri
    * @param isExternalWebUri
    * @throws URISyntaxException 
    */
   public DCURI(String container, String type, String id) throws URISyntaxException {
      this(container, type, id, false, false, false);
   }
   /**
    * Creates a new Datacore URI
    * @param container must not be null
    * @param type null for (unknown) external Web URI 
    * @param id unencoded
    * @param isRelativeUri
    * @param isExternalDatacoreUri
    * @param isExternalWebUri
    * @throws URISyntaxException 
    */
   public DCURI(URI container, String type, String id) {
      this(container, type, id, false, false, false);
   }

   /**
    * SAME AS SimpleUriService
    * TODO don't normalize ?
    * @param containerUrl if null, conf'd default
    * @param modelType
    * @param id
    * @return escaped ex. http://data.ozwillo.com/dc/type/geo:CityGroup_0/FR/CC%20les%20Ch%C3%A2teaux
    * SAVE IF "//" in id (ex. itself an URI) in which case id is fully URL encoded
    * ex. http://data.ozwillo.com/dc/type/photo:Library_0/https%3A%2F%2Fwww.10thingstosee.com%2Fmedia%2Fphotos%2Ffrance-778943_HjRL4GM.jpg
    * and to get an URI, new URI(returned)
    */
   private static URI buildUri(URI containerUrl, String modelType, String id) throws URISyntaxException {
      if (modelType == null) {
         // external URI : type not known because not parsable
         return new URI(containerUrl.toString() + '/' + id);
         // (no need of encoding nor toASCIIString because id = unencoded path here)
      }
      
      String path = "/dc/type/" + modelType;
      if (id.contains("//")) {
         try {
            URI escapedModelTypeUri = new URI(containerUrl.getScheme(), null,
                  containerUrl.getHost(), containerUrl.getPort(), path, null, null).normalize();
            // ex. escapedUri = http://data.ozwillo.com/dc/type/geo:CityGroup_0/FR/CC%20les%20Châteaux
            String escapedModelTypeUriString = escapedModelTypeUri.toASCIIString();
            // and to get an URI, new URI(escapedUri.toASCIIString()), else UTF-8 ex. â not encoded
            // ex. http://data.ozwillo.com/dc/type/geo:CityGroup_0/FR/CC%20les%20Ch%C3%A2teaux
            return new URI(escapedModelTypeUriString + '/' + URLEncoder.encode(id, StandardCharsets.UTF_8.name()));
         } catch (URISyntaxException usex) {
            // can't happen since containerUrl & this.containerUrl are nice URIs
            throw usex;
         } catch (UnsupportedEncodingException ueex) {
            // can't happen
            throw new URISyntaxException("bad uri", ueex.getMessage());
         }
      }
      
      path += '/' + id;
      // ex. (decoded) path = "/dc/type/geo:CityGroup_0/FR/CC les Châteaux"
      try {
         URI escapedUri = new URI(containerUrl.getScheme(), null,
               containerUrl.getHost(), containerUrl.getPort(), path, null, null).normalize();
         // ex. escapedUri = http://data.ozwillo.com/dc/type/geo:CityGroup_0/FR/CC%20les%20Châteaux
         return new URI(escapedUri.toASCIIString());
         // and to get an URI, new URI(escapedUri.toASCIIString()), else UTF-8 ex. â not encoded
         // ex. http://data.ozwillo.com/dc/type/geo:CityGroup_0/FR/CC%20les%20Ch%C3%A2teaux
      } catch (URISyntaxException usex) {
         // can't happen since containerUrl & this.containerUrl are nice URIs
         throw usex;
      }
   }


   private String getEncodedId() {
      if (this.encodedId == null && this.id != null) {
         this.encodedId = encodeId(id);
      }
      return this.encodedId;
   }
   
   /**
    * @obsolete for now
    * @param id
    * @return
    */
   private static String encodeId(String id) {
      if (id.contains("//")) {
         try {
            return URLEncoder.encode(id, StandardCharsets.UTF_8.name());
         } catch (UnsupportedEncodingException ueex) {
            return "bad uri";
         }
      }
      if (id.length() == 0) {
         return id;
      }
      StringBuilder encodedIdSb = new StringBuilder();
      String[] pathComponents = id.split("/");
      // at least one :
      encodedIdSb.append(pathComponents[0]);
      for (int i = 1; i < pathComponents.length; i++) {
         encodedIdSb.append(pathComponents[i]);
      }
      return encodedIdSb.toString();
   }

   /**
    * 
    * @return unencoded
    * @obsolete for now
    */
   private String getPath() {
      if (cachedPath != null) {
         return cachedPath;
      }
      StringBuilder pathSb = new StringBuilder();
      if (type != null) {
         pathSb.append(DatacoreApi.DC_TYPE_PATH); // NB. front slash because none in baseUrl
         pathSb.append(type); // ex. "city.sample.city"
         pathSb.append('/');
      } else {
         // external URI : type not known because not parsable
         pathSb.append('/');
      }
      pathSb.append(id); // ex. "London", "Lyon"...
      cachedPath = pathSb.toString();
      return cachedPath;
   }
   /**
    * cached ; encodes path (including id) using java.net.URI
    * @return
    * @throws URISyntaxException
    */
   public URI toURI() throws URISyntaxException {
      if (cachedUri == null) {
         /*
         // ex. (decoded) path = "/dc/type/geo:CityGroup_0/FR/CC les Châteaux"
         URI escapedUri = new URI(containerUrl.getScheme(), null,
            containerUrl.getHost(), containerUrl.getPort(), getPath(), null, null).normalize();
         // ex. escapedUri = http://data.ozwillo.com/dc/type/geo:CityGroup_0/FR/CC%20les%20Châteaux
         cachedUri = new URI(escapedUri.toASCIIString()); // else UTF-8 ex. â not encoded
         // ex. cachedUri = http://data.ozwillo.com/dc/type/geo:CityGroup_0/FR/CC%20les%20Ch%C3%A2teaux
          */
         return buildUri(containerUrl, type, id);
      }
      return cachedUri;
   }
   /**
    * cached ; built on toURI()
    * @return [container]dc/type/[type]/id
    * @throws IllegalArgumentException if bad URI syntax
    */
   @Override
   public String toString() {
      if (cachedStringUri != null) {
         return cachedStringUri;
      }
      try {
         cachedStringUri = toURI().toString();
      } catch (URISyntaxException urisex) {
         throw new IllegalArgumentException("Bad URI syntax ", urisex);
      }
      return cachedStringUri;
   }
   /**
    * cached
    * @return
    */
   public String toUnencodedString() {
      if (cachedUnencodedStringUri != null) {
         return cachedUnencodedStringUri;
      }
      cachedUnencodedStringUri = container + getPath();  // NB. container is ex. http://data.ozwillo.com
      return cachedUnencodedStringUri;
   }
   @Override
   public boolean equals(Object o) {
      if (o == null || !(o instanceof DCURI)) {
         return false;
      }
      return this.toString().equals(o.toString());
   }
   @Override
   public int hashCode() {
      return toString().hashCode();
   }

   public URI getContainerUrl() {
      return containerUrl;
   }
   public String getContainer() {
      return container;
   }
   public String getType() {
      return type;
   }
   public String getId() {
      return id;
   }
   public boolean isRelativeUri() {
      return isRelativeUri;
   }
   public boolean isExternalDatacoreUri() {
      return isExternalDatacoreUri;
   }
   public boolean isExternalWebUri() {
      return isExternalWebUri;
   }
   public boolean isExternalUri() {
      return isExternalUri;
   }
   
}
