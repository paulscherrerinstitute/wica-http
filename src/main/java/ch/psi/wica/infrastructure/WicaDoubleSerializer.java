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
 * Provides a means of serializing objects of type Double in such a way that
 * a configurable number of digits appear after the decimal point.
 *
 * This class works in conjunction with Jackson library module class with whom
 * it must be registered.
 */
@Immutable
class WicaDoubleSerializer extends JsonSerializer<Double>
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final int numericScale;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new custom serializer which generates a string representation
    * with the specified numeric scale (that's to say with the specified number
    * of digits after the decimal point).
    *
    * Where the numeric scale forces rounding, then a RoundingMode.HALF_UP
    * strategy is implemented.
    *
    * @param numericScale - a positive number specifying the number of digits to
    *     appear after the decimal point in the string representation.
    *
    * @throws IllegalArgumentException if the requested numeric scale is negative.
    */
   WicaDoubleSerializer( int numericScale )
   {
      Validate.isTrue(numericScale >= 0, String.format( "numericScale ('%d') cannot be negative", numericScale ) );
      this.numericScale = numericScale;
   }


/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   /**
    * Serialize the Double.
    *
    * @param value the value to be serialized.
    * @param gen reference to a Java generator object that provides methods for generating the output string.
    * @param serializers reference to a serializer provider (not needed by this implementation)
    *
    */
   @Override
   public void serialize( Double value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
   {
      // BigDecimal cannot be used to represent Infinity or Nan so we have to handle these cases separately
      if ( value.isNaN() )
      {
         // Note: the format here is important. Javascript Numbers can understand NaN, but not nan or Nan or naN.
         gen.writeNumber( "NaN" );
      }
      else if ( value.isInfinite() )
      {
         // Note: the format here is important. Javascript Numbers can understand Infinity but not infinity.
         gen.writeNumber( "Infinity" );
      }
      else
      {
         // Note: BigDecimal provides a convenient way of formatting our double with the required number of digits.
         final BigDecimal bd = BigDecimal.valueOf( value ).setScale( numericScale, RoundingMode.HALF_UP );
         final String bdAsString = bd.toPlainString();
         // Note: the generator explicitly supports serializing a number field with String input type.
         gen.writeNumber( bdAsString );
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/


}
