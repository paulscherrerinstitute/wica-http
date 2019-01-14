/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.stream;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.WicaChannelValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChangeFilteringChannelValueMapperTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private WicaChannelValueMapper mapper;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @BeforeEach
   void setUp()
   {
      this.mapper = new WicaChangeFilteringChannelValueMapper( 1 );
   }

   private static Stream<Arguments> getStringArgs()
   {
      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( "abc" ), List.of( "abc") ),
                        Arguments.of( List.of( "abcd", "efgh", "ijkl", "mnop" ), List.of( "mnop" ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getStringArgs"  )
   void testMapWithStringList( List<String> inputStringList, List<String> outputListList )
   {
      final List<WicaChannelValue> inputList = inputStringList.stream().map( v -> WicaChannelValue.createChannelValueConnected( v ) ).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList = outputListList.stream().map( v -> WicaChannelValue.createChannelValueConnected(v ) ).collect(Collectors.toList());

      final List<WicaChannelValue> actualOutputList  = mapper.map( inputList );
      assertEquals( expectedOutputList.size(), actualOutputList.size() );
      assertEquals( expectedOutputList, actualOutputList );
   }

   private static Stream<Arguments> getIntegerArgs()
   {
      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( 0 ), List.of( 0 ) ),
                        Arguments.of( List.of( 0, 0, 0, 0 ), List.of( 0 ) ),
                        Arguments.of( List.of( 0, 1 ), List.of( 0 ) ),
                        Arguments.of( List.of( 0, 2), List.of( 0, 2 ) ),
                        Arguments.of( List.of( 0, 2, 4, 4, 0 ), List.of( 0, 2, 4, 0 ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getIntegerArgs"  )
   void testMapWithIntegerList( List<Integer> inputIntList, List<Integer> outputIntList )
   {
      final List<WicaChannelValue> inputList = inputIntList.stream().map( v -> WicaChannelValue.createChannelValueConnected( v ) ).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList = outputIntList.stream().map( v -> WicaChannelValue.createChannelValueConnected(v ) ).collect(Collectors.toList());

      final List<WicaChannelValue> actualOutputList  = mapper.map( inputList );
      assertEquals( expectedOutputList.size(), actualOutputList.size() );
      assertEquals( expectedOutputList, actualOutputList );
   }

   private static Stream<Arguments> getDoubleArgs()
   {
      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( 0.0 ), List.of( 0.0 ) ),
                        Arguments.of( List.of( 0.1, 0.1, 0.1, 0.1 ), List.of( 0.1 ) ),
                        Arguments.of( List.of( 0.4, 0.9 ), List.of( 0.4 ) ),
                        Arguments.of( List.of( 0.5, 1.6), List.of( 0.5, 1.6 ) ),
                        Arguments.of( List.of( 0.0, 2.0, 4.0, 4.0, 0.0 ), List.of( 0.0, 2.0, 4.0, 0.0 ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getDoubleArgs"  )
   void testMapWithDoubleList( List<Double> inputDblList, List<Double> outputDblList )
   {
      final List<WicaChannelValue> inputList = inputDblList.stream().map( v -> WicaChannelValue.createChannelValueConnected(v ) ).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList = outputDblList.stream().map( v -> WicaChannelValue.createChannelValueConnected(v) ).collect(Collectors.toList());

      final WicaChannelValue initValue = WicaChannelValue.createChannelValueConnected(Double.NEGATIVE_INFINITY );
      //final WicaChannelValueMapper mapper = new WicaChangeFilteringChannelValueMapper( initValue, 1 );
      final List<WicaChannelValue> actualOutputList  = mapper.map( inputList );
      assertEquals( expectedOutputList.size(), actualOutputList.size() );
      assertEquals( expectedOutputList, actualOutputList );
   }

   private static Stream<Arguments> getMultipleMapIntegerArgs()
   {
      return Stream.of( Arguments.of( List.of( 0, 0, 0, 0 ), List.of( 0 ),    List.of( 0, 0, 0, 2 ), List.of( 2 ) ),
                        Arguments.of( List.of( 0, 0, 0, 2 ), List.of( 0, 2 ), List.of( 2 ),          List.of() ) );
   }

   @ParameterizedTest
   @MethodSource( "getMultipleMapIntegerArgs"  )
   void testMultipleMapOperations( List<Integer> inputIntList1, List<Integer> outputIntList1, List<Integer> inputIntList2, List<Integer> outputIntList2 )
   {
      final List<WicaChannelValue> inputList1 = inputIntList1.stream().map( v -> WicaChannelValue.createChannelValueConnected(v ) ).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList1 = outputIntList1.stream().map( v -> WicaChannelValue.createChannelValueConnected( v ) ).collect(Collectors.toList());
      final List<WicaChannelValue> outputList1  = mapper.map( inputList1 );
      assertEquals( expectedOutputList1.size(), outputList1.size() );
      assertEquals( expectedOutputList1, outputList1 );
      
      final List<WicaChannelValue> inputList2 = inputIntList2.stream().map( v -> WicaChannelValue.createChannelValueConnected(v ) ).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList2 = outputIntList2.stream().map( v -> WicaChannelValue.createChannelValueConnected( v ) ).collect(Collectors.toList());
      final List<WicaChannelValue> outputList2  = mapper.map( inputList2 );
      assertEquals( expectedOutputList2.size(), outputList2.size() );
      assertEquals( expectedOutputList2, outputList2 );
   }   

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
