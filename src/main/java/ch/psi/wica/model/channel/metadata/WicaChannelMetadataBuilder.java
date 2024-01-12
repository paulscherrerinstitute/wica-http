/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.metadata;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelMetadataBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/

   /**
    * Creates the metadata for a channel whose type is not yet known.
    *
    * @return the instance.
    */
   public static WicaChannelMetadataUnknown createUnknownInstance()
   {
      return new WicaChannelMetadataUnknown();
   }

   /**
    * Creates the metadata for a channel whose type is STRING.
    *
    * @return the instance.
    */
   public static WicaChannelMetadataString createStringInstance()
   {
      return new WicaChannelMetadataString();
   }

   /**
    * Creates the metadata for a channel whose type is STRING_ARRAY.
    *
    * @return the instance.
    */
   public static WicaChannelMetadataStringArray createStringArrayInstance()
   {
      return new WicaChannelMetadataStringArray();
   }

   /**
    * Creates the metadata for a channel whose type is INTEGER.
    *
    * @param units the units.
    * @param upperDisplay the upper display limit.
    * @param lowerDisplay the lower display limit.
    * @param upperControl the upper control limit.
    * @param lowerControl the lower control limit.
    * @param upperAlarm the upper alarm limit.
    * @param lowerAlarm the lower alarm limit.
    * @param upperWarning the upper warning limit.
    * @param lowerWarning the lower warning limit.
    * @return the instance.
    */
   public static WicaChannelMetadataInteger createIntegerInstance( String units,
                                                                   int upperDisplay, int lowerDisplay,
                                                                   int upperControl, int lowerControl,
                                                                   int upperAlarm, int lowerAlarm,
                                                                   int upperWarning, int lowerWarning )
   {
      return new WicaChannelMetadataInteger( units, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                             upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   /**
    * Creates the metadata for a channel whose type is INTEGER_ARRAY.
    *
    * @param units the units.
    * @param upperDisplay the upper display limit.
    * @param lowerDisplay the lower display limit.
    * @param upperControl the upper control limit.
    * @param lowerControl the lower control limit.
    * @param upperAlarm the upper alarm limit.
    * @param lowerAlarm the lower alarm limit.
    * @param upperWarning the upper warning limit.
    * @param lowerWarning the lower warning limit.
    * @return the instance.
    */
   public static WicaChannelMetadataIntegerArray createIntegerArrayInstance( String units,
                                                                             int upperDisplay, int lowerDisplay,
                                                                             int upperControl, int lowerControl,
                                                                             int upperAlarm, int lowerAlarm,
                                                                             int upperWarning, int lowerWarning )
   {
      return new WicaChannelMetadataIntegerArray(units, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                                 upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   /**
    * Creates the metadata for a channel whose type is REAL.
    *
    * @param units the units.
    * @param precision the precision.
    * @param upperDisplay the upper display limit.
    * @param lowerDisplay the lower display limit.
    * @param upperControl the upper control limit.
    * @param lowerControl the lower control limit.
    * @param upperAlarm the upper alarm limit.
    * @param lowerAlarm the lower alarm limit.
    * @param upperWarning the upper warning limit.
    * @param lowerWarning the lower warning limit.
    * @return the instance.
    */
   public static WicaChannelMetadataReal createRealInstance( String units,
                                                             int precision,
                                                             double upperDisplay, double lowerDisplay,
                                                             double upperControl, double lowerControl,
                                                             double upperAlarm, double lowerAlarm,
                                                             double upperWarning, double lowerWarning )
   {
      return new WicaChannelMetadataReal( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                         upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

   /**
    * Creates the metadata for a channel whose type is REAL_ARRAY.
    *
    * @param units the units.
    * @param precision the precision.
    * @param upperDisplay the upper display limit.
    * @param lowerDisplay the lower display limit.
    * @param upperControl the upper control limit.
    * @param lowerControl the lower control limit.
    * @param upperAlarm the upper alarm limit.
    * @param lowerAlarm the lower alarm limit.
    * @param upperWarning the upper warning limit.
    * @param lowerWarning the lower warning limit.
    * @return the instance.
    */
   public static WicaChannelMetadataRealArray createRealArrayInstance( String units,
                                                                       int precision,
                                                                       double upperDisplay, double lowerDisplay,
                                                                       double upperControl, double lowerControl,
                                                                       double upperAlarm, double lowerAlarm,
                                                                       double upperWarning, double lowerWarning )
   {
      return new WicaChannelMetadataRealArray( units, precision, upperDisplay, lowerDisplay, upperControl, lowerControl,
                                               upperAlarm, lowerAlarm, upperWarning, lowerWarning );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
