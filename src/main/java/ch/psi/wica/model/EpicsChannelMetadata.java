/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Map;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@Immutable
public abstract class EpicsChannelMetadata
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static final ObjectMapper jsonObjectMapper = new Jackson2ObjectMapperBuilder().createXmlMapper(false ).build();

   private EpicsChannelType type;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   private EpicsChannelMetadata( EpicsChannelType type )
   {
      this.type = type;
   }

/*- Class methods ------------------------------------------------------------*/

   public static String convertMapToJsonRepresentation( Map<EpicsChannelName,EpicsChannelMetadata> channelMetadataMap )
   {
      try
      {
         return jsonObjectMapper.writeValueAsString( channelMetadataMap );
      }
      catch ( JsonProcessingException ex )
      {
         return "error";
      }
   }

   public static EpicsChannelMetadata createStringInstance()
   {
      return new EpicsChannelMetadataString();
   }

   public static EpicsChannelMetadata createIntegerInstance()
   {
      return new EpicsChannelMetadataInteger( "",
                                              0, 0,
                                              0, 0,
                                              0,   0,
                                              0, 0 );
   }

   public static EpicsChannelMetadata createRealInstance( String units,
                                                          int precision,
                                                          double upperDisplay, double lowerDisplay,
                                                          double upperControl, double lowerControl,
                                                          double upperAlarm,   double lowerAlarm,
                                                          double upperWarning, double lowerWarning )
   {
      return new EpicsChannelMetadataReal( units,
                                           precision,
                                           upperDisplay, lowerDisplay,
                                           upperControl, lowerControl,
                                           upperAlarm,   lowerAlarm,
                                           upperWarning, lowerWarning );
   }

/*- Public methods -----------------------------------------------------------*/

   @JsonProperty( "type" )
   public EpicsChannelType getType()
   {
      return type;
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

   private static class EpicsChannelMetadataString extends EpicsChannelMetadata
   {
      EpicsChannelMetadataString()
      {
         super( EpicsChannelType.STRING );
      }
   }

   public static class EpicsChannelMetadataInteger extends EpicsChannelMetadata
   {
      private String units;
      private int upperDisplay;
      private int lowerDisplay;
      private int upperControl;
      private int lowerControl;
      private int upperAlarm;
      private int lowerAlarm;
      private int upperWarning;
      private int lowerWarning;

      public EpicsChannelMetadataInteger( String units,
                                          int upperDisplay, int lowerDisplay,
                                          int upperControl, int lowerControl,
                                          int upperAlarm,   int lowerAlarm,
                                          int upperWarning, int lowerWarning )
      {
         super( EpicsChannelType.INTEGER );
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

      // Engineering Units
      @JsonProperty( "egu" )
      public String getUnits()
      {
         return units;
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

   public static class EpicsChannelMetadataReal extends EpicsChannelMetadata
   {
      private String units;
      private int precision;
      private double upperDisplay;
      private double lowerDisplay;
      private double upperControl;
      private double lowerControl;
      private double upperAlarm;
      private double lowerAlarm;
      private double upperWarning;
      private double lowerWarning;


      public EpicsChannelMetadataReal( String units,
                                       int precision,
                                       double upperDisplay, double lowerDisplay,
                                       double upperControl, double lowerControl,
                                       double upperAlarm,   double lowerAlarm,
                                       double upperWarning, double lowerWarning )
      {
         super( EpicsChannelType.REAL );
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

}
