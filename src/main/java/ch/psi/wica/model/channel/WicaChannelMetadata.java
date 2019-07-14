/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

// Note the Filter Id in the annotation here must match the definition in
// the WicaChannelMetadataSerializer.
@JsonFilter( "WicaChannelDataFilter" )
@Immutable
public abstract class WicaChannelMetadata extends WicaChannelData
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelType type;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private WicaChannelMetadata( WicaChannelType type )
   {
      this.type = type;
   }


/*- Class methods ------------------------------------------------------------*/

   public static WicaChannelMetadata createUnknownInstance()
   {
      return WicaChannelMetadataUnknown.UNKNOWN_INSTANCE;
   }

   public static WicaChannelMetadata createStringInstance()
   {
      return new WicaChannelMetadataString();
   }

   public static WicaChannelMetadata createStringArrayInstance()
   {
      return new WicaChannelMetadataStringArray();
   }

   public static WicaChannelMetadata createIntegerInstance( String units,
                                                            int upperDisplay, int lowerDisplay,
                                                            int upperControl, int lowerControl,
                                                            int upperAlarm, int lowerAlarm,
                                                            int upperWarning, int lowerWarning )
   {
      return new WicaChannelMetadataInteger(units, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                            upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   public static WicaChannelMetadata createIntegerArrayInstance( String units,
                                                                 int upperDisplay, int lowerDisplay,
                                                                 int upperControl, int lowerControl,
                                                                 int upperAlarm, int lowerAlarm,
                                                                 int upperWarning, int lowerWarning )
   {
      return new WicaChannelMetadataIntegerArray(units, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                                 upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   public static WicaChannelMetadata createRealInstance( String units,
                                                         int precision,
                                                         double upperDisplay, double lowerDisplay,
                                                         double upperControl, double lowerControl,
                                                         double upperAlarm, double lowerAlarm,
                                                         double upperWarning, double lowerWarning )
   {
      return new WicaChannelMetadataReal(units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                         upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   public static WicaChannelMetadata createRealArrayInstance( String units,
                                                              int precision,
                                                              double upperDisplay, double lowerDisplay,
                                                              double upperControl, double lowerControl,
                                                              double upperAlarm, double lowerAlarm,
                                                              double upperWarning, double lowerWarning )
   {
      return new WicaChannelMetadataRealArray(units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                              upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

/*- Public methods -----------------------------------------------------------*/

   @JsonProperty( "type" )
   public WicaChannelType getType()
   {
      return type;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

/*- Nested Class: WicaChannelMetadataUnknown --------------------------------*/

   private static class WicaChannelMetadataUnknown extends WicaChannelMetadata
   {
      private static final WicaChannelMetadataUnknown UNKNOWN_INSTANCE = new WicaChannelMetadataUnknown();

      WicaChannelMetadataUnknown()
      {
         super( WicaChannelType.UNKNOWN );
      }
   }


/*- Nested Class: WicaChannelMetadataString --------------------------------*/

   private static class WicaChannelMetadataString extends WicaChannelMetadata
   {
      WicaChannelMetadataString()
      {
         super( WicaChannelType.STRING );
      }

      @SuppressWarnings( "SameParameterValue" )
      WicaChannelMetadataString( WicaChannelType subtype )
      {
         super( subtype );
      }
   }


/*- Nested Class: WicaChannelMetadataStringArray ----------------------------*/

   private static class WicaChannelMetadataStringArray extends WicaChannelMetadataString
   {
      WicaChannelMetadataStringArray()
      {
         super( WicaChannelType.STRING_ARRAY );
      }
   }


/*- Nested Class: WicaChannelMetadataInteger --------------------------------*/

   private static class WicaChannelMetadataInteger extends WicaChannelMetadata
   {
      private final String units;
      private final int upperDisplay;
      private final int lowerDisplay;
      private final int upperControl;
      private final int lowerControl;
      private final int upperAlarm;
      private final int lowerAlarm;
      private final int upperWarning;
      private final int lowerWarning;

      @SuppressWarnings( "Duplicates" )
      private WicaChannelMetadataInteger( WicaChannelType subType,
                                          String units,
                                          int upperDisplay, int lowerDisplay,
                                          int upperControl, int lowerControl,
                                          int upperAlarm, int lowerAlarm,
                                          int upperWarning, int lowerWarning )
      {
         super( subType );
         this.units = Validate.notNull( units );
         this.upperDisplay = upperDisplay;
         this.lowerDisplay = lowerDisplay;
         this.upperControl = upperControl;
         this.lowerControl = lowerControl;
         this.upperAlarm =   upperAlarm;
         this.lowerAlarm =   lowerAlarm;
         this.upperWarning = upperWarning;
         this.lowerWarning = lowerWarning;
      }
      private WicaChannelMetadataInteger( String units,
                                          int upperDisplay, int lowerDisplay,
                                          int upperControl, int lowerControl,
                                          int upperAlarm, int lowerAlarm,
                                          int upperWarning, int lowerWarning )
      {
         this( WicaChannelType.INTEGER,
               units,
               upperDisplay, lowerDisplay,
               upperControl, lowerControl,
               upperAlarm, lowerAlarm,
               upperWarning, lowerWarning );
      }


      // Engineering Units
      @JsonProperty( "egu" )
      public String getUnits()
      {
         return units;
      }

      // High Operating Range
      @JsonProperty( "hopr" )
      public int getUpperDisplay()
      {
         return upperDisplay;
      }

      // Low Operating Range
      @JsonProperty( "lopr" )
      public int getLowerDisplay()
      {
         return lowerDisplay;
      }

      // Drive High Control Limit
      @JsonProperty( "drvh" )
      public int getUpperControl()
      {
         return upperControl;
      }

      // Drive Low Control Limit
      @JsonProperty( "drvl" )
      public int getLowerControl()
      {
         return lowerControl;
      }

      // Upper Alarm limit
      @JsonProperty( "hihi" )
      public int getUpperAlarm()
      {
         return upperAlarm;
      }

      // Lower Alarm Limit
      @JsonProperty( "lolo" )
      public int getLowerAlarm()
      {
         return lowerAlarm;
      }

      // Upper Warning Limit
      @JsonProperty( "high" )
      public int getUpperWarning()
      {
         return upperWarning;
      }

      // Lower Warning Limit
      @JsonProperty( "low" )
      public int getLowerWarning()
      {
         return lowerWarning;
      }
   }


/*- Nested Class: WicaChannelMetadataIntegerArray ---------------------------*/

   private static class WicaChannelMetadataIntegerArray extends WicaChannelMetadataInteger
   {
      private WicaChannelMetadataIntegerArray( String units,
                                               int upperDisplay, int lowerDisplay,
                                               int upperControl, int lowerControl,
                                               int upperAlarm, int lowerAlarm,
                                               int upperWarning, int lowerWarning )
      {
         super( WicaChannelType.INTEGER_ARRAY, units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      }
   }

/*- Nested Class: WicaChannelMetadataReal -----------------------------------*/

   private static class WicaChannelMetadataReal extends WicaChannelMetadata
   {
      private final String units;
      private final int precision;
      private final double upperDisplay;
      private final double lowerDisplay;
      private final double upperControl;
      private final double lowerControl;
      private final double upperAlarm;
      private final double lowerAlarm;
      private final double upperWarning;
      private final double lowerWarning;


      @SuppressWarnings( "Duplicates" )
      private WicaChannelMetadataReal( WicaChannelType subType,
                                       String units,
                                       int precision,
                                       double upperDisplay, double lowerDisplay,
                                       double upperControl, double lowerControl,
                                       double upperAlarm, double lowerAlarm,
                                       double upperWarning, double lowerWarning )
      {
         super( subType );
         this.units = Validate.notNull( units );
         this.precision = precision;
         this.upperDisplay = upperDisplay;
         this.lowerDisplay = lowerDisplay;
         this.upperControl = upperControl;
         this.lowerControl = lowerControl;
         this.upperAlarm =   upperAlarm;
         this.lowerAlarm =   lowerAlarm;
         this.upperWarning = upperWarning;
         this.lowerWarning = lowerWarning;
      }

      private WicaChannelMetadataReal( String units,
                                       int precision,
                                       double upperDisplay, double lowerDisplay,
                                       double upperControl, double lowerControl,
                                       double upperAlarm, double lowerAlarm,
                                       double upperWarning, double lowerWarning )
      {
         this( WicaChannelType.REAL,
               units,
               precision,
               upperDisplay, lowerDisplay,
               upperControl, lowerControl,
               upperAlarm, lowerAlarm,
               upperWarning, lowerWarning );
      }

      // Engineering Units
      @JsonProperty( "egu" )
      public String getUnits()
      {
         return units;
      }

      // Display Precision
      @JsonProperty( "prec" )
      public int getPrecision()
      {
         return precision;
      }

      // High Operating Range
      @JsonProperty( "hopr" )
      public double getUpperDisplay()
      {
         return upperDisplay;
      }

      // Low Operating Range
      @JsonProperty( "lopr" )
      public double getLowerDisplay()
      {
         return lowerDisplay;
      }

      // Drive High Control Limit
      @JsonProperty( "drvh" )
      public double getUpperControl()
      {
         return upperControl;
      }

      // Drive Low Control Limit
      @JsonProperty( "drvl" )
      public double getLowerControl()
      {
         return lowerControl;
      }

      // Upper Alarm limit
      @JsonProperty( "hihi" )
      public double getUpperAlarm()
      {
         return upperAlarm;
      }

      // Lower Alarm Limit
      @JsonProperty( "lolo" )
      public double getLowerAlarm()
      {
         return lowerAlarm;
      }

      // Upper Warning Limit
      @JsonProperty( "high" )
      public double getUpperWarning()
      {
         return upperWarning;
      }

      // Lower Warning Limit
      @JsonProperty( "low" )
      public double getLowerWarning()
      {
         return lowerWarning;
      }
   }


/*- Nested Class: WicaChannelMetadataRealArray ------------------------------*/

   private static class WicaChannelMetadataRealArray extends WicaChannelMetadataReal
   {
      private WicaChannelMetadataRealArray( String units,
                                            int precision,
                                            double upperDisplay, double lowerDisplay,
                                            double upperControl, double lowerControl,
                                            double upperAlarm, double lowerAlarm,
                                            double upperWarning, double lowerWarning )
      {
         super( WicaChannelType.REAL_ARRAY, units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      }
   }
}
