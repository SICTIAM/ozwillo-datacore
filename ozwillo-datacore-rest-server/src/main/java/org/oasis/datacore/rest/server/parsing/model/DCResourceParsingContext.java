package org.oasis.datacore.rest.server.parsing.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.oasis.datacore.core.meta.model.DCField;
import org.oasis.datacore.core.meta.model.DCModelBase;
import org.oasis.datacore.rest.api.util.DCURI;


/**
 * Keeps the state of parsing (path in model instance) in order to be able
 * to display error messages that pinpoint to error location.
 * 
 * TODO LATER OPT less context for performance unless enabled in request context ?
 * 
 * @author mdutoo
 *
 */
public class DCResourceParsingContext {
   
   protected Stack<DCResourceValue> resourceValueStack = new Stack<DCResourceValue>(); // TODO or of String only ??
   protected HashSet<DCURI> embeddedUriSet = new HashSet<DCURI>();

   // TODO or same list, with error level in Log and boolean hasError ?!?
   private List<ResourceParsingLog> errors = null;
   private List<ResourceParsingLog> warnings = null;
   
   /**
    * For multi resource parsing, requires enter(DCModel, uri) first
    */
   public DCResourceParsingContext() {
      
   }
   
   /**
    * For single resource parsing
    * @param model
    * @param uri
    */
   public DCResourceParsingContext(DCModelBase model, DCModelBase storageModel, DCURI uri) {
      this.addEmbeddedUri(uri);
      this.enter(model, storageModel, uri);
   }
   
   public DCModelBase peekModel() {
      for (int i = this.resourceValueStack.size() - 1 ; i >= 0 ; i--) {
         DCModelBase stackedModel = this.resourceValueStack.get(i).getModel();
         if (stackedModel != null) {
            return stackedModel;
         }
      }
      // (and not using stack.peek() because is would be null for embedded subresource ex. i18n,
      // nor by copying it because allows to know non embedded subresources)
      return null;
   }
   
   public DCModelBase peekStorageModel() {
      for (int i = this.resourceValueStack.size() - 1 ; i >= 0 ; i--) {
         DCModelBase stackedStorageModel = this.resourceValueStack.get(i).getStorageModel();
         if (stackedStorageModel != null) {
            return stackedStorageModel;
         }
      }
      // (and not using stack.peek() because is would be null for embedded subresource ex. i18n,
      // nor by copying it because allows to know non embedded subresources)
      return null;
   }
   
   public boolean hasEmbeddedUri(DCURI dcUri) {
      return embeddedUriSet.contains(dcUri);
   }
   
   public void addEmbeddedUri(DCURI dcUri) {
      embeddedUriSet.add(dcUri);
   }
   
   public boolean isTopLevel() {
      return this.resourceValueStack.size() == 1; // i.e. only done yet enter(model, storageModel, uri)
   }
   
   
   /**
    * 
    * @param model
    * @param storageModel
    * @param uri may be null if query
    */
   public void enter(DCModelBase model, DCModelBase storageModel, DCURI uri) {
      ///this.resourceValueStack.add(new DCResourceValue(model.getName() + '[' + id + ']', null, id));
      this.resourceValueStack.add(new DCResourceValue(null, model, storageModel, null, uri));
   }
   
   public DCResourceValue peekResourceValue() {
      return this.resourceValueStack.peek();
   }

   /**
    * For within list only
    * @param field list element field
    * @param value
    * @param index
    */
   public void enter(DCModelBase model, DCModelBase storageModel, DCField field, Object value, long index) {
      // TODO LATER OPT less context for performance unless enabled in request context ?
      DCResourceValue previousResourceValue = null;
      if (!this.resourceValueStack.isEmpty()) {
         previousResourceValue = this.resourceValueStack.peek();
      }
      if ("resource".equals(field.getType())) {
         /*if (value instanceof String) {
            throw new RuntimeException("If remote resource, pass its URI as DCURI rather than String");
         } else if (value instanceof DCURI) {
            ///storageModel = null; // TODO
         } else {
            // embedded resource
            ///embeddedUriSet.add(e);
         }*/
      }
      this.resourceValueStack.add(new DCResourceValue(previousResourceValue, model, storageModel, field,  value, index));
   }
   public void enter(DCModelBase model, DCModelBase storageModel, DCField field, Object value) {
      String fullValuedPath;
      DCResourceValue previousResourceValue = null;
      if (this.resourceValueStack.isEmpty()) {
         fullValuedPath = "Missing root model name";
      } else {
         // TODO LATER OPT less context for performance unless enabled in request context ?
         previousResourceValue = this.resourceValueStack.peek();
         /*fullValuedPath = previousResourceValue.getFullValuedPath() + "/";
         if (previousResourceValue.getField() == null
               || !"list".equals(previousResourceValue.getField().getType())) {
            fullValuedPath = previousResourceValue.getFullValuedPath() + "/" + field.getName();
         } // else list element field with useless name
         if (!(value instanceof List<?> || value instanceof Map<?,?>)) {
            fullValuedPath += "[" + ((value == null) ? "null" : ((value instanceof String) ?
                  "'" + value + "'" : value)) + "]";
         }*/
      }
      if ("resource".equals(field.getType())) {
         /*if (value instanceof String) {
            throw new RuntimeException("If remote resource, pass its URI as DCURI rather than String");
         } else if (value instanceof DCURI) {
            ///storageModel = null; // TODO
         } else {
            // embedded resource
            ///embeddedUriSet.add(e);
         }*/
      }
      this.resourceValueStack.add(new DCResourceValue(previousResourceValue, model, storageModel, field,  value));
   }
   public void exit() {
      this.resourceValueStack.pop();
   }
   
   public void addError(String message) {
      this.addError(message, null);
   }

   public void addError(String message, Exception ex) {
      String fieldFullPath = this.resourceValueStack.peek().getFullValuedPath();
      this.getOrCreateErrors().add(new ResourceParsingLog(fieldFullPath, message, ex));
   }

   public boolean hasErrors() {
      return this.errors != null;
   }
   
   public List<ResourceParsingLog> getErrors() {
      return this.errors;
   }
   
   public void addWarning(String message) {
      this.addWarning(message, null);
   }

   public void addWarning(String message, Exception ex) {
      String fieldFullPath = this.resourceValueStack.peek().getFullValuedPath();
      this.getOrCreateWarnings().add(new ResourceParsingLog(fieldFullPath, message, ex));
   }

   public boolean hasWarnings() {
      return this.warnings != null;
   }
   
   public List<ResourceParsingLog> getWarnings() {
      return this.warnings;
   }
   
   private List<ResourceParsingLog> getOrCreateErrors() {
      if (this.errors == null) {
         this.errors = new ArrayList<ResourceParsingLog>();
      }
      return this.errors;
   }

   private List<ResourceParsingLog> getOrCreateWarnings() {
      if (this.warnings == null) {
         this.warnings = new ArrayList<ResourceParsingLog>();
      }
      return this.warnings;
   }
   

   // TODO extract to ResourceParsingService
   public static String formatParsingErrorsMessage(DCResourceParsingContext resourceParsingContext,
         boolean detailedErrorsMode) {
      // TODO or render (HTML ?) template ?
      StringBuilder sb = new StringBuilder("Parsing aborted, found "
            + resourceParsingContext.getErrors().size() + " errors "
            + ((resourceParsingContext.hasWarnings()) ? "(and "
            + resourceParsingContext.getWarnings().size() + " warnings) " : "")
            + ".\nErrors:");
      for (ResourceParsingLog error : resourceParsingContext.getErrors()) {
         //sb.append("\n   - in context "); // too long
         sb.append("\n - ");
         String fieldFullPath = error.getFieldFullPath();
         if (fieldFullPath != null && !fieldFullPath.isEmpty()) {
            sb.append(fieldFullPath);
            sb.append(" : ");
         } // else ex. root of a query
         sb.append(error.getMessage());
         if (error.getException() != null) {
            sb.append(". Exception message : ");
            sb.append(error.getException().getMessage());
            if (detailedErrorsMode) {
               sb.append("\n      Exception details : \n\n");
               sb.append(ExceptionUtils.getFullStackTrace(error.getException()));
               sb.append("\n");
            }
         }
      }
      
      if (resourceParsingContext.hasWarnings()) {
         // TODO or render (HTML ?) template ?
         sb.append("\nWarnings:");
         for (ResourceParsingLog warning : resourceParsingContext.getWarnings()) {
            sb.append("\n   - for field ");
            sb.append(warning.getFieldFullPath());
            sb.append(" : ");
            sb.append(warning.getMessage());
            if (warning.getException() != null) {
               sb.append(". Exception message : ");
               sb.append(warning.getException().getMessage());
               if (detailedErrorsMode) {
                  sb.append("\n      Exception details : \n\n");
                  sb.append(ExceptionUtils.getFullStackTrace(warning.getException()));
                  sb.append("\n");
               }
            }
         }
      }
      
      return sb.toString();
   }

}
