package org.oasis.datacore.core.meta.model;

import java.util.LinkedHashSet;


/**
 * i18n field, as a list of l (set key) to v maps
 * NB. "required" field has no meaning here (would be within a given language and even then...)
 * TODO LATER default language in more context than global & model : request, user...
 * query limit : taken as biggest among its "v" list subfield and the field itself
 * @author mdutoo
 *
 */
public class DCI18nField extends DCListField {

   /** Use it from ResourceEntityMapperService.getDefaultLanguage(field), for now */
   public static final String DEFAULT_LANGUAGE = "en";
   
   public static final String KEY_LANGUAGE = "l";
   public static final String KEY_VALUE = "v";
   
   /** NOT USED YET Model-level default language if any ; NB. "required" means "default language required" for i18n field */
   private String defaultLanguage = null;
   /* Whether any Resource-level default language ; NB. "required" means "default language required" for i18n field */
   //private String hasResourceLevelDefaultLanguage = null;

   /** for unmarshalling only */
   public DCI18nField() {
      
   }
   
   /**
    * TODO LATER define how "required" applies to i18n (ex. required only for model/resource "default" language)
    * @param name
    */
   public DCI18nField(String name, int queryLimit) {
      super(name, "i18n", createI18nMap(queryLimit));
      /*super(name, "i18", false, 0);
      
      DCMapField i18Map = new DCMapField("zzz");
      i18Map.addField(new DCField("v", "string", true, 100));
      i18Map.addField(new DCField("l", "string", false, 0));
      this.list = new DCListField("i18List", i18Map);*/
   }
   public DCI18nField(String name, int queryLimit, LinkedHashSet<String> aliasedStorageNames) {
      super(name, "i18n", createI18nMap(queryLimit));
      this.setAliasedStorageNames(aliasedStorageNames);
      // set list conf :
      this.setIsSet(true);
      this.setKeyFieldName(KEY_LANGUAGE);
   }
   public DCI18nField(String name, int queryLimit, String singleAliasedStorageName) {
      super(name, "i18n", createI18nMap(queryLimit));
      this.setSingleAliasedStorageName(singleAliasedStorageName);
      // set list conf :
      this.setIsSet(true);
      this.setKeyFieldName(KEY_LANGUAGE);
   }
   public DCI18nField(String name, int queryLimit, String singleAliasedStorageName, boolean readonly) {
      this(name, queryLimit, singleAliasedStorageName);
      this.setReadonly(readonly);
   }
   
   private static DCMapField createI18nMap(int valueQueryLimit) {
      DCMapField i18Map = new DCMapField("i18nMap"); // NB. this map name is meaningless
      i18Map.addField(new DCField(KEY_VALUE, "string", true, valueQueryLimit));
      i18Map.addField(new DCField(KEY_LANGUAGE, "string", false, 0)); // no index BUT queriable !!
      return i18Map;
   }
   
   
   // NOO rather in DCResource & ResourceService
   public void addTranslation(String lang, String text) {
      //HashMap<String, String> mapField = new HashMap<String, String>();
      //new DCField("t", "string", false, 10);
      //new DCField("v", "string", false, 10);
   }

   public String getDefaultLanguage() {
      return defaultLanguage;
   }

   public DCI18nField setDefaultLanguage(String defaultLanguage) {
      this.defaultLanguage = defaultLanguage;
      return this;
   }

   public void setFulltext(boolean fulltext) {
      // also setting on value field, because checked at ex. query parsing :
      // (TODO LATER rather introspect model upwards)
      DCField valueField = ((DCMapField) this.getListElementField()).getMapFields().get(KEY_VALUE);
      valueField.setFulltext(fulltext);
      super.setFulltext(fulltext);
   }
   
}
