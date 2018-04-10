package com.github.gzm55.sisu.plexus;


import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * based on class PlexusTestCase of sisu.plexus project
 */
public class PlexusJUnit5TestCase
{
  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  private static final String PLEXUS_HOME = "plexus.home";

  // ----------------------------------------------------------------------
  // Initialization-on-demand
  // ----------------------------------------------------------------------

  private static final class Lazy
  {
    static
    {
      final String path = System.getProperty( "basedir" );
      BASEDIR = null != path ? path : new File( "" ).getAbsolutePath();
    }

    static final String BASEDIR;
  }

  // ----------------------------------------------------------------------
  // Utility methods
  // ----------------------------------------------------------------------

  public static String getBasedir()
  {
    return Lazy.BASEDIR;
  }

  public static File getTestFile( final String path )
  {
    return getTestFile( getBasedir(), path );
  }

  public static File getTestFile( final String basedir, final String path )
  {
    File root = new File( basedir );
    if ( !root.isAbsolute() )
    {
      root = new File( getBasedir(), basedir );
    }
    return new File( root, path );
  }

  public static String getTestPath( final String path )
  {
    return getTestFile( path ).getAbsolutePath();
  }

  public static String getTestPath( final String basedir, final String path )
  {
    return getTestFile( basedir, path ).getAbsolutePath();
  }

  public static String getTestConfiguration( final Class<?> clazz )
  {
    // always use outermost class name
    final String name = clazz.getName();
    final int i = name.indexOf( '$' );

    return ( i < 0 ? name : name.substring( 0, i ) ).replace( '.', '/' ) + ".xml";
  }

  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private volatile PlexusContainer container;

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public final String getTestConfiguration()
  {
    return getTestConfiguration( getClass() );
  }

  // ----------------------------------------------------------------------
  // Customizable methods
  // ----------------------------------------------------------------------

  protected void customizeContext( @SuppressWarnings( "unused" ) final Context context )
  {
    // place-holder for tests to customize
  }

  protected String getCustomConfigurationName()
  {
    return null; // place-holder for tests to customize
  }

  protected void customizeContainerConfiguration( @SuppressWarnings( "unused" ) final ContainerConfiguration configuration )
  {
    // place-holder for tests to customize
  }

  protected PlexusContainer getContainer()
  {
    if ( null == container )
    {
      setupContainer();
    }
    return container;
  }

  @BeforeEach
  protected synchronized void setupContainer()
  {
    if ( null == container )
    {
      try
      {
        container = new DefaultPlexusContainer( config() );
      }
      catch ( final PlexusContainerException e )
      {
        throw new IllegalArgumentException( e );
      }
    }
  }

  @AfterEach
  protected synchronized void teardownContainer()
  {
    if ( null != container )
    {
      container.dispose();
      container = null;
    }
  }

  // ----------------------------------------------------------------------
  // Shared methods
  // ----------------------------------------------------------------------

  protected final String getConfigurationName( @SuppressWarnings( "unused" ) final String name )
  {
    return getTestConfiguration();
  }

  protected final ClassLoader getClassLoader()
  {
    return getClass().getClassLoader();
  }

  protected final InputStream getResourceAsStream( final String name )
  {
    return getClass().getResourceAsStream( name );
  }

  protected final Object lookup( final String role )
    throws ComponentLookupException
  {
    return getContainer().lookup( role );
  }

  protected final Object lookup( final String role, final String hint )
    throws ComponentLookupException
  {
    return getContainer().lookup( role, hint );
  }

  protected final <T> T lookup( final Class<T> role )
    throws ComponentLookupException
  {
    return getContainer().lookup( role );
  }

  protected final <T> T lookup( final Class<T> role, final String hint )
    throws ComponentLookupException
  {
    return getContainer().lookup( role, hint );
  }

  protected final void release( final Object component )
    throws ComponentLifecycleException
  {
    getContainer().release( component );
  }

  // ----------------------------------------------------------------------
  // Implementation methods
  // ----------------------------------------------------------------------

  private final ContainerConfiguration config()
  {
    final ContainerConfiguration config = new DefaultContainerConfiguration();

    // Apply current test context

    config.setName( "test" ).setContext( context() );

    // Find per-test components XML

    String path = getCustomConfigurationName();
    if ( null == path )
    {
      path = getConfigurationName( null );
    }

    config.setContainerConfiguration( path );

    // Per-test config customization

    customizeContainerConfiguration( config );

    return config;
  }

  private final Map<Object, Object> context()
  {
    final Context context = new DefaultContext();
    context.put( "basedir", getBasedir() );

    // Per-test context customization

    customizeContext( context );

    // Provide 'plexus.home' fall-back

    if ( !context.contains( PLEXUS_HOME ) )
    {
      context.put( PLEXUS_HOME, plexusHome() );
    }

    return context.getContextData();
  }

  private static String plexusHome()
  {
    final File home = getTestFile( "target/plexus-home" );
    if ( !home.isDirectory() )
    {
      home.mkdirs();
    }
    return home.getAbsolutePath();
  }
}
