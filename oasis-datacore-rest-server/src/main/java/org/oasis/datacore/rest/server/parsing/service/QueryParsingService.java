package org.oasis.datacore.rest.server.parsing.service;

import org.oasis.datacore.core.meta.model.DCField;
import org.oasis.datacore.core.meta.model.DCFieldTypeEnum;
import org.oasis.datacore.rest.server.parsing.exception.ResourceParsingException;
import org.oasis.datacore.rest.server.parsing.model.DCQueryParsingContext;

/**
 * 
 * @author agiraudon
 * 
 */

public interface QueryParsingService {

	public void parseCriteriaFromQueryParameter(String fieldPath, String operatorAndValue, DCField dcField, DCQueryParsingContext queryParsingContext) throws ResourceParsingException;

	public void addSort(String fieldPath, String operatorAndValue, DCQueryParsingContext queryParsingContext);
	
	public Object parseValue(DCFieldTypeEnum dcFieldTypeEnum, String queryValue) throws ResourceParsingException;
		
}