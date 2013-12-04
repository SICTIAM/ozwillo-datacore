package org.oasis.datacore.rest.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.oasis.datacore.rest.api.util.DCURI;
import org.oasis.datacore.rest.api.util.UriHelper;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;


/**
 * A Datacore data Resource.
 * This class is the Java support for producing JSON-LD-like JSON out of Datacore data.
 * 
 * TODO patch date support by Jackson else Caused by: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field "weekOfWeekyear" (class org.joda.time.DateTime)
 *  
 * @author mdutoo
 *
 */
@ApiModel(value = "A Datacore data Resource")
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class DCResource {
   
   @ApiModelProperty(value = "URI", position=0, required=true)
   @JsonProperty
   private String uri;
   /*@JsonProperty
   private String iri; // TODO in addition to uri ?? test conflict !
   @JsonProperty
   private String type; // TODO types ??? in addition to uri ? or model ?!? test conflict !*/
   @ApiModelProperty(value = "version", position=1, notes="The server's up-to-date version must "
         + "be provided (save when creating it), otherwise it will fail due to optimistic locking.")
   @JsonProperty
   private Long version;

   /** types : model plus type mixins */
   @JsonProperty
   private List<String> types;

   // creation / last modified date, author ? (readonly !)
   @JsonProperty
   private DateTime created;
   @JsonProperty
   private DateTime lastModified;
   @JsonProperty
   private String createdBy;
   @JsonProperty
   private String lastModifiedBy;
   
   /** Other (business) properties. They are of the types supported by JSON (on Jackson) :
    * String, Boolean, Double, Map, List
    * see http://en.wikipedia.org/wiki/JSON#Data_types.2C_syntax_and_example */
   //@JsonIgnore // NO error 204 no content, rather not visible and explicitly @JsonProperty actual fields
   private Map<String,Object> properties;
   
   public DCResource() {
      this.properties = new HashMap<String,Object>();
      this.types = new ArrayList<String>();
   }
   public DCResource(Map<String,Object> properties) {
      this.properties = properties;
      this.types = new ArrayList<String>();
   }
   
   /** helper method to build new DCResources */
   public static DCResource create(String containerUrl, String modelType, String iri) {
      DCResource resource = new DCResource();
      resource.types.add(modelType);
      resource.setUri(UriHelper.buildUri(containerUrl, modelType, iri));
      return resource;
   }
   /** helper method to build new DCResources 
    * @throws URISyntaxException 
    * @throws MalformedURLException */
   public static DCResource create(String uri) throws MalformedURLException, URISyntaxException {
      DCResource resource = new DCResource();
      DCURI dcUri = DCURI.fromUri(uri);
      String modelType = dcUri.getType();
      resource.getTypes().add(modelType);
      resource.setUri(UriHelper.buildUri(dcUri.getContainer(), modelType, dcUri.getId()));
      return resource;
   }
   /** helper method to build new DCResources */
   public DCResource addType(String mixinType) {
      this.types.add(mixinType);
      return this;
   }
   /** helper method to build new DCResources */
   public DCResource set(String fieldName, String fieldValue) {
      this.properties.put(fieldName, fieldValue);
      return this;
   }

   // TODO to unmarshall embedded resources as DC(Sub)Resource rather than HashMaps
   // (and if possible same for embedded maps ???) BUT can't know when is embedded resource or map
   @JsonSubTypes({ @JsonSubTypes.Type(String.class), @JsonSubTypes.Type(Boolean.class),
      @JsonSubTypes.Type(Double.class), @JsonSubTypes.Type(DateTime.class),
      @JsonSubTypes.Type(Map.class), @JsonSubTypes.Type(List.class) })
   ///   @JsonSubTypes.Type(DCSubResource(Map).class), @JsonSubTypes.Type(DCList.class) })
   @JsonAnyGetter
   public Map<String, Object> getProperties() {
      return this.properties;
   }
   @JsonSubTypes({ @JsonSubTypes.Type(String.class), @JsonSubTypes.Type(Boolean.class),
      @JsonSubTypes.Type(Double.class), @JsonSubTypes.Type(DateTime.class),
      @JsonSubTypes.Type(Map.class), @JsonSubTypes.Type(List.class) })
   ///   @JsonSubTypes.Type(DCSubResource(Map).class), @JsonSubTypes.Type(DCList.class) })
   @JsonAnySetter
   public void setProperty(String name, Object value) {
      this.properties.put(name, value);
   }
   
   public String getUri() {
      return uri;
   }
   public void setUri(String uri) {
      this.uri = uri;
   }
   /*public String getIri() {
      return iri;
   }
   public void setIri(String iri) {
      this.iri = iri;
   }
   public String getType() {
      return type;
   }
   public void setType(String type) {
      this.type = type;
   }*/
   public Long getVersion() {
      return version;
   }
   public void setVersion(Long version) {
      this.version = version;
   }
   public List<String> getTypes() {
      return types;
   }
   public void setTypes(List<String> types) {
      this.types = types;
   }
   public DateTime getCreated() {
      return created;
   }
   public void setCreated(DateTime created) {
      this.created = created;
   }
   public DateTime getLastModified() {
      return lastModified;
   }
   public void setLastModified(DateTime lastModified) {
      this.lastModified = lastModified;
   }
   public String getCreatedBy() {
      return createdBy;
   }
   public void setCreatedBy(String createdBy) {
      this.createdBy = createdBy;
   }
   public String getLastModifiedBy() {
      return lastModifiedBy;
   }
   public void setLastModifiedBy(String lastModifiedBy) {
      this.lastModifiedBy = lastModifiedBy;
   }

   /**
    * TODO refactor to ResourceService, in order to rather
    * use the "right" ObjectMapper instance
    */
   public String toString() {
      try {
         return new ObjectMapper().writeValueAsString(this);
      } catch (JsonProcessingException e) {
         return "DCResource[" + this.uri + " , bad json]";
      }
   }
   
}
