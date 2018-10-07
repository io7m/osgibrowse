package com.io7m.osgibrowse.client.bnd;

import com.io7m.osgibrowse.client.api.OBClientProviderType;
import com.io7m.osgibrowse.client.api.OBClientType;

public final class OBClients implements OBClientProviderType
{
  public OBClients()
  {

  }

  @Override
  public OBClientType createEmptyClient()
  {
    return new OBClient();
  }
}
