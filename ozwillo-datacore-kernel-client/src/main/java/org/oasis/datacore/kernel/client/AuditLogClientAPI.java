package org.oasis.datacore.kernel.client;

import java.util.Map;

import org.joda.time.Instant;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface AuditLogClientAPI {
	
	 @Path("/l/event")
	 @POST
	 @Consumes(MediaType.APPLICATION_JSON)
	 public Response json(RemoteEvent remoteEvent);
	 
	 class RemoteEvent {
		 Instant time;
		 Map<String, Object> log;
		 
		 public RemoteEvent() {}
		 
		 public RemoteEvent(Instant t, Map<String, Object> l ) {
			 time = t;
			 log = l;
		 }
	
		 public Instant getTime() {
		   return time;
		 }
	
		 public Map<String, Object> getLog() {
		   return log;
		 }
	 }
	 
}