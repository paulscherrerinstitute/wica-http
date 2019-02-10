/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Provides a means of serializing objects of type double[] in such a way that
 * a configurable number of digits appear after the decimal point.
 *
 * This class works in conjunction with Jackson library module class with whom
 * it must be registered.
 */
@Immutable
class WicaDoubleArraySerializer extends JsonSerializer<double[]>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int numericScale;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new custom serializer for double arrays.
    *
    * The serializer generates a string representation with the specified
    * numeric scale (that's to say with the specified number of digits after
    * the decimal point).
    *
    * Where the numeric scale forces rounding, then a RoundingMode.HALF_UP
    * strategy is implemented.
    *
    * The JSON generator associated with this serializer can be configured to
    * determine whether this serializer writes special values NaN and Infinity
    * as numbers or strings.
    *
    * @param numericScale a positive number specifying the number of digits to
    *     appear after the decimal point in the serialized representation.
    *
    * @throws IllegalArgumentException if the requested numeric scale is negative.
    */
   WicaDoubleArraySerializer( int numericScale )
   {
      Validate.isTrue(numericScale >= 0, String.format( "numericScale ('%d') cannot be negative", numericScale ) );
      this.numericScale = numericScale;
   }

/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Serialize the Double Array.
    *
    * @param values the array of values to be serialized.
    * @param gen reference to a Java generator object that provides methods for generating the output string.
    * @param serializers reference to a serializer provider (not needed by this implementation)
    *
    */
   @Override
   public void serialize( double[] values, JsonGenerator gen, SerializerProvider serializers ) throws IOException
   {
      gen.writeStartArray();

      for ( double value: values )
      {
         // All other detaiuls the same as for the double scalar serializer
         WicaDoubleSerializer.serializeDouble( value, gen, numericScale );
      }
      gen.writeEndArray();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
