/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public abstract class WicaChannelMetadata
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
      return new EpicsChannelMetadataUnknown();
   }

   public static WicaChannelMetadata createStringInstance()
   {
      return new EpicsChannelMetadataString();
   }

   public static WicaChannelMetadata createStringArrayInstance()
   {
      return new EpicsChannelMetadataStringArray();
   }

   public static WicaChannelMetadata createIntegerInstance( String units,
                                                            int upperDisplay, int lowerDisplay,
                                                            int upperControl, int lowerControl,
                                                            int upperAlarm, int lowerAlarm,
                                                            int upperWarning, int lowerWarning )
   {
      return new EpicsChannelMetadataInteger( units, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                              upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   public static WicaChannelMetadata createIntegerArrayInstance( String units,
                                                                 int upperDisplay, int lowerDisplay,
                                                                 int upperControl, int lowerControl,
                                                                 int upperAlarm, int lowerAlarm,
                                                                 int upperWarning, int lowerWarning )
   {
      return new EpicsChannelMetadataIntegerArray( units, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                                   upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   public static WicaChannelMetadata createRealInstance( String units,
                                                         int precision,
                                                         double upperDisplay, double lowerDisplay,
                                                         double upperControl, double lowerControl,
                                                         double upperAlarm, double lowerAlarm,
                                                         double upperWarning, double lowerWarning )
   {
      return new EpicsChannelMetadataReal( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                           upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   public static WicaChannelMetadata createRealArrayInstance( String units,
                                                              int precision,
                                                              double upperDisplay, double lowerDisplay,
                                                              double upperControl, double lowerControl,
                                                              double upperAlarm, double lowerAlarm,
                                                              double upperWarning, double lowerWarning )
   {
      return new EpicsChannelMetadataRealArray( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl,
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

/*- Nested Class: EpicsChannelMetadataUnknown --------------------------------*/

   private static class EpicsChannelMetadataUnknown extends WicaChannelMetadata
   {
      EpicsChannelMetadataUnknown()
      {
         super( WicaChannelType.UNKNOWN );
      }
   }


/*- Nested Class: EpicsChannelMetadataString --------------------------------*/

   private static class EpicsChannelMetadataString extends WicaChannelMetadata
   {
      EpicsChannelMetadataString()
      {
         super( WicaChannelType.STRING );
      }

      EpicsChannelMetadataString( WicaChannelType subtype )
      {
         super( subtype );
      }
   }


/*- Nested Class: EpicsChannelMetadataStringArray ----------------------------*/

   private static class EpicsChannelMetadataStringArray extends EpicsChannelMetadataString
   {
      EpicsChannelMetadataStringArray()
      {
         super( WicaChannelType.STRING_ARRAY );
      }
   }


/*- Nested Class: EpicsChannelMetadataInteger --------------------------------*/

   private static class EpicsChannelMetadataInteger extends WicaChannelMetadata
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

      private EpicsChannelMetadataInteger( WicaChannelType subType,
                                           String units,
                                           int upperDisplay, int lowerDisplay,
                                           int upperControl, int lowerControl,
                                           int upperAlarm,   int lowerAlarm,
                                           int upperWarning, int lowerWarning )
      {
         super( subType );
         this.units = Validate.notNull( units );
         this.upperDisplay = Validate.notNull( upperDisplay );
         this.lowerDisplay = Validate.notNull( lowerDisplay );
         this.upperControl = Validate.notNull( upperControl );
         this.lowerControl = Validate.notNull( lowerControl );
         this.upperAlarm =   Validate.notNull( upperAlarm );
         this.lowerAlarm =   Validate.notNull( lowerAlarm );
         this.upperWarning = Validate.notNull( upperWarning );
         this.lowerWarning = Validate.notNull( lowerWarning );
      }
      private EpicsChannelMetadataInteger( String units,
                                          int upperDisplay, int lowerDisplay,
                                          int upperControl, int lowerControl,
                                          int upperAlarm,   int lowerAlarm,
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


/*- Nested Class: EpicsChannelMetadataIntegerArray ---------------------------*/

   private static class EpicsChannelMetadataIntegerArray extends EpicsChannelMetadataInteger
   {
      private EpicsChannelMetadataIntegerArray( String units,
                                               int upperDisplay, int lowerDisplay,
                                               int upperControl, int lowerControl,
                                               int upperAlarm,   int lowerAlarm,
                                               int upperWarning, int lowerWarning )
      {
         super( WicaChannelType.INTEGER_ARRAY, units, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      }
   }

/*- Nested Class: EpicsChannelMetadataReal -----------------------------------*/

   private static class EpicsChannelMetadataReal extends WicaChannelMetadata
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


      private EpicsChannelMetadataReal( WicaChannelType subType,
                                       String units,
                                       int precision,
                                       double upperDisplay, double lowerDisplay,
                                       double upperControl, double lowerControl,
                                       double upperAlarm,   double lowerAlarm,
                                       double upperWarning, double lowerWarning )
      {
         super( subType );
         this.units = Validate.notNull( units );
         this.precision = precision;
         this.upperDisplay = Validate.notNull( upperDisplay );
         this.lowerDisplay = Validate.notNull( lowerDisplay );
         this.upperControl = Validate.notNull( upperControl );
         this.lowerControl = Validate.notNull( lowerControl );
         this.upperAlarm =   Validate.notNull( upperAlarm );
         this.lowerAlarm =   Validate.notNull( lowerAlarm );
         this.upperWarning = Validate.notNull( upperWarning );
         this.lowerWarning = Validate.notNull( lowerWarning );
      }

      private EpicsChannelMetadataReal( String units,
                                        int precision,
                                        double upperDisplay, double lowerDisplay,
                                        double upperControl, double lowerControl,
                                        double upperAlarm,   double lowerAlarm,
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


/*- Nested Class: EpicsChannelMetadataRealArray ------------------------------*/

   private static class EpicsChannelMetadataRealArray extends EpicsChannelMetadataReal
   {
      private EpicsChannelMetadataRealArray( String units,
                                             int precision,
                                             double upperDisplay, double lowerDisplay,
                                             double upperControl, double lowerControl,
                                             double upperAlarm,   double lowerAlarm,
                                             double upperWarning, double lowerWarning )
      {
         super( WicaChannelType.REAL_ARRAY, units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl, upperAlarm, lowerAlarm, upperWarning, lowerWarning );
      }
   }
}
