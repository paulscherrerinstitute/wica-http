package ch.psi.wica.controlsystem.epics;

import ch.psi.wica.controlsystem.event.WicaChannelMetadataUpdateEvent;
import ch.psi.wica.controlsystem.event.WicaChannelMonitoredValueUpdateEvent;
import ch.psi.wica.model.channel.WicaChannelMetadata;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class EventReceiverMock
{
   private AtomicReference<LocalDateTime> metadataPublished = new AtomicReference<>();
   private AtomicReference<LocalDateTime> valuePublished = new AtomicReference<>();
   private AtomicReference<WicaChannelMetadata> metadata = new AtomicReference<>();
   private AtomicReference<WicaChannelValue> value = new AtomicReference<>();

   public void arm()
   {
      metadataPublished.set( null );
      valuePublished.set( null );
   }

   @EventListener
   public void handleWicaChannelMonitoredValueUpdateEvent ( WicaChannelMonitoredValueUpdateEvent event)
   {
      value.set( event.getWicaChannelValue() );
      valuePublished.set( LocalDateTime.now());
   }

   @EventListener
   public void handleWicaChannelMetadataUpdateEvent ( WicaChannelMetadataUpdateEvent event )
   {
      metadata.set( event.getWicaChannelMetadata() );
      metadataPublished.set( LocalDateTime.now() );
   }

   Optional<LocalDateTime> getMetadataPublishedTimestamp()
   {
      return Optional.ofNullable( metadataPublished.get() );
   }
   Optional<LocalDateTime> getValuePublishedTimestamp()
   {
      return Optional.ofNullable( valuePublished.get() );
   }
   Optional<WicaChannelMetadata> getMetadata()
   {
      return Optional.ofNullable( metadata.get() );
   }
   Optional<WicaChannelValue> getValue()
   {
      return Optional.ofNullable( value.get() );
   }
}
