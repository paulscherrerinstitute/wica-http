/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelName
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   static final Protocol DEFAULT_PROTOCOL = Protocol.CA;
   static final int DEFAULT_INSTANCE = 1;

   private static final String PROTOCOL_REGEX = "(?<protocol>ca://|pv://)";
   private static final String CSNAME_REGEX = "(?<csname>[A-Z|a-z|0-9|:|_|\\-|<|>]+)";
   private static final String INSTANCE_REGEX = "(?<instance>##[0-9]+)";
   private static final String WICA_CHANNEL_NAME_FORMAT = PROTOCOL_REGEX + "?" + CSNAME_REGEX + INSTANCE_REGEX + "?";
   private static final Pattern pattern = Pattern.compile( WICA_CHANNEL_NAME_FORMAT );

   private final Protocol protocol;
   private final ControlSystemName controlSystemName;
   private final int instance;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   public WicaChannelName( Protocol protocol, ControlSystemName controlSystemName, int instance )
   {
      this.protocol = protocol;
      this.controlSystemName = Validate.notNull( controlSystemName );
      this.instance = instance;
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelName of( String strSpecifier )
   {
      final Matcher matcher = pattern.matcher( strSpecifier );

      Validate.isTrue( matcher.matches() );

      // The protocol token may or man not be present. When not present we choose the default.
      final Protocol protocol = matcher.group("protocol" ) == null ?
            DEFAULT_PROTOCOL : Protocol.of( matcher.group ("protocol" ) );

      // The csname token MUST be present so we just grab it.
      final ControlSystemName csName = ControlSystemName.of( matcher.group( "csname" ) );

      // The instance token may or may not be present. When not present we choose the default.
      final int instance = matcher.group("instance" ) == null ?
            DEFAULT_INSTANCE : Integer.parseInt( matcher.group("instance" ).split( "##" )[ 1] );

      return new WicaChannelName( protocol, csName, instance );
   }

/*- Public methods -----------------------------------------------------------*/

   public Protocol getProtocol()
   {
      return protocol;
   }
   public ControlSystemName getControlSystemName()
   {
      return controlSystemName;
   }
   public int getInstance()
   {
      return instance;
   }

   /**
    * Returns a string representation of the channel name that is intended to be future-proof.
    *
    * @return the representation.
    */
   public String asString()
   {
      return protocol.getName() + controlSystemName.asString() + "##" + instance;
   }

   /**
    * Returns a string representation of the channel name suitable for user display and/or
    * diagnostic purposes and which may be subject to future change.
    *
    * @return the representation.
    */
   @Override
   public String toString()
   {
      return asString();
   }


   @Override
   public boolean equals( Object o )
   {
      if ( this == o ) return true;
      if ( !(o instanceof WicaChannelName) ) return false;
      WicaChannelName that = (WicaChannelName) o;
      return instance == that.instance &&
            protocol == that.protocol &&
            Objects.equals(controlSystemName, that.controlSystemName);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(protocol, controlSystemName, instance);
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   public enum Protocol
   {
      CA( "ca://" ),
      PV( "pv://" );

      private static final Map<String,Protocol> ENUM_MAP;
      static
      {
         Map<String,Protocol> map = new ConcurrentHashMap<>();
         for (Protocol instance : Protocol.values())
         {
            map.put( instance.getName(), instance );
         }
         ENUM_MAP = Collections.unmodifiableMap( map );
      }

      private final String name;

      Protocol( String name )
      {
         this.name = name;
      }

      String getName()
      {
         return name;
      }

      public static Protocol of ( String name )
      {
         Validate.isTrue( ENUM_MAP.containsKey( name ), "The protocol named: '" + name + "' was not recognised" );
         return Protocol.ENUM_MAP.get( name );
      }
   }
}
