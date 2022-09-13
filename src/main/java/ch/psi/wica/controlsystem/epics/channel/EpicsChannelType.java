/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.controlsystem.epics.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import org.apache.commons.lang3.Validate;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

/**
 * Represents the type of EPICS Channel. This can potentially change every
 * time new channel metadata is received.
 * <p>
 * {@code @implNote.}
 * The support here is based on the use of Wica with the PSI CA channel
 * access client library.
 */
public enum EpicsChannelType
{

/*- Public attributes --------------------------------------------------------*/

   /**
    * Maps the EPICS STRING type to/from the Wica Channel Type STRING.
    */
   STRING ( WicaChannelType.STRING ),

   /**
    * Maps the EPICS BYTE type to/from the Wica Channel Type INTEGER.
    */
   BYTE ( WicaChannelType.INTEGER ),

   /**
    * Maps the EPICS SHORT type to/from the Wica Channel Type INTEGER.
    */
   SHORT ( WicaChannelType.INTEGER ),

   /**
    * Maps the EPICS INTEGER type to/from the Wica Channel Type INTEGER.
    */
   INTEGER ( WicaChannelType.INTEGER ),

   /**
    * Maps the EPICS FLOAT type to/from the Wica Channel Type REAL.
    */
   FLOAT ( WicaChannelType.REAL ),

   /**
    * Maps the EPICS DOUBLE type to/from the Wica Channel Type REAL.
    */
   DOUBLE ( WicaChannelType.REAL ),

   /**
    * Maps the EPICS STRING ARRAY type to/from the Wica Channel Type STRING_ARRAY.
    */
   STRING_ARRAY ( WicaChannelType.STRING_ARRAY),

   /**
    * Maps the EPICS BYTE ARRAY type to/from the Wica Channel Type INTEGER_ARRAY.
    */
   BYTE_ARRAY ( WicaChannelType.INTEGER_ARRAY ),

   /**
    * Maps the EPICS SHORT ARRAY type to/from the Wica Channel Type INTEGER_ARRAY.
    */
   SHORT_ARRAY ( WicaChannelType.INTEGER_ARRAY ),

   /**
    * Maps the EPICS INTEGER ARRAY type to/from the Wica Channel Type INTEGER_ARRAY.
    */
   INTEGER_ARRAY ( WicaChannelType.INTEGER_ARRAY ),

   /**
    * Maps the EPICS FLOAT ARRAY type to/from the Wica Channel Type REAL_ARRAY.
    */
   FLOAT_ARRAY ( WicaChannelType.REAL_ARRAY),

   /**
    * Maps the EPICS DOUBLE ARRAY type to/from the Wica Channel Type REAL_ARRAY.
    */
   DOUBLE_ARRAY  ( WicaChannelType.REAL_ARRAY);

/*- Private attributes -------------------------------------------------------*/

   private final WicaChannelType wicaChannelType;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/

   /**
    * Creates a new instance.
    *
    * @param wicaChannelType the wica channel type.
    */
   EpicsChannelType( WicaChannelType wicaChannelType )
   {
      this.wicaChannelType = wicaChannelType;
   }


/*- Class methods ------------------------------------------------------------*/

   /**
    * Checks whether the supplied object is of a supported EpicsChannelType.
    *
    * @param obj the object to check.
    * @return the result, set true when the object is recognised.
    */
   public static boolean isRecognisedType( Object obj )
   {
      Validate.notNull( obj );

      return ( obj instanceof String  ) || ( obj instanceof String[] ) ||
             ( obj instanceof Byte    ) || ( obj instanceof byte[]   ) ||
             ( obj instanceof Short   ) || ( obj instanceof short[]  ) ||
             ( obj instanceof Integer ) || ( obj instanceof int[]    ) ||
             ( obj instanceof Float   ) || ( obj instanceof float[]  ) ||
             ( obj instanceof Double  ) || ( obj instanceof double[] );
   }

   /**
    * Attempts to map the supplied Plain-Old-Java-Object (POJO) onto a
    * Epics Channel Type.
    * <p>
    * Attempts to decode an unrecognised type will result in an exception.
    * <p>
    * @param pojo the object (which should be recognised and not null)
    *
    * @return the Epics Channel Type.
    *
    * @throws NullPointerException if the supplied argument was null.
    * @throws IllegalArgumentException if the type of the object was not recognised.
    */
   public static EpicsChannelType getTypeFromPojo( Object pojo )
   {
      Validate.notNull( pojo );
      Validate.isTrue( isRecognisedType( pojo ) );

      if ( pojo instanceof Byte )
      {
         return BYTE;
      }
      else if ( pojo instanceof byte[] )
      {
         return BYTE_ARRAY;
      }
      else if ( pojo instanceof Short )
      {
         return SHORT;
      }
      else if ( pojo instanceof short[] )
      {
         return SHORT_ARRAY;
      }
      else if ( pojo instanceof Integer )
      {
         return INTEGER;
      }
      else if ( pojo instanceof int[] )
      {
         return INTEGER_ARRAY;
      }
      else if ( pojo instanceof Float )
      {
         return FLOAT;
      }
      else if ( pojo instanceof float[] )
      {
         return FLOAT_ARRAY;
      }
      if ( pojo instanceof Double )
      {
         return DOUBLE;
      }
      else if ( pojo instanceof double[] )
      {
         return DOUBLE_ARRAY;
      }
      else if ( pojo instanceof String )
      {
         return STRING;
      }
      else if ( pojo instanceof String[] )
      {
         return STRING_ARRAY;
      }

      throw new IllegalArgumentException( "The supplied object was of an unrecognised type: ('" + pojo + "')");
   }


/*- Public methods -----------------------------------------------------------*/

   @SuppressWarnings("unused")
   public WicaChannelType getWicaChannelType()
   {
      return this.wicaChannelType;
   }

   @Override
   public String toString()
   {
      return this.name();
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
