package org.oasis.datacore.rest.server.parsing.service;

import org.oasis.datacore.core.meta.model.DCField;
import org.oasis.datacore.rest.server.parsing.exception.ResourceParsingException;
import org.oasis.datacore.rest.server.parsing.model.DCQueryParsingContext;
import org.oasis.datacore.rest.server.parsing.model.QueryOperatorsEnum;

/**
 * 
 * @author agiraudon
 * 
 */

public interface QueryParsingService {

   /**
    * Parses query parameter criteria to OperatorValues according to model field.
    * TODO LATER using ANTLR ?!?
    * recognizes MongoDB criteria (operators & values), see http://docs.mongodb.org/manual/reference/operator/query/
    * @param entityFieldPath
    * @param dcField
    * @param operatorAndValue
    * @param queryParsingContext
    * @throws ResourceParsingException
    */
   void parseQueryParameter(String entityFieldPath, DCField dcField, String operatorAndValue,
         DCQueryParsingContext queryParsingContext) throws ResourceParsingException;

   /**
    * Visits its OperatorValues and builds Spring Criteria out of them
    * @param queryParsingContext
    */
   void buildCriteria(DCQueryParsingContext queryParsingContext);

	/**
	 * NOT USED
	 * Parses using the operatorEnum.parsingType if any, else the given dcField type
	 * @param operatorEnum
	 * @param dcField
	 * @param value
	 * @return
	 * @throws ResourceParsingException
	 */
	Object parseQueryValue(QueryOperatorsEnum operatorEnum, DCField dcField,
         String value) throws ResourceParsingException;
	
}
