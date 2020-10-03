/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model.channel;

/*- Imported packages --------------------------------------------------------*/

import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the type of a WicaChannel. This can potentially change every
 * time new channel metadata is received.
 */
public enum WicaChannelType
{

/*- Public attributes --------------------------------------------------------*/

   UNKNOWN, // Initial state: used to represent the type of a channel which has
            // not yet received any information from the underlying data source.
   SHORT,
   INTEGER,
   REAL,
   STRING,
   SHORT_ARRAY,
   INTEGER_ARRAY,
   REAL_ARRAY,
   STRING_ARRAY;


/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/

   /**
    * Checks whether the supplied object is of a supported WicaChannelType.
    *
    * @param obj the object to check.
    * @return the result, set true when the object is recognised.
    */
   public static boolean isRecognisedType( Object obj )
   {
      Validate.notNull( obj);

      return ( obj instanceof Short   ) || ( obj instanceof short[]  ) ||
             ( obj instanceof Integer ) || ( obj instanceof int[]    ) ||
             ( obj instanceof Double  ) || ( obj instanceof double[] ) ||
             ( obj instanceof String  ) || ( obj instanceof String[] );
   }

   /**
    * Attempts to map the supplied Plain-Old-Java-Object (POJO) onto a Wica Type.
    *
    * Only a very limited number of types are supported, the attempt
    * to decode an unrecognised type will result in an exception.

    * @param pojo the object (which should be recognised and not null)
    *
    * @return the WicaChannelType
    *
    * @throws NullPointerException if the supplied argument was null.
    * @throws IllegalArgumentException if the type of the object was not recognised.
    */
   public static WicaChannelType getTypeFromPojo( Object pojo )
   {
      Validate.notNull( pojo );
      Validate.isTrue( isRecognisedType( pojo ) );

      if ( pojo instanceof Short )
      {
         return SHORT;
      }
      else if ( pojo instanceof short[] )
      {
         return SHORT_ARRAY;
      }
      if ( pojo instanceof Integer )
      {
         return INTEGER;
      }
      else if ( pojo instanceof int[] )
      {
         return INTEGER_ARRAY;
      }
      else if ( ( pojo instanceof Float ) || ( pojo instanceof Double) )
      {
         return REAL;
      }
      else if ( ( pojo instanceof float[] ) || ( pojo instanceof double[] ) )
      {
         return REAL_ARRAY;
      }
      else if ( pojo instanceof String)
      {
         return STRING;
      }
      else if ( pojo instanceof String[] )
      {
         return STRING_ARRAY;
      }

      throw new IllegalArgumentException( "The supplied object was of an unrecognised type" );
   }


/*- Public methods -----------------------------------------------------------*/

   @Override
   public String toString()
   {
      return this.name();
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
