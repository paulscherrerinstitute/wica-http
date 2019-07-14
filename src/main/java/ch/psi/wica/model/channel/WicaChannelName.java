/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.app.ControlSystemName;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelName
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final String PROTOCOL_REGEX = "(?<protocol>ca://|pv://)";

   /**
    * The regex below needs to include as a minimum the set of allowed characters
    * that are valid for an EPICS channel name. According to section 6.3.2 of the
    * EPICS Application Developer's Guide this includes the following:
    *    a-z A-Z 0-9 _ - : . [ ] < >
    */
   private static final String CONTROL_SYSTEM_NAME_REGEX = "(?<csname>[a-zA-Z0-9_\\-:.\\[\\]<>]+)";

   private static final String INSTANCE_REGEX = "(?<instance>##[0-9]+)";
   private static final String WICA_CHANNEL_NAME_FORMAT = PROTOCOL_REGEX + "?" + CONTROL_SYSTEM_NAME_REGEX + INSTANCE_REGEX + "?";
   private static final Pattern pattern = Pattern.compile( WICA_CHANNEL_NAME_FORMAT );

   private final Protocol protocol;
   private final ControlSystemName controlSystemName;
   private final Integer instance;
   private final String stringRepresentation;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaChannelName( Protocol protocol, ControlSystemName controlSystemName, Integer instance, String strSpecifier )
   {
      this.protocol = protocol;
      this.controlSystemName = Validate.notNull( controlSystemName );
      this.instance = instance;
      this.stringRepresentation = strSpecifier;
   }

/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelName of( String strSpecifier )
   {
      final Matcher matcher = pattern.matcher( strSpecifier );

      Validate.isTrue( matcher.matches(), "The string: '" + strSpecifier + "' was not a valid channel name." );

      // The protocol token may or man not be present. When not present we choose the default.
      final Protocol protocol = matcher.group("protocol" ) == null ?
            null : Protocol.of( matcher.group ("protocol" ) );

      // The control system name token MUST be present so we just grab it.
      @SuppressWarnings( "SpellCheckingInspection" )
      final ControlSystemName controlSystemName = ControlSystemName.of(matcher.group("csname" ) );

      // The instance token may or may not be present. When not present we choose the default.
      final Integer instance = matcher.group("instance" ) == null ?
            null : Integer.parseInt( matcher.group("instance" ).split( "##" )[ 1] );

      return new WicaChannelName( protocol, controlSystemName, instance, strSpecifier );
   }

/*- Public methods -----------------------------------------------------------*/

   public Optional<Protocol> getProtocol()
   {
      return Optional.ofNullable( protocol );
   }

   public ControlSystemName getControlSystemName()
   {
      return controlSystemName;
   }

   public Optional<Integer> getInstance()
   {
      return Optional.ofNullable( instance );
   }

   /**
    * Returns a string representation of the channel name that is intended to be future-proof.
    *
    * @return the representation.
    */
   public String asString()
   {
      return stringRepresentation;
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
      return protocol == that.protocol &&
            Objects.equals(controlSystemName, that.controlSystemName) &&
            Objects.equals(instance, that.instance);
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
