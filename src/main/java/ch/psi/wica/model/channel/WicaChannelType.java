/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the type of WicaChannel. This can potentially change every
 * time new channel metadata is received.
 */
public enum WicaChannelType
{

/*- Public attributes --------------------------------------------------------*/

   UNKNOWN, // Initial state: used to represent the type of channel which has
            // not yet received any information from the underlying data source.
   INTEGER,
   REAL,
   STRING,
   INTEGER_ARRAY,
   REAL_ARRAY,
   STRING_ARRAY;


/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @Override
   public String toString()
   {
      return this.name();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
