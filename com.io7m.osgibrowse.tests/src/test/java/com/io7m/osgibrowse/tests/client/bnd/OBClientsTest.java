package com.io7m.osgibrowse.tests.client.bnd;

import com.io7m.osgibrowse.client.api.OBClientProviderType;
import com.io7m.osgibrowse.client.bnd.OBClients;
import com.io7m.osgibrowse.tests.client.OBClientContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OBClientsTest extends OBClientContract
{
  @Override
  protected OBClientProviderType clients()
  {
    return new OBClients();
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(OBClientsTest.class);
  }
}
