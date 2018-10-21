package com.io7m.osgibrowse.catalog.xml;

import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXESchemaResolutionMappings;
import com.io7m.jxe.core.JXEXInclude;
import com.io7m.osgibrowse.catalog.api.OBCatalogParseError;
import com.io7m.osgibrowse.catalog.api.OBCatalogParserConfigurationException;
import com.io7m.osgibrowse.catalog.api.OBCatalogParserType;
import com.io7m.osgibrowse.catalog.api.OBCatalogType;
import com.io7m.osgibrowse.catalog.xml.v1.OBCatalogV1Schema;
import com.io7m.osgibrowse.catalog.xml.v1.OBCatalogXMLV1HandlerCatalog;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.Locator2;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class OBCatalogXML
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCatalogXML.class);

  private static final JXESchemaResolutionMappings SCHEMAS =
    JXESchemaResolutionMappings.builder()
      .putMappings(OBCatalogV1Schema.SCHEMA.namespace(), OBCatalogV1Schema.SCHEMA)
      .build();
  private final JXEHardenedSAXParsers parsers;

  public OBCatalogXML()
  {
    this.parsers = new JXEHardenedSAXParsers();
  }

  public OBCatalogParserType createParser(
    final OBCatalogXMLParserRequest request)
    throws OBCatalogParserConfigurationException
  {
    Objects.requireNonNull(request, "request");

    try {
      final XMLReader reader =
        this.parsers.createXMLReader(Optional.empty(), JXEXInclude.XINCLUDE_DISABLED, SCHEMAS);
      final HandlerInitial handler = new HandlerInitial(request, reader);
      reader.setContentHandler(handler);
      return new Parser(request, reader, handler);
    } catch (final ParserConfigurationException | SAXException e) {
      throw new OBCatalogParserConfigurationException(e.getMessage(), e);
    }
  }

  private static final class HandlerInitial extends DefaultHandler2
  {
    private final OBCatalogXMLParserRequest request;
    private final XMLReader reader;
    private final OBCatalogXMLErrorLog errors;
    private Locator2 locator;
    private OBCatalogXMLV1HandlerCatalog catalog_handler;

    HandlerInitial(
      final OBCatalogXMLParserRequest in_request,
      final XMLReader in_reader)
    {
      this.request = Objects.requireNonNull(in_request, "request");
      this.reader = Objects.requireNonNull(in_reader, "reader");
      this.reader.setErrorHandler(this);
      this.reader.setContentHandler(this);
      this.errors = new OBCatalogXMLErrorLog();
    }

    @Override
    public void setDocumentLocator(
      final Locator in_locator)
    {
      this.locator = Objects.requireNonNull((Locator2) in_locator, "Locator");
    }

    @Override
    public void warning(
      final SAXParseException e)
    {
      this.errors.warning(e);
    }

    @Override
    public void error(
      final SAXParseException e)
    {
      this.errors.error(e);
    }

    @Override
    public void fatalError(
      final SAXParseException e)
      throws SAXException
    {
      this.errors.fatalError(e);
      throw e;
    }

    @Override
    public void startDocument()
    {
      LOG.trace("startDocument");
    }

    @Override
    public void endDocument()
    {
      LOG.trace("endDocument");
    }

    @Override
    public void startPrefixMapping(
      final String prefix,
      final String uri)
      throws SAXException
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace("startPrefixMapping: {} {}", prefix, uri);
      }

      if (Objects.equals(uri, OBCatalogV1Schema.SCHEMA.namespace().toString())) {
        this.catalog_handler =
          new OBCatalogXMLV1HandlerCatalog(this.request, this.reader, this.errors, this.locator);
        this.reader.setContentHandler(this.catalog_handler);
        return;
      }

      throw new SAXParseException(
        "Unrecognized schema namespace URI: " + uri,
        null,
        this.request.file().toString(), 1, 0);
    }

    public OBCatalogType content()
    {
      return this.catalog_handler.content();
    }
  }

  private static final class Parser implements OBCatalogParserType
  {
    private final XMLReader reader;
    private final HandlerInitial handler;
    private final OBCatalogXMLParserRequest request;

    Parser(
      final OBCatalogXMLParserRequest in_request,
      final XMLReader in_reader,
      final HandlerInitial in_handler)
    {
      this.request = Objects.requireNonNull(in_request, "request");
      this.reader = Objects.requireNonNull(in_reader, "reader");
      this.handler = Objects.requireNonNull(in_handler, "handler");
    }

    @Override
    public Validation<Seq<OBCatalogParseError>, OBCatalogType> parse()
    {
      try {
        final InputSource source = new InputSource(this.request.stream());
        source.setSystemId(this.request.file().toString());
        this.reader.parse(source);

        if (this.handler.errors.errors().isEmpty()) {
          return Validation.valid(this.handler.content());
        }

      } catch (final SAXParseException e) {
        this.handler.errors.addError(OBCatalogXMLErrorLog.createErrorFromParseException(e));
      } catch (final SAXException | IOException e) {
        this.handler.errors.addError(
          OBCatalogXMLErrorLog.createErrorFromException(this.request.file(), e));
      }

      return Validation.invalid(this.handler.errors.errors());
    }

    @Override
    public void close()
      throws IOException
    {
      this.request.stream().close();
    }
  }
}
