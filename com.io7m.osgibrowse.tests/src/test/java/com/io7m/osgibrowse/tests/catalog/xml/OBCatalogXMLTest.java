package com.io7m.osgibrowse.tests.catalog.xml;

import com.io7m.osgibrowse.catalog.api.OBCatalogParseError;
import com.io7m.osgibrowse.catalog.api.OBCatalogParserType;
import com.io7m.osgibrowse.catalog.api.OBCatalogType;
import com.io7m.osgibrowse.catalog.xml.OBCatalogXMLParserRequest;
import com.io7m.osgibrowse.catalog.xml.OBCatalogXML;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public final class OBCatalogXMLTest
{
  private static final Logger LOG = LoggerFactory.getLogger(OBCatalogXMLTest.class);

  private static void dump(
    final Validation<Seq<OBCatalogParseError>, OBCatalogType> result)
  {
    if (result.isValid()) {
      LOG.debug("result: {}", result.get());
    } else {
      result.getError().forEach(e -> LOG.error("result error: {}", e));
    }
  }

  private static OBCatalogXMLParserRequest request(final String file)
    throws Exception
  {
    final URL url = OBCatalogXMLTest.class.getResource("/com/io7m/osgibrowse/tests/catalog/xml/" + file);
    return OBCatalogXMLParserRequest.of(url.toURI(), url.openStream());
  }

  @Test
  public void testEmpty()
    throws Exception
  {
    final OBCatalogXML v1 = new OBCatalogXML();

    final OBCatalogParserType parser = v1.createParser(request("empty.xml"));
    final Validation<Seq<OBCatalogParseError>, OBCatalogType> result = parser.parse();
    dump(result);
    Assertions.assertTrue(result.isValid());
    final OBCatalogType catalog = result.get();
    Assertions.assertEquals(0, catalog.repositories().size());
  }

  @Test
  public void testInvalid0()
    throws Exception
  {
    final OBCatalogXML v1 = new OBCatalogXML();

    final OBCatalogParserType parser = v1.createParser(request("invalid-0.xml"));
    final Validation<Seq<OBCatalogParseError>, OBCatalogType> result = parser.parse();
    dump(result);
    Assertions.assertTrue(result.isInvalid());
    final Seq<OBCatalogParseError> errors = result.getError();
  }
}
