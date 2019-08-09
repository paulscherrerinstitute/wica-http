/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the filtering which will be used when processing the values
 * received for a WicaChannel.
 */
public enum WicaFilterType
{

/*- Public attributes --------------------------------------------------------*/

   ALL_VALUE       ("all-value" ),
   RATE_LIMITER    ("rate-limiter" ),
   LAST_N          ("last-n" ),
   ONE_IN_M        ("one-in-m" ),
   CHANGE_FILTERER ("changes" );

/*- Private attributes -------------------------------------------------------*/

   private final String name;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   WicaFilterType( String name )
   {
      this.name = name;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public String toString()
   {
      return name;
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
