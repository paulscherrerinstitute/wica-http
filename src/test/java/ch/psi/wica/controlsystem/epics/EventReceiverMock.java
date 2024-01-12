/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.controlsystem.epics;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.controlsystem.event.wica.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.controlsystem.event.wica.WicaChannelPolledValueUpdateEvent;
import ch.psi.wica.model.channel.metadata.WicaChannelMetadata;
import ch.psi.wica.model.channel.value.WicaChannelValue;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Component
public class EventReceiverMock
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final AtomicReference<LocalDateTime> metadataPublished = new AtomicReference<>();
   private final AtomicReference<LocalDateTime> valuePublished = new AtomicReference<>();
   private final AtomicReference<WicaChannelMetadata> metadata = new AtomicReference<>();
   private final AtomicReference<WicaChannelValue> value = new AtomicReference<>();


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public void arm()
   {
      metadataPublished.set( null );
      valuePublished.set( null );
   }

   public Optional<LocalDateTime> getMetadataPublishedTimestamp()
   {
      return Optional.ofNullable( metadataPublished.get() );
   }

   public Optional<LocalDateTime> getValuePublishedTimestamp()
   {
      return Optional.ofNullable( valuePublished.get() );
   }

   public Optional<WicaChannelMetadata> getMetadata()
   {
      return Optional.ofNullable( metadata.get() );
   }

   public Optional<WicaChannelValue> getValue()
   {
      return Optional.ofNullable( value.get() );
   }

   @EventListener
   public void handleWicaChannelMonitoredValueUpdateEvent( WicaChannelMonitoredValueUpdateEvent event)
   {
      value.set( event.getWicaChannelValue() );
      valuePublished.set( LocalDateTime.now());
   }

   @EventListener
   public void handleWicaChannelMonitoredMetadataUpdateEvent( WicaChannelMetadataUpdateEvent event )
   {
      metadata.set( event.getWicaChannelMetadata() );
      metadataPublished.set( LocalDateTime.now() );
   }

   @EventListener
   public void handleWicaChannelPolledValueUpdateEvent( WicaChannelPolledValueUpdateEvent event )
   {
      value.set( event.getWicaChannelValue() );
      valuePublished.set( LocalDateTime.now());
   }


/*- Package-level methods ----------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
