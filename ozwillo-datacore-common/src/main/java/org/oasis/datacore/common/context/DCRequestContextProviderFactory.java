package org.oasis.datacore.common.context;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Provides DCRequestContextProvider, by default SimpleRequestContextProvider
 * but allows  another impl (ex. CXF Exchange) to be injected if need be
 * @author mdutoo
 *
 */
@Component
public class DCRequestContextProviderFactory extends RequestContextProviderBase implements DCRequestContextProvider {
   
   private static DCRequestContextProviderFactory instance = null;
   
   @Autowired(required=false)
   private DCRequestContextProvider requestContextProvider = new SimpleRequestContextProvider<Object>();
   
   public DCRequestContextProviderFactory() {
      DCRequestContextProviderFactory.instance = this;
   }
   
   /** static access */
   public static DCRequestContextProvider getProvider() {
      return DCRequestContextProviderFactory.instance.requestContextProvider;
   }
   
   /** shortcut to getProvider()'s */
   @Override
   public Map<String, Object> getRequestContext() {
      return DCRequestContextProviderFactory.getProvider().getRequestContext();
   }
   
}
