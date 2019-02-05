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

   private final boolean writeInfinityAsString;
   private final boolean writeNanAsString;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Constructs a new custom serializer for doubles.
    *
    * The serializer generates a string representation with the specified
    * numeric scale (that's to say with the specified number of digits after
    * the decimal point).
    *
    * Where the numeric scale forces rounding, then a RoundingMode.HALF_UP
    * strategy is implemented.
    *
    * The serializer can be configured to write special values (NaN and
    * infinity) as number or strings. For strict JSON compliance the string
    * format should be chosen, but this may imply extra work on the decoding
    * end to handle the type conversion. For JSON5 compliance numbers can be
    * used.
    *
    * @param numericScale - a positive number specifying the number of digits to
    *     appear after the decimal point in the string representation.
    *
    * @param writeNanAsString - determines whether the special value Double.NaN
    *     will be serialised as a number or a string.
    *
    * @param writeInfinityAsString - determines whether the special values
    *     Double.POSITIVE_INFINITY and DOUBLE.NEGATIVE_INFINITY will be
    *     serialised as a number or a string.
    *
    * @throws IllegalArgumentException if the requested numeric scale is negative.
    */
   WicaDoubleSerializer( int numericScale, boolean writeNanAsString, boolean writeInfinityAsString )
   {
      Validate.isTrue(numericScale >= 0, String.format( "numericScale ('%d') cannot be negative", numericScale ) );
      this.numericScale = numericScale;
      this.writeNanAsString = writeNanAsString;
      this.writeInfinityAsString = writeInfinityAsString;
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
      // Note: the format here is important. Javascript Numbers can understand NaN and
      // Infinity, but the JSON specification does not support them. JSON5, however, is
      // a superset of JSON which does.
      //
      // Possible workarounds: (a) send NaN as a String and fix programmatically at the
      // receiving side; (b) send in JSON5 format then use JSON5 Javascript decoder on
      // the receiving end.
      //
      // The Jackson library used for the serialisation here is able to support both
      // possibilities so it is left as a configuration option in the application to
      // configure the appropriate behaviour.

      if ( value.isNaN() )
      {

         if ( this.writeNanAsString )
         {
            gen.writeString("NaN" );
         }
         else
         {
            gen.writeNumber("NaN" );
         }
      }
      else if ( value.isInfinite() )
      {
         // Note: the format here is important. Javascript Numbers can understand Infinity but not infinity.
         if ( this.writeInfinityAsString )
         {
            gen.writeString("Infinity");
         }
         else
         {
            gen.writeNumber( "Infinity" );
         }
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
