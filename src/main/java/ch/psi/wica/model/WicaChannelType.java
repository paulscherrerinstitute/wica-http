/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.model;

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

   /**
    * Checks whether the supplied object is of a supported WicaChannelType.
    *
    * @param obj the object to check.
    * @return the result, set true when the object is recognised.
    */
   public static boolean isRecognisedType( Object obj )
   {
      Validate.notNull( obj);

      return ( obj instanceof Integer ) || ( obj instanceof int[] ) ||
             ( obj instanceof Double  ) || ( obj instanceof double[] ) ||
             ( obj instanceof String  ) || ( obj instanceof String[] );
   }

   /**
    * Maps the supplied Java object onto a Wica Type.
    *
    * Only a very limited number of types are supported, the attempt
    * to decode an unrecognised type will result in an exception.

    * @param obj the object (which should be recognised and not null)
    *
    * @return the WicaChannelType
    *
    * @throws NullPointerException if the supplied argument was null.
    * @throws IllegalArgumentException if the type of the object was not recognised.
    */
   public static WicaChannelType getTypeFromObject( Object obj )
   {
      Validate.notNull( obj );
      Validate.isTrue( isRecognisedType( obj ) );

      if ( obj instanceof Integer)
      {
         return INTEGER;
      }
      else if ( obj instanceof int[] )
      {
         return INTEGER_ARRAY;
      }
      else if ( obj instanceof Double)
      {
         return REAL;
      }
      else if ( obj instanceof double[] )
      {
         return REAL_ARRAY;
      }
      else if ( obj instanceof String)
      {
         return STRING;
      }
      else if ( obj instanceof String[] )
      {
         return STRING_ARRAY;
      }

      throw new IllegalArgumentException( "The supplied object was of an unrecognised type" );
   }


/*- Public methods -----------------------------------------------------------*/
/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
