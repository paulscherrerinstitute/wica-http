/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.infrastructure;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelMetadata;
import ch.psi.wica.model.WicaChannelName;
import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;



/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueMapSerializerTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger( WicaChannelValueMapSerializerTest.class );

   private WicaChannelValueMapSerializer wicaChannelValueSerializer;

/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setUp()
   {
      wicaChannelValueSerializer = new WicaChannelValueMapSerializer(Map.of(WicaChannelName.of("simon" ), Set.of("val", "sevr", "ts", "type" ),
                                                                            WicaChannelName.of( "simon2" ), Set.of( "val", "stat", "conn") ), 4 );
   }

   @Test
   void testValueSerialisation()
   {
      final WicaChannelValue disconnValue = WicaChannelValue.createChannelValueDisconnected();
      logger.info("JSON DISCONNECTED Value serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(disconnValue ) );

      final WicaChannelValue strValue = WicaChannelValue.createChannelValueConnected( "abc" );
      logger.info("JSON STRING Value serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(strValue ) );

      final WicaChannelValue intValue = WicaChannelValue.createChannelValueConnected ( 123 );
      logger.info("JSON INTEGER Value serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(intValue ) );

      final WicaChannelValue dblValue = WicaChannelValue.createChannelValueConnected( 123.79486914 );
      logger.info("JSON REAL Value ex1 serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(dblValue ) );

      final WicaChannelValue dblValue2 = WicaChannelValue.createChannelValueConnected( 123.010 );
      logger.info("JSON REAL Value ex2 serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(dblValue2 ) );

      final WicaChannelValue strArrayValue = WicaChannelValue.createChannelValueConnected( new String[] { "abc", "def" } );
      logger.info("JSON STRING Value serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(strArrayValue ) );

      final int[] intArr = { 123, 456 };
      final WicaChannelValue intArrayValue = WicaChannelValue.createChannelValueConnected( intArr );
      logger.info("JSON INTEGER Value serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(intArrayValue ) );

      final double[] dblArr = { 123.0, 456.0 };
      final WicaChannelValue dblArrayValue = WicaChannelValue.createChannelValueConnected( dblArr );
      logger.info("JSON REAL Value serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(dblArrayValue ) );
   }


   @Test
   void testChannelValueListSerialisation1()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( "abc" );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( "def" );

      final List<WicaChannelValue> list = List.of( strValue1, strValue2 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(list ));
   }

   @Test
   void testChannelValueListSerialisation2()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( 56.78 );

      final List<WicaChannelValue> list = List.of( strValue1, strValue2 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(list ));
   }

   @Test
   void testChannelValueListSerialisation3()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueConnected( 56 );

      final List<WicaChannelValue> list = List.of( strValue1, strValue2 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(list ));
   }

   @Test
   void testChannelValueListSerialisation4()
   {
      final WicaChannelValue strValue1 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue strValue2 = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue strValue3 = WicaChannelValue.createChannelValueConnected( 56 );
      final List<WicaChannelValue> list = List.of( strValue1, strValue2, strValue3 );
      logger.info("JSON List serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(list ));
   }


   @Test
   void testChannelMapSerialisation1()
   {
      final WicaChannelValue value1 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue value2 = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue value3 = WicaChannelValue.createChannelValueConnected( 56 );
      final List<WicaChannelValue> list1 = List.of( value1, value2, value3 );
      final WicaChannelValue value4 = WicaChannelValue.createChannelValueConnected( 12.34 );
      final WicaChannelValue value5 = WicaChannelValue.createChannelValueDisconnected();
      final WicaChannelValue value6 = WicaChannelValue.createChannelValueConnected( 56 );
      final List<WicaChannelValue> list2 = List.of( value4, value5, value6 );
      final Map<WicaChannelName,List<WicaChannelValue>> map = Map.of( WicaChannelName.of( "simon" ), list1,
                                                                      WicaChannelName.of( "simon2" ), list2 );
      logger.info("JSON Map serialisation looks like this: '{}'", wicaChannelValueSerializer.serialize(map ) );

   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
