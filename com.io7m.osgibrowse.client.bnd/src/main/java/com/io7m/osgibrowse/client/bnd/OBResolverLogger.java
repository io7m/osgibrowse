package com.io7m.osgibrowse.client.bnd;

import biz.aQute.resolve.ResolverLogger;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;

import java.util.Objects;

final class OBResolverLogger extends ResolverLogger
{
  private final Logger logger;

  OBResolverLogger(
    final Logger in_logger)
  {
    this.logger = Objects.requireNonNull(in_logger, "logger");
  }

  @Override
  public void log(
    final int level,
    final String msg,
    final Throwable throwable)
  {
    switch (level) {
      case ResolverLogger.LOG_DEBUG: {
        this.logger.debug("resolve: {}: ", msg, throwable);
        return;
      }
      case ResolverLogger.LOG_ERROR: {
        this.logger.error("resolve: {}: ", msg, throwable);
        return;
      }
      case ResolverLogger.LOG_INFO: {
        this.logger.info("resolve: {}: ", msg, throwable);
        return;
      }
      case ResolverLogger.LOG_WARNING: {
        this.logger.warn("resolve: {}: ", msg, throwable);
        return;
      }
      default: {
        this.logger.trace("resolve: {}: ", msg, throwable);
      }
    }
  }

  @Override
  public void log(
    final int level,
    final String message)
  {
    switch (level) {
      case ResolverLogger.LOG_DEBUG: {
        this.logger.debug("resolve: {}", message);
        return;
      }
      case ResolverLogger.LOG_ERROR: {
        this.logger.error("resolve: {}", message);
        return;
      }
      case ResolverLogger.LOG_INFO: {
        this.logger.info("resolve: {}", message);
        return;
      }
      case ResolverLogger.LOG_WARNING: {
        this.logger.warn("resolve: {}", message);
        return;
      }
      default: {
        this.logger.trace("resolve: {}", message);
      }
    }
  }

  @Override
  public void log(
    final ServiceReference sr,
    final int level,
    final String message)
  {
    switch (level) {
      case ResolverLogger.LOG_DEBUG: {
        this.logger.debug("resolve: {}: {}", sr, message);
        return;
      }
      case ResolverLogger.LOG_ERROR: {
        this.logger.error("resolve: {}: {}", sr, message);
        return;
      }
      case ResolverLogger.LOG_INFO: {
        this.logger.info("resolve: {}: {}", sr, message);
        return;
      }
      case ResolverLogger.LOG_WARNING: {
        this.logger.warn("resolve: {}: {}", sr, message);
        return;
      }
      default: {
        this.logger.trace("resolve: {}: {}", sr, message);
      }
    }
  }

  @Override
  public void log(
    final ServiceReference sr,
    final int level,
    final String message,
    final Throwable exception)
  {
    switch (level) {
      case ResolverLogger.LOG_DEBUG: {
        this.logger.debug("resolve: {}: {}: ", sr, message, exception);
        return;
      }
      case ResolverLogger.LOG_ERROR: {
        this.logger.error("resolve: {}: {}: ", sr, message, exception);
        return;
      }
      case ResolverLogger.LOG_INFO: {
        this.logger.info("resolve: {}: {}: ", sr, message, exception);
        return;
      }
      case ResolverLogger.LOG_WARNING: {
        this.logger.warn("resolve: {}: {}: ", sr, message, exception);
        return;
      }
      default: {
        this.logger.trace("resolve: {}: {}: ", sr, message, exception);
      }
    }
  }
}
