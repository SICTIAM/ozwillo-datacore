package org.oasis.datacore.rest.api;

import java.net.URL;
import java.net.URLEncoder;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.oasis.datacore.rest.api.binding.DatacoreObjectMapper;
import org.oasis.datacore.rest.api.util.ResourceParsingHelper;

public class DatacoreApiUtilTest {
   
   @Test
   public void testEncodingLibs() throws Exception {
      String value = "\"Bordeaux&= +.;#~_\"";
      String jdkEncodedValue = "name=" + URLEncoder.encode(value, "UTF-8");
      Assert.assertEquals("JDK and CXF should do same URL encoding", "name=%22Bordeaux%26%3D+%2B.%3B%23%7E_%22", jdkEncodedValue);
   }
   
   @Test
   public void testUrlEncoding() throws Exception {
      String unencodedUrlString = "https://data.ozwillo.com/dc/type/geocibg:НаселеноMесто_0?odisp:name=$fulltextСофия&geocibg:есто=есто:есто";
      URL url = new URL(unencodedUrlString);
      String urlString = url.toURI().toASCIIString(); // encoded
      Assert.assertEquals("https://data.ozwillo.com/dc/type/geocibg:%D0%9D%D0%B0%D1%81%D0%B5%D0%BB%D0%B5%D0%BD%D0%BEM%D0%B5%D1%81%D1%82%D0%BE_0"
            + "?odisp:name=$fulltext%D0%A1%D0%BE%D1%84%D0%B8%D1%8F&geocibg:%D0%B5%D1%81%D1%82%D0%BE=%D0%B5%D1%81%D1%82%D0%BE:%D0%B5%D1%81%D1%82%D0%BE", urlString);
   }
   
   @Test
   public void testDate() throws Exception {
      // preparing
      DatacoreObjectMapper mapper = new DatacoreObjectMapper();
      DateTime testDate1 = new DateTime(); // 2014-01-08T10:31:19.062+01:00
      String testDate1StringWithLocale = mapper.writer().writeValueAsString(testDate1); // "2014-01-08T10:31:19.062+01:00"
      DateTime testDate1ReadGMT = mapper.reader(DateTime.class).readValue(testDate1StringWithLocale); // 2014-01-08T09:31:19.062Z
      // or could directly create date with GMT locale
      
      // by jackson
      String testDate1ReadStringGMT = mapper.writer().writeValueAsString(testDate1ReadGMT); // 2014-01-08T09:31:19.062Z
      DateTime testDate1ReadStringGMTReadGMT = mapper.reader(DateTime.class).readValue(testDate1ReadStringGMT); // 2014-01-08T09:31:19.062Z
      Assert.assertEquals("Date should be written & read fine by Jackson mapper", testDate1ReadGMT, testDate1ReadStringGMTReadGMT); 
      
      // like jackson joda DateTimeDeserializer 
      String testDate1ReadWrittenByJodaStringGMT = testDate1ReadGMT.toString(); // 2014-01-08T09:31:19.062Z
      DateTime testDate1ReadWrittenByJodaStringGMTRead = ResourceParsingHelper.parseDate(testDate1ReadWrittenByJodaStringGMT); // 2014-01-08T09:31:19.062Z
      Assert.assertEquals("Date should be written & read the same by Jackson mapper & Joda", testDate1ReadGMT, testDate1ReadWrittenByJodaStringGMTRead);
   }
   
   @Test
   public void testLong() throws Exception {
      Long testLong1 = 1234535678996789900l;
      String testLong1String = testLong1.toString();
      Long testLong1StringRead = ResourceParsingHelper.parseLong(testLong1String);
      Assert.assertEquals("Long should be written & read fine as String", testLong1, testLong1StringRead);
   }
   
}
