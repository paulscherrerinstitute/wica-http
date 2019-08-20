/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.*;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

class WicaChannelMetadataMixins
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes: ----------------------------------------------------------*/

/*- Nested Class: WicaChannelMetadataSerializerMixin -------------------------*/

   @JsonFilter( "WicaChannelDataFilter" )
   @JsonPropertyOrder( { "wsts", "egu", "prec", "hopr", "lopr", "drvh", "drvl", "hihi", "lolo", "high", "low" } )
   static abstract class WicaChannelMetadataSerializerMixin extends WicaChannelMetadata
   {
      private WicaChannelMetadataSerializerMixin()
      {
         super(null);
      }

      @Override
      public abstract @JsonProperty( "wsts" )
      LocalDateTime getWicaServerTimestamp();
   }

/*- Nested Class: WicaChannelMetadataUnknownSerializerMixin ------------------*/

   static abstract class WicaChannelMetadataUnknownSerializerMixin extends WicaChannelMetadata.WicaChannelMetadataUnknown
   {
      private WicaChannelMetadataUnknownSerializerMixin() { super(); }
   }

/*- Nested Class: WicaChannelMetadataStringSerializerMixin -------------------*/

   static abstract class WicaChannelMetadataStringSerializerMixin extends WicaChannelMetadata.WicaChannelMetadataString
   {
      private WicaChannelMetadataStringSerializerMixin()
      {
         super();
      }
   }

/*- Nested Class: WicaChannelMetadataStringArraySerializerMixin --------------*/

   static abstract class WicaChannelMetadataStringArraySerializerMixin extends WicaChannelMetadata.WicaChannelMetadataStringArray
   {
      private WicaChannelMetadataStringArraySerializerMixin()
      {
         super();
      }
   }

/*- Nested Class: WicaChannelMetadataIntegerSerializerMixin ------------------*/
   public static abstract class WicaChannelMetadataIntegerSerializerMixin extends WicaChannelMetadata.WicaChannelMetadataInteger
   {
      private WicaChannelMetadataIntegerSerializerMixin()
      {
         super(null, 0, 0, 0, 0, 0, 0, 0, 0);
      }

      // Engineering Units
      @Override
      public abstract @JsonProperty( "egu" )
      String getUnits();

      // High Operating Range
      @Override
      public abstract @JsonProperty( "hopr" )
      int getUpperDisplay();

      // Low Operating Range
      @Override
      public abstract @JsonProperty( "lopr" )
      int getLowerDisplay();

      // Drive High Control Limit
      @Override
      public abstract @JsonProperty( "drvh" )
      int getUpperControl();

      // Drive Low Control Limit
      @Override
      public abstract @JsonProperty( "drvl" )
      int getLowerControl();

      // Upper Alarm limit
      @Override
      public abstract @JsonProperty( "hihi" )
      int getUpperAlarm();

      // Lower Alarm Limit
      @Override
      public abstract @JsonProperty( "lolo" )
      int getLowerAlarm();

      // Upper Warning limit
      @Override
      public abstract @JsonProperty( "high" )
      int getUpperWarning();

      // Lower Warning Limit
      @Override
      public abstract @JsonProperty( "low" )
      int getLowerWarning();
   }


/*- Nested Class: WicaChannelMetadataIntegerArraySerializerMixin -------------*/

   static abstract class WicaChannelMetadataIntegerArraySerializerMixin extends WicaChannelMetadataMixins.WicaChannelMetadataIntegerSerializerMixin
   {
      private WicaChannelMetadataIntegerArraySerializerMixin() { super(); }
   }

/*- Nested Class: WicaChannelMetadataRealSerializerMixin ---------------------*/

   static abstract class WicaChannelMetadataRealSerializerMixin extends WicaChannelMetadata.WicaChannelMetadataReal
   {
      private WicaChannelMetadataRealSerializerMixin()
      {
         super( null, 0, 0, 0, 0, 0, 0, 0, 0, 0);
      }

      // Engineering Units
      @Override
      public abstract @JsonProperty( "egu" )
      String getUnits();

      // Display Precision
      @Override
      public abstract @JsonProperty( "prec" )
      int getPrecision();

      // High Operating Range
      @Override
      public abstract @JsonProperty( "hopr" )
      double getUpperDisplay();

      // Low Operating Range
      @Override
      public abstract @JsonProperty( "lopr" )
      double getLowerDisplay();

      // Drive High Control Limit
      @Override
      public abstract @JsonProperty( "drvh" )
      double getUpperControl();

      // Drive Low Control Limit
      @Override
      public abstract @JsonProperty( "drvl" )
      double getLowerControl();

      // Upper Alarm limit
      @Override
      public abstract @JsonProperty( "hihi" )
      double getUpperAlarm();

      // Lower Alarm Limit
      @Override
      public abstract @JsonProperty( "lolo" )
      double getLowerAlarm();

      // Upper Warning limit
      @Override
      public abstract @JsonProperty( "high" )
      double getUpperWarning();

      // Lower Warning Limit
      @Override
      public abstract @JsonProperty( "low" )
      double getLowerWarning();
   }

/*- Nested Class: WicaChannelMetadataRealArraySerializerMixin ----------------*/

   static abstract class WicaChannelMetadataRealArraySerializerMixin extends WicaChannelMetadataMixins.WicaChannelMetadataRealSerializerMixin
   {
      private WicaChannelMetadataRealArraySerializerMixin() { super(); }
   }

}
