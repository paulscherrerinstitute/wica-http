/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.infrastructure.channel.WicaChannelBuilder;
import ch.psi.wica.infrastructure.channel.WicaChannelPropertiesBuilder;
import ch.psi.wica.model.channel.WicaChannel;
import ch.psi.wica.model.channel.WicaChannelProperties;
import ch.psi.wica.model.stream.WicaStream;
import ch.psi.wica.model.stream.WicaStreamId;
import ch.psi.wica.model.stream.WicaStreamProperties;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SuppressWarnings( "WeakerAccess" )
public class WicaStreamBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaStreamId wicaStreamId;
   private WicaStreamProperties wicaStreamProperties = WicaStreamPropertiesBuilder.create().build();
   private List<WicaChannel> wicaChannels = new ArrayList<>();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   // Private to force use of the create factory method.
   private WicaStreamBuilder() {}


/*- Class methods ------------------------------------------------------------*/

   public static WicaStreamBuilder create()
   {
      return new WicaStreamBuilder();
   }


/*- Public methods -----------------------------------------------------------*/

   public WicaStreamBuilder withId( String wicaStreamId )
   {
      Validate.notNull( wicaStreamId,"The 'wicaStreamId' argument was null." );
      return withId( WicaStreamId.of( wicaStreamId ) );
   }

   public WicaStreamBuilder withId( WicaStreamId wicaStreamId )
   {
      Validate.notNull( wicaStreamId,"The 'wicaStreamId' argument was null." );
      this.wicaStreamId = wicaStreamId;
      return this;
   }

   public WicaStreamBuilder withChannels( Set<WicaChannel> wicaChannels )
   {
      Validate.notNull( wicaChannels,"The 'wicaChannels' argument was null." );
      this.wicaChannels.addAll( wicaChannels );
      return this;
   }

   public WicaStreamBuilder withDefaultStreamProperties()
   {
      this.wicaStreamProperties = WicaStreamPropertiesBuilder.create().withDefaultProperties().build();
      return this;
   }

   public WicaStreamBuilder withStreamProperties( WicaStreamProperties wicaStreamProperties )
   {
      Validate.notNull( wicaStreamProperties,"The 'wicaStreamProperties' argument was null." );
      this.wicaStreamProperties = WicaStreamPropertiesBuilder.create().withStreamProperties( wicaStreamProperties ).build();
      return this;
   }

   public WicaStreamBuilder withChannel( WicaChannel wicaChannel )
   {
      Validate.notNull( wicaChannel,"The 'wicaChannel' argument was null." );
      Validate.isTrue(  ! wicaChannels.contains( wicaChannel ),"The 'wicaChannel' argument specified a channel that was identical to one that was already defined." );
      wicaChannels.add( wicaChannel );
      return this;
   }

   public WicaStreamBuilder withChannelNameAndStreamProperties( String wicaChannelName )
   {
      Validate.notNull( wicaChannelName,"The 'WicaChannelName' argument was null." );

      final WicaChannelProperties wicaChannelPropertiesFromStream = WicaChannelPropertiesBuilder.create().withChannelPropertiesFromStream( wicaStreamProperties ).build();
      final WicaChannel wicaChannel = WicaChannelBuilder.create()
            .withChannelNameAndProperties( wicaChannelName, wicaChannelPropertiesFromStream ).build();
      Validate.isTrue(  ! wicaChannels.contains( wicaChannel ),"The 'WicaChannelName' argument specified a channel that was identical to one that was already defined." );
      wicaChannels.add ( wicaChannel );
      return this;
   }

   public WicaStreamBuilder withChannelNameAndDefaultProperties( String wicaChannelName )
   {
      Validate.notNull( wicaChannelName,"The 'WicaChannelName' argument was null." );

      final WicaChannel wicaChannel = WicaChannelBuilder.create().withChannelNameAndDefaultProperties( wicaChannelName ).build();
      Validate.isTrue(  ! wicaChannels.contains( wicaChannel ),"The 'WicaChannelName' argument specified a channel that was identical to one that was already defined." );
      wicaChannels.add ( wicaChannel );
      return this;
   }

   public WicaStreamBuilder withChannelNameAndCombinedProperties( String wicaChannelName, WicaChannelProperties wicaChannelProperties  )
   {
      Validate.notNull( wicaChannelName,"The 'WicaChannelName' argument was null." );
      Validate.notNull( wicaChannelProperties,"The 'wicaChannelProperties' argument was null." );

      final WicaChannelProperties wicaChannelCombinedProperties = WicaChannelPropertiesBuilder.create()
            .withChannelPropertiesFromStream( wicaStreamProperties )
            .withChannelProperties( wicaChannelProperties )
            .build();

      final WicaChannel wicaChannel = WicaChannelBuilder.create().withChannelNameAndProperties( wicaChannelName, wicaChannelCombinedProperties ).build();
      Validate.isTrue(  ! wicaChannels.contains( wicaChannel ),"The 'WicaChannelName' argument specified a channel that was identical to one that was already defined." );
      wicaChannels.add( wicaChannel );
      return this;
   }

   public WicaStream build()
   {
      final WicaStreamId wicaStreamId = this.wicaStreamId == null ? WicaStreamId.createNext() : this.wicaStreamId;
      return new WicaStream( wicaStreamId, wicaStreamProperties, new HashSet<>(wicaChannels ) );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
