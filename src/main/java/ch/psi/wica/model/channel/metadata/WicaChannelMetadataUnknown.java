/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel.metadata;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the metadata for a channel whose type is not yet known.
 */
public class WicaChannelMetadataUnknown extends WicaChannelMetadata
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new instance of the metadata for a channel whose type is unknown.
    */
   public WicaChannelMetadataUnknown()
   {
      super( WicaChannelType.UNKNOWN );
   }

/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
