/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure.channel;

/*- Imported packages --------------------------------------------------------*/

import java.util.Set;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

public class WicaChannelDataSerializerBuilder
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private Set<String> fieldsOfInterest = Set.of();
   private int numericScale = 6;
   private boolean quoteNumericStrings = false;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

      // Private to force use of the create factory method.
      private WicaChannelDataSerializerBuilder() {}

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   public static WicaChannelDataSerializerBuilder create()
   {
      return new WicaChannelDataSerializerBuilder();
   }

   public WicaChannelDataSerializerBuilder withFieldsOfInterest( Set<String> fieldsOfInterest )
   {
      this.fieldsOfInterest = fieldsOfInterest;
      return this;
   }

   public WicaChannelDataSerializerBuilder withNumericScale( int numericScale )
   {
      this.numericScale = numericScale;
      return this;
   }

   public WicaChannelDataSerializerBuilder withQuotedNumericStrings( boolean quoteNumericStrings )
   {
      this.quoteNumericStrings = quoteNumericStrings;
      return this;
   }

   public WicaChannelDataSerializer build()
   {
      return new WicaChannelDataSerializer( this.fieldsOfInterest, this.numericScale, this.quoteNumericStrings );
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
