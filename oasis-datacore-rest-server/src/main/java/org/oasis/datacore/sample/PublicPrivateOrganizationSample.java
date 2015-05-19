package org.oasis.datacore.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.oasis.datacore.common.context.DCRequestContextProvider;
import org.oasis.datacore.common.context.SimpleRequestContextProvider;
import org.oasis.datacore.core.meta.model.DCField;
import org.oasis.datacore.core.meta.model.DCMixin;
import org.oasis.datacore.core.meta.model.DCModel;
import org.oasis.datacore.core.meta.model.DCModelBase;
import org.oasis.datacore.core.meta.model.DCResourceField;
import org.oasis.datacore.core.meta.model.DCSecurity;
import org.oasis.datacore.core.meta.pov.DCProject;
import org.oasis.datacore.model.resource.ModelResourceMappingService;
import org.oasis.datacore.rest.api.DCResource;
import org.oasis.datacore.rest.api.util.UriHelper;
import org.oasis.datacore.rest.server.parsing.exception.ResourceParsingException;
import org.oasis.datacore.rest.server.resource.ResourceException;
import org.oasis.datacore.rest.server.resource.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;


/**
 * Draft of Public & Private Organization Models, to help test copy to one from the other.
 * 
 * @author mdutoo
 *
 */
@Component
public class PublicPrivateOrganizationSample extends DatacoreSampleMethodologyBase {
   
   public static final String ORG1 = "org1:Organization_0";
   public static final String ORGPRI1 = "orgpri1:Organization_0";
   public static final String ORG2 = "org2:Organization_0";
   public static final String ORGPRI2 = ORG2; /// "orgpri2:Organization_0";
   public static final String ORGPRI2_PROJECT = "orgpri2";

   /** to POST models */
   @Autowired
   private ModelResourceMappingService mrMappingService;
   
   @Override
   public void doOSelectPerimeter() {
      
   }
   

   // see doc in overriden
   @Override
   public void do1DesignFlatModel() {
   }


   // see doc in overriden
   @Override
   public void do2ExternalizeModelAsMixins() {
   }


   // see doc in overriden
   @Override
   public void do2CreateModels(List<DCModelBase> modelsToCreate) {
      // org(pri)1 : orgpri (reference) and inherits org (else can't query on both pub & pri fields) and links org, both storage
      
      DCModelBase org1 = new DCModel(ORG1)
      /*ignCommuneModel.setDocumentsetDocumentationation("{ \"uri\": \"http://localhost:8180/dc/type/country/France\", "
            + "\"name\": \"France\" }");*/
         .addField(new DCField("org1:jurisdiction", "string", true, 100)) // TODO actually geoco, in oasis.main or visible geo.main
         .addField(new DCField("org1:regNumber", "string", true, 100))
         .addField(new DCField("org1:activity", "string", true, 100)) // actually orgact
         .addField(new DCField("org1:name", "string", true, 100));
      org1.setDocumentation("see xls : id = org:jurisdiction/org:name");
      org1.setStorage(true);
      org1.setInstanciable(true);
      
      DCModelBase orgpri1 = new DCModel(ORGPRI1)
      /*ignCommuneModel.setDocumentsetDocumentationation("{ \"uri\": \"http://localhost:8180/dc/type/country/France\", "
            + "\"name\": \"France\" }");*/
         .addMixin(org1)
         // fields to help publishing to pub org :
         .addField(new DCResourceField("orgpri1:org", org1.getName(), false, 100)) // link to pub org
         .addField(new DCField("orgpri1:publishedVersion", "long", false, 0)) // version of orgpri last copied to pub org
         .addField(new DCField("orgpri1:state", "string", false, 100)) // "published" or "modified" (or "new", "deleted" ??)
         // orgpri specific business fields :
         .addField(new DCField("orgpri1:employeeNb", "int", false, 0)); // or any other private field, TODO Q which ones ??
      orgpri1.setDocumentation("see xls : id = org:jurisdiction/org:name");
      orgpri1.setStorage(true);
      orgpri1.setInstanciable(true);
      
      /*datacoreApiClient.*//*postDataInType(Arrays.asList(org1, orgpri1).stream().map(
            m -> mrMappingService.modelToResource(model, null)).collect(Collectors.toList()));*/
         
      
      // org(pri)2 : idem but same name and orgpub hides orgpri when accessed from pov or "front" project
      // (orgpri could be in "back" project visible from oasis.main but then its deps ex. geo must be made visible
      // from their own project, and beware when upgrading them to new versions)
      
      DCModelBase org2 = new DCModel(ORG2)
      /*ignCommuneModel.setDocumentsetDocumentationation("{ \"uri\": \"http://localhost:8180/dc/type/country/France\", "
            + "\"name\": \"France\" }");*/
         .addField(new DCField("org2:jurisdiction", "string", true, 100)) // TODO actually geoco, in oasis.main or visible geo.main
         .addField(new DCField("org2:regNumber", "string", true, 100))
         .addField(new DCField("org2:activity", "string", true, 100)) // actually orgact
         .addField(new DCField("org2:name", "string", true, 100));
      org2.setDocumentation("see xls : id = org:jurisdiction/org:name");
      org2.setStorage(true);
      org2.setInstanciable(true);
      
      DCModelBase orgpri2 = new DCModel(ORGPRI2, ORGPRI2_PROJECT) // NB. in another project !
      /*ignCommuneModel.setDocumentsetDocumentationation("{ \"uri\": \"http://localhost:8180/dc/type/country/France\", "
            + "\"name\": \"France\" }");*/
         .addMixin(org2)
         // fields to help publishing to pub org :
         ///.addField(new DCResourceField("orgpri2:org", org2.getName(), false, 100)) // link to pub org NOOO ARE THE SAME
         .addField(new DCField("orgpri2:publishedVersion", "long", false, 0)) // version of orgpri last copied to pub org
         .addField(new DCField("orgpri2:state", "string", false, 100)) // "published" or "modified" (or "new", "deleted" ??)
         // orgpri specific business fields :
         .addField(new DCField("orgpri2:employeeNb", "int", false, 0)); // or any other private field, TODO Q which ones ??
      orgpri2.setDocumentation("see xls : id = org:jurisdiction/org:name");
      orgpri2.setStorage(true);
      orgpri2.setInstanciable(true);

      DCProject mainProject = modelAdminService.getProject(DCProject.OASIS_MAIN);
      // first create the project
      DCProject orgpri2Project = new DCProject(ORGPRI2_PROJECT);
      orgpri2Project.addLocalVisibleProject(mainProject);
      modelAdminService.addProject(orgpri2Project);
      mainProject.addLocalModel(org2); // TODO TODO TODO else error On model orgpri2:Organization_0 in project orgpri2: Unable to add model to project orgpri2 with visible projects {}, this model has unknown mixins : [org2:Organization_0]
      // only then add model (else dep models not visible)
      DCModelBase storageMetamodel = this.fork(
            modelAdminService.getModelBase(ResourceModelIniter.MODEL_MODEL_NAME),
            orgpri2Project, false, false, true, false);
      orgpri2Project.addLocalModel(orgpri2);
      ///orgpri2.setPointOfViewNames(Arrays.asList(mainProject.getName()));
      
      
      // org3 : org is model-visible (to authentified users since pub) and has the orgpri mixin which
      // has its own DCSecurity (NOT model-visible to authentified etc.) and uses the Resource-level
      // rights for itself to achieve "personal data" behaviour
      
      // orgx : pov ?

      
      // NB. for now mixins must be added BEFORE models they're added to !!!
      modelsToCreate.addAll(Arrays.asList(storageMetamodel,
            org1, orgpri1, org2, // even if already added
            orgpri2)); // NB. has to be created in another project !
   }


   // see doc in overriden
   @Override
   public void do3FillReferenceData() throws Exception {
      
   }


   // see doc in overriden
   @Override
   public void do4FillSampleData() throws WebApplicationException {
      try {
         doInitSampleDataOrg1();
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      try {
         doInitSampleDataOrg2();
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }


   private void doInitSampleDataOrg1() throws Exception {
      List<DCResource> resourcesToPost = new ArrayList<DCResource>();

      String owUri = UriHelper.buildUri(containerUrl, ORG1, "FR/Open Wide");
      try {
         resourceService.get(owUri, ORG1);
      } catch (ResourceNotFoundException rnfex) {
         /*if (Response.Status.NOT_FOUND.getStatusCode() != waex.getResponse().getStatus()) {
            throw new RuntimeException("Unexpected error", waex);
         }*/
         DCResource ow = DCResource.create(owUri)
               .set("org1:name", "Open Wide")
               .set("org1:jurisdiction", "FR")
               .set("org1:regNumber", "12347366")
               .set("org1:activity", "722");
               
         // once props are complete, build URI out of them and schedule post :
         ///country.setUriFromId(containerUrl, (String) country.get("plo:name"));
         resourcesToPost.add(ow);
      }

      String owPriUri = UriHelper.buildUri(containerUrl, ORGPRI1, "FR/Open Wide");
      try {
         resourceService.get(owPriUri, ORGPRI1);
      } catch (ResourceNotFoundException rnfex) {
         /*if (Response.Status.NOT_FOUND.getStatusCode() != waex.getResponse().getStatus()) {
            throw new RuntimeException("Unexpected error", waex);
         }*/
         DCResource owPri = DCResource.create(owPriUri)
               .set("org1:name", "Open Wide")
               .set("org1:jurisdiction", "FR")
               .set("org1:regNumber", "12347366")
               .set("org1:activity", "722")
               .set("orgpri1:org", owUri)
               .set("orgpri1:publishedVersion", 0)
               .set("orgpri1:state", "published")
               .set("orgpri1:employeeNb", 130);
         // once props are complete, build URI out of them and schedule post :
         ///country.setUriFromId(containerUrl, (String) country.get("plo:name"));
         resourcesToPost.add(owPri);
      }

      String pnUri = UriHelper.buildUri(containerUrl, ORG1, "Pôle Numérique");
      try {
         resourceService.get(pnUri, "plo:country_0");
      } catch (ResourceNotFoundException rnfex) {
         /*if (Response.Status.NOT_FOUND.getStatusCode() != waex.getResponse().getStatus()) {
            throw new RuntimeException("Unexpected error", waex);
         }*/
         DCResource pn = DCResource.create(pnUri)
               .set("org1:name", "Pôle Numérique")
               .set("org1:jurisdiction", "FR")
               .set("org1:regNumber", "12347367")
               .set("org1:activity", "111")/*
               .set("orgpri1:org", pnUri)
               .set("orgpri1:publishedVersion", 0)
               .set("orgpri1:state", "published")
               .set("orgpri1:employeeNb", 10)*/;
         // once props are complete, build URI out of them and schedule post :
         ///country.setUriFromId(containerUrl, (String) country.get("plo:name"));
         resourcesToPost.add(pn);
      }

      for (DCResource resource : resourcesToPost) {
         /*datacoreApiClient.*/postDataInType(resource);
      }
   }
   



   private void doInitSampleDataOrg2() throws Exception {
      String owUri = UriHelper.buildUri(containerUrl, ORG2, "FR/Open Wide");
      try {
         resourceService.get(owUri, ORG2);
      } catch (ResourceNotFoundException rnfex) {
         /*if (Response.Status.NOT_FOUND.getStatusCode() != waex.getResponse().getStatus()) {
            throw new RuntimeException("Unexpected error", waex);
         }*/
         DCResource ow = DCResource.create(owUri)
               .set("org2:name", "Open Wide")
               .set("org2:jurisdiction", "FR")
               .set("org2:regNumber", "12347366")
               .set("org2:activity", "722");
               
         // once props are complete, build URI out of them and schedule post :
         ///country.setUriFromId(containerUrl, (String) country.get("plo:name"));
         /*datacoreApiClient.*/postDataInType(ow);
      }

      String owPriUri = UriHelper.buildUri(containerUrl, ORGPRI2, "FR/Open Wide");
      try {
         new SimpleRequestContextProvider<DCResource>() { // set context project beforehands :
            protected DCResource executeInternal() throws ResourceException {
               return resourceService.get(owPriUri, ORGPRI2);
            }
         }.execInContext(new ImmutableMap.Builder<String, Object>()
               .put(DCRequestContextProvider.PROJECT, ORGPRI2_PROJECT).build());
         
      } catch (RuntimeException rex) {
         if (!(rex.getCause() instanceof ResourceNotFoundException)) {
            throw rex;
         }
         /*if (Response.Status.NOT_FOUND.getStatusCode() != waex.getResponse().getStatus()) {
            throw new RuntimeException("Unexpected error", waex);
         }*/
         DCResource owPri = DCResource.create(owPriUri)
               .set("org2:name", "Open Wide")
               .set("org2:jurisdiction", "FR")
               .set("org2:regNumber", "12347366")
               .set("org2:activity", "722")
               ///.set("orgpri2:org", owUri) /// NOO ARE THE SAME
               .set("orgpri2:publishedVersion", 0)
               .set("orgpri2:state", "published")
               .set("orgpri2:employeeNb", 130);
         // once props are complete, build URI out of them and schedule post :
         ///country.setUriFromId(containerUrl, (String) country.get("plo:name"));
         
         new SimpleRequestContextProvider<Object>() { // set context project beforehands :
            protected Object executeInternal() throws ResourceException {
               /*datacoreApiClient.*/postDataInType(owPri); return null;
            }
         }.execInContext(new ImmutableMap.Builder<String, Object>()
               .put(DCRequestContextProvider.PROJECT, ORGPRI2_PROJECT).build());
      }

      String pnUri = UriHelper.buildUri(containerUrl, ORG2, "Pôle Numérique");
      try {
         resourceService.get(pnUri, "plo:country_0");
      } catch (ResourceNotFoundException rnfex) {
         /*if (Response.Status.NOT_FOUND.getStatusCode() != waex.getResponse().getStatus()) {
            throw new RuntimeException("Unexpected error", waex);
         }*/
         DCResource pn = DCResource.create(pnUri)
               .set("org2:name", "Pôle Numérique")
               .set("org2:jurisdiction", "FR")
               .set("org2:regNumber", "12347367")
               .set("org2:activity", "111")/*
               ///.set("orgpri2:org", pnUri) /// NOOO ARE THE SAME
               .set("orgpri2:publishedVersion", 0)
               .set("orgpri2:state", "published")
               .set("orgpri2:employeeNb", 10)*/;
         // once props are complete, build URI out of them and schedule post :
         ///country.setUriFromId(containerUrl, (String) country.get("plo:name"));
         /*datacoreApiClient.*/postDataInType(pn);
      }
   }


   /**
    * NB. can't have several at once
    * @param model
    * @param forkModel
    * @param overrideModel
    * @param forkData
    * @param readonlyData
    * @return
    * @throws RuntimeException : ResourceException if hard fork & fails
    */
   public DCModelBase fork(DCModelBase model, DCProject inProject, boolean forkModel, boolean overrideModel,
         boolean forkData, boolean readonlyData) throws RuntimeException {
      DCModelBase forkedModel = inProject.getLocalModel(model.getName());
      if (forkedModel != null) {
         throw new RuntimeException("already forked"); // TODO LATER change
      }
      
      if (!forkModel && !overrideModel && !forkData && !readonlyData) {
         // i.e. fully visible : same model
         return model;
      }
      
      if (forkModel) { // and data
         DCResource mr;
         try {
            mr = mrMappingService.modelToResource(model, null);
            forkedModel = mrMappingService.toModelOrMixin(mr);
         } catch (ResourceParsingException | ResourceException e) {
            throw new RuntimeException("Error while hard forking model "
                  + model.getName() + " in project " + inProject.getName(), e);
         }
         
      } else {
         forkedModel = new DCModel(model.getName(), inProject.getName());
         forkedModel.addMixin(model);
      
         if (overrideModel) { // and data
            forkedModel.setStorage(true);
         } else if (forkData) {
            // TODO TODO mark this model's resource as readonly in this project
            forkedModel.setStorage(true);
         } else if (readonlyData) {
            DCSecurity forkedSecurity = new DCSecurity(model.getSecurity());
            // TODO mark this model's resources as readonly from this project
            ///forkedSecurity.setResourceReadonly(true); // TODO
            forkedModel.setSecurity(forkedSecurity);
            // TODO DCSecurity inheritance !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         } // else already handled
      }
      
      inProject.addLocalModel(forkedModel);
      return forkedModel;
   }
   
}
