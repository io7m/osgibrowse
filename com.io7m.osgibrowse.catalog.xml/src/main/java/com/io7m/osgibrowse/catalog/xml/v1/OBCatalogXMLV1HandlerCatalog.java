package com.io7m.osgibrowse.catalog.xml.v1;

import com.io7m.osgibrowse.catalog.api.OBCatalogRepositoryLink;
import com.io7m.osgibrowse.catalog.api.OBCatalogType;
import com.io7m.osgibrowse.catalog.xml.OBCatalogXMLAttributes;
import com.io7m.osgibrowse.catalog.xml.OBCatalogXMLErrorLog;
import com.io7m.osgibrowse.catalog.xml.OBCatalogXMLParserRequest;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.Locator2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

public final class OBCatalogXMLV1HandlerCatalog extends DefaultHandler2
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OBCatalogXMLV1HandlerCatalog.class);

  private final OBCatalogXMLParserRequest request;
  private final XMLReader reader;
  private final OBCatalogXMLErrorLog errors;
  private final Locator2 locator;
  private TreeSet<OBCatalogRepositoryLink> repositories;

  public OBCatalogXMLV1HandlerCatalog(
    final OBCatalogXMLParserRequest in_request,
    final XMLReader in_reader,
    final OBCatalogXMLErrorLog in_errors,
    final Locator2 in_locator)
  {
    this.request = Objects.requireNonNull(in_request, "request");
    this.reader = Objects.requireNonNull(in_reader, "reader");
    this.errors = Objects.requireNonNull(in_errors, "errors");
    this.locator = Objects.requireNonNull(in_locator, "locator");
    this.repositories = TreeSet.empty();
  }

  @Override
  public void startElement(
    final String uri,
    final String local_name,
    final String qualified_name,
    final Attributes attributes)
  {
    if (!this.errors.errors().isEmpty()) {
      return;
    }

    if (!Objects.equals(uri, OBCatalogV1Schema.SCHEMA.namespace().toString())) {
      return;
    }

    LOG.trace("startElement: {} {} {} {}", uri, local_name, qualified_name, attributes);

    switch (local_name) {
      case "catalog": {
        break;
      }
      case "repository": {
        try {
          this.repositories = this.repositories.add(this.parseRepository(attributes));
        } catch (final URISyntaxException e) {
          this.errors.addError(
            OBCatalogXMLErrorLog.createErrorFromException(this.request.file(), e));
        }
        break;
      }
      default: {
        break;
      }
    }
  }

  private OBCatalogRepositoryLink parseRepository(
    final Attributes attributes)
    throws URISyntaxException
  {
    final Map<String, String> map = OBCatalogXMLAttributes.attributeMap(attributes);
    return OBCatalogRepositoryLink.builder()
      .setUri(new URI(map.get("url")))
      .build();
  }

  @Override
  public void endElement(
    final String uri,
    final String local_name,
    final String qual_name)
  {
    if (!this.errors.errors().isEmpty()) {
      return;
    }

    if (!Objects.equals(uri, OBCatalogV1Schema.SCHEMA.namespace().toString())) {
      return;
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("endElement: {} {} {} {}", uri, local_name, qual_name);
    }
  }

  public OBCatalogType content()
  {
    return new Catalog(this.request.file(), this.repositories);
  }

  private static final class Catalog implements OBCatalogType
  {
    private final TreeSet<OBCatalogRepositoryLink> repositories;
    private final URI uri;

    Catalog(
      final URI in_uri,
      final TreeSet<OBCatalogRepositoryLink> in_repositories)
    {
      this.uri = Objects.requireNonNull(in_uri, "uri");
      this.repositories = Objects.requireNonNull(in_repositories, "repositories");
    }

    @Override
    public URI uri()
    {
      return this.uri;
    }

    @Override
    public SortedSet<OBCatalogRepositoryLink> repositories()
    {
      return this.repositories;
    }
  }
}
