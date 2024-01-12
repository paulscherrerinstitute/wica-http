/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.value.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueChangeDetectingFilterTest
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private static WicaChannelValueFilter filter;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   private static Stream<Arguments> getStringArgs()
   {
      filter = new WicaChannelValueChangeDetectingFilter(1 );

      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( "abc" ), List.of( "abc") ),
                        Arguments.of( List.of( "abcd", "efgh", "ijkl", "mnop" ), List.of( "abcd", "efgh", "ijkl", "mnop" ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getStringArgs"  )
   void testMapWithStringList( List<String> inputStringList, List<String> outputStringList )
   {
      final List<WicaChannelValue> inputList = inputStringList.stream().map( WicaChannelValueBuilder::createChannelValueConnectedString ).collect( Collectors.toList() );
      final List<WicaChannelValue> expectedOutputList = outputStringList.stream().map( WicaChannelValueBuilder::createChannelValueConnectedString ).collect( Collectors.toList() );
      final List<WicaChannelValue> actualOutputList  = filter.apply( inputList );
      assertThat( actualOutputList.size(), is( actualOutputList.size() ) );

      for ( int i = 0; i < expectedOutputList.size(); i++ )
      {
         final String expectedValue = ((WicaChannelValueConnectedString) expectedOutputList.get( i ) ).getValue();
         final String actualValue = ((WicaChannelValueConnectedString) actualOutputList.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }

   private static Stream<Arguments> getIntegerArgs()
   {
      filter = new WicaChannelValueChangeDetectingFilter(1 );

      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( 0 ), List.of( 0 ) ),
                        Arguments.of( List.of( 0, 0, 0, 0 ), List.of() ),
                        Arguments.of( List.of( 0, 2 ), List.of( 2 ) ),
                        Arguments.of( List.of( 2, 4), List.of( 4 ) ),
                        Arguments.of( List.of( 4, 5, 6, 7, 9 ), List.of( 9 ) ),
                        Arguments.of( List.of( 8, 7, 5 ), List.of( 5 ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getIntegerArgs"  )
   void testMapWithIntegerList( List<Integer> inputIntList, List<Integer> outputIntList )
   {
      WicaChannelValueBuilder.createChannelValueConnectedInteger( 0 );
      final List<WicaChannelValue> inputList = inputIntList.stream().map( WicaChannelValueBuilder::createChannelValueConnectedInteger ).collect( Collectors.toList() );
      final List<WicaChannelValue> expectedOutputList = outputIntList.stream().map(WicaChannelValueBuilder::createChannelValueConnectedInteger).collect(Collectors.toList());

      final List<WicaChannelValue> actualOutputList  = filter.apply(inputList );
      assertThat( actualOutputList.size(), is( expectedOutputList.size() ) );

      for ( int i = 0; i < expectedOutputList.size(); i++ )
      {
         final int expectedValue = ((WicaChannelValueConnectedInteger) expectedOutputList.get( i ) ).getValue();
         final int actualValue = ((WicaChannelValueConnectedInteger) actualOutputList.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }

   private static Stream<Arguments> getDoubleArgs()
   {
      filter = new WicaChannelValueChangeDetectingFilter(1 );

      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( 5.0 ), List.of( 5.0 ) ),
                        Arguments.of( List.of( 0.0, 0.5, 1.0, 1.5, 2.6 ), List.of( 0.0, 2.6 ) ),
                        Arguments.of( List.of( 0.1, 0.1, 0.1, 0.1 ), List.of( 0.1 ) ),
                        Arguments.of( List.of( 0.4, 0.9, 2.0 ), List.of( 2.0 ) ),
                        Arguments.of( List.of( 0.5, 1.6), List.of( 0.5, 1.6 ) ),
                        Arguments.of( List.of( 0.9, 2.0, 4.0, 4.0, 0.0 ), List.of( 2.0, 4.0, 0.0 ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getDoubleArgs"  )
   void testMapWithDoubleList( List<Double> inputDblList, List<Double> outputDblList )
   {
      final List<WicaChannelValue> inputList = inputDblList.stream().map(WicaChannelValueBuilder::createChannelValueConnectedReal).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList = outputDblList.stream().map(WicaChannelValueBuilder::createChannelValueConnectedReal).collect(Collectors.toList());

      final List<WicaChannelValue> actualOutputList  = filter.apply(inputList );
      assertThat( actualOutputList.size(), is( expectedOutputList.size() ) );

      for ( int i = 0; i < expectedOutputList.size(); i++ )
      {
         final double expectedValue = ((WicaChannelValueConnectedReal) expectedOutputList.get( i ) ).getValue();
         final double actualValue = ((WicaChannelValueConnectedReal) actualOutputList.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }

   private static Stream<Arguments> getMultipleMapIntegerArgs()
   {
      filter = new WicaChannelValueChangeDetectingFilter(1 );
      return Stream.of( Arguments.of( List.of( 0, 0, 0, 0 ), List.of( 0 ), List.of( 0, 3, 5 ), List.of( 3, 5 ) ),
                        Arguments.of( List.of( 0, 0, 0, 2 ), List.of( 0, 2 ), List.of( 0, 1, 4 ), List.of( 0, 4 ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getMultipleMapIntegerArgs"  )
   void testMultipleMapOperations( List<Integer> inputIntList1, List<Integer> outputIntList1, List<Integer> inputIntList2, List<Integer> outputIntList2 )
   {
      final List<WicaChannelValue> inputList1 = inputIntList1.stream().map(WicaChannelValueBuilder::createChannelValueConnectedInteger).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList1 = outputIntList1.stream().map(WicaChannelValueBuilder::createChannelValueConnectedInteger).collect(Collectors.toList());
      final List<WicaChannelValue> actualOutputList1  = filter.apply(inputList1 );

      assertThat( actualOutputList1.size(), is( expectedOutputList1.size() ) );
      for ( int i = 0; i < expectedOutputList1.size(); i++ )
      {
         final int expectedValue = ((WicaChannelValueConnectedInteger) expectedOutputList1.get( i ) ).getValue();
         final int actualValue = ((WicaChannelValueConnectedInteger) actualOutputList1.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
      
      final List<WicaChannelValue> inputList2 = inputIntList2.stream().map(WicaChannelValueBuilder::createChannelValueConnectedInteger).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList2 = outputIntList2.stream().map(WicaChannelValueBuilder::createChannelValueConnectedInteger).collect(Collectors.toList());
      final List<WicaChannelValue> actualOutputList2  = filter.apply(inputList2 );

      assertThat( actualOutputList2.size(), is( expectedOutputList2.size() ) );
      for ( int i = 0; i < expectedOutputList2.size(); i++ )
      {
         final int expectedValue = ((WicaChannelValueConnectedInteger) expectedOutputList2.get( i ) ).getValue();
         final int actualValue = ((WicaChannelValueConnectedInteger) actualOutputList2.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }

   private static Stream<Arguments> getMultipleMapMixedTypeArgs()
   {
      filter = new WicaChannelValueChangeDetectingFilter(1 );

      return Stream.of( Arguments.of( List.of( 0, 0, 0, 0 ), List.of( 0 ), List.of( "abc", "def", "ghi" ), List.of( "abc", "def", "ghi" ) ),
                        Arguments.of( List.of( 5, 5, 0, 2, 0 ), List.of( 5, 0, 2, 0 ), List.of( "jkl" ), List.of( "jkl" ) ));
   }

   @ParameterizedTest
   @MethodSource( "getMultipleMapMixedTypeArgs"  )
   void testMultipleMapMixedTypeOperations( List<Integer> inputIntList1, List<Integer> outputIntList1, List<String> inputStringList2, List<String> outputStringList2 )
   {
      final List<WicaChannelValue> inputList1 = inputIntList1.stream().map(WicaChannelValueBuilder::createChannelValueConnectedInteger).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList1 = outputIntList1.stream().map(WicaChannelValueBuilder::createChannelValueConnectedInteger).collect(Collectors.toList());
      final List<WicaChannelValue> actualOutputList1  = filter.apply(inputList1 );

      assertThat( actualOutputList1.size(), is( expectedOutputList1.size() ) );
      for ( int i = 0; i < expectedOutputList1.size(); i++ )
      {
         final int expectedValue = ((WicaChannelValueConnectedInteger) expectedOutputList1.get( i ) ).getValue();
         final int actualValue = ((WicaChannelValueConnectedInteger) actualOutputList1.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }

      final List<WicaChannelValue> inputList2 = inputStringList2.stream().map(WicaChannelValueBuilder::createChannelValueConnectedString).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList2 = outputStringList2.stream().map(WicaChannelValueBuilder::createChannelValueConnectedString).collect(Collectors.toList());
      final List<WicaChannelValue> actualOutputList2  = filter.apply(inputList2 );

      assertThat( actualOutputList2.size(), is( expectedOutputList2.size() ) );
      for ( int i = 0; i < expectedOutputList2.size(); i++ )
      {
         final String expectedValue = ((WicaChannelValueConnectedString) expectedOutputList2.get( i ) ).getValue();
         final String actualValue = ((WicaChannelValueConnectedString) actualOutputList2.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
