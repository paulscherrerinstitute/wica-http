/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import ch.psi.wica.model.channel.WicaChannelValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/

@SpringBootTest
class WicaChannelValueAveragingFilterTest
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
      filter = new WicaChannelValueAveragingFilter( 2 );

      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( "abc" ), List.of() ),
                        Arguments.of( List.of( "abcd", "efgh", "ijkl", "mnop" ), List.of() ) );
   }

   @ParameterizedTest
   @MethodSource( "getStringArgs"  )
   void testMapWithStringList( List<String> inputStringList, List<String> outputStringList )
   {
      final List<WicaChannelValue> inputList = inputStringList.stream().map( WicaChannelValue::createChannelValueConnectedString ).collect( Collectors.toList() );
      final List<WicaChannelValue> expectedOutputList = outputStringList.stream().map( WicaChannelValue::createChannelValueConnectedString ).collect( Collectors.toList() );
      final List<WicaChannelValue> actualOutputList  = filter.apply( inputList );
      assertThat( actualOutputList.size(), is( actualOutputList.size() ) );

      for ( int i = 0; i < expectedOutputList.size(); i++ )
      {
         final String expectedValue = ((WicaChannelValue.WicaChannelValueConnectedString) expectedOutputList.get( i ) ).getValue();
         final String actualValue = ((WicaChannelValue.WicaChannelValueConnectedString) actualOutputList.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }

   private static Stream<Arguments> getIntegerArgs()
   {
      filter = new WicaChannelValueAveragingFilter( 2 );

      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( 5 ), List.of() ),
                        Arguments.of( List.of( -1, 1 ), List.of( 2 ) ),
                        Arguments.of( List.of( 1, 2, 3, 4, 5 ), List.of( 1, 3, 5 ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getIntegerArgs"  )
   void testMapWithIntegerList( List<Integer> inputIntList, List<Integer> outputIntList )
   {
      WicaChannelValue.createChannelValueConnectedInteger( 0 );
      final List<WicaChannelValue> inputList = inputIntList.stream().map( WicaChannelValue::createChannelValueConnectedInteger ).collect( Collectors.toList() );
      final List<WicaChannelValue> expectedOutputList = outputIntList.stream().map(WicaChannelValue::createChannelValueConnectedInteger).collect(Collectors.toList());

      final List<WicaChannelValue> actualOutputList  = filter.apply(inputList );
      assertThat( actualOutputList.size(), is( expectedOutputList.size() ) );

      for ( int i = 0; i < expectedOutputList.size(); i++ )
      {
         final int expectedValue = ((WicaChannelValue.WicaChannelValueConnectedInteger) expectedOutputList.get( i ) ).getValue();
         final int actualValue = ((WicaChannelValue.WicaChannelValueConnectedInteger) actualOutputList.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }

   private static Stream<Arguments> getDoubleArgs()
   {
      filter = new WicaChannelValueAveragingFilter( 2 );

      return Stream.of( Arguments.of( List.of(), List.of() ),
                        Arguments.of( List.of( 5.0 ), List.of() ),
                        Arguments.of( List.of( -1.0, 1.0 ), List.of( 2.0 ) ),
                        Arguments.of( List.of( 1.0, 2.0, 3.0, 4.0, 5.0 ), List.of( 1.0, 2.5, 4.5 ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getDoubleArgs"  )
   void testMapWithDoubleList( List<Double> inputDblList, List<Double> outputDblList )
   {
      final List<WicaChannelValue> inputList = inputDblList.stream().map(WicaChannelValue::createChannelValueConnectedReal).collect(Collectors.toList());
      final List<WicaChannelValue> expectedOutputList = outputDblList.stream().map(WicaChannelValue::createChannelValueConnectedReal).collect(Collectors.toList());

      final List<WicaChannelValue> actualOutputList  = filter.apply(inputList );
      assertThat( actualOutputList.size(), is( expectedOutputList.size() ) );

      for ( int i = 0; i < expectedOutputList.size(); i++ )
      {
         final double expectedValue = ((WicaChannelValue.WicaChannelValueConnectedReal) expectedOutputList.get( i ) ).getValue();
         final double actualValue = ((WicaChannelValue.WicaChannelValueConnectedReal) actualOutputList.get( i )).getValue();
         assertThat( actualValue, is( expectedValue ) );
      }
   }

   private static Stream<Arguments> getMixedTypeArgs()
   {
      filter = new WicaChannelValueAveragingFilter( 2 );

      return Stream.of( Arguments.of( List.of( WicaChannelValue.createChannelValueConnectedString( "abc" ),
                                               WicaChannelValue.createChannelValueConnectedInteger( 1 ),
                                               WicaChannelValue.createChannelValueConnectedReal( 2.5 ),
                                               WicaChannelValue.createChannelValueDisconnected(),
                                               WicaChannelValue.createChannelValueConnectedInteger( 99 ) ),
                                      List.of( WicaChannelValue.createChannelValueConnectedReal( 1.75 ),
                                               WicaChannelValue.createChannelValueDisconnected() ) ) );
   }

   @ParameterizedTest
   @MethodSource( "getMixedTypeArgs"  )
   void testMixedTypeOperations( List<WicaChannelValue> inputArgList, List<WicaChannelValue> outputArgList )
   {
      final List<WicaChannelValue> actualOutputList = filter.apply( inputArgList );

      assertThat( actualOutputList.size(), is( outputArgList.size() ) );
      for ( int i = 0; i < outputArgList.size(); i++ )
      {
         final WicaChannelValue expectedValue = outputArgList.get( i );
         final WicaChannelValue actualValue = actualOutputList.get( i );
         assertThat( actualValue.isConnected(), is( expectedValue.isConnected() ) );
         assertThat( actualValue.getType(), is( expectedValue.getType() ) );

         if ( actualValue.getType() == WicaChannelType.INTEGER )
         {
            final int actualIntValue = ((WicaChannelValue.WicaChannelValueConnectedInteger) actualValue).getValue();
            final int expectedIntValue = ((WicaChannelValue.WicaChannelValueConnectedInteger) expectedValue).getValue();
            assertThat( actualIntValue, is( expectedIntValue ) );
         }

         if ( actualValue.getType() == WicaChannelType.REAL )
         {
            final double actualRealValue = ((WicaChannelValue.WicaChannelValueConnectedReal) actualValue).getValue();
            final double expectedRealValue = ((WicaChannelValue.WicaChannelValueConnectedReal) expectedValue).getValue();
            assertThat( actualRealValue, is( expectedRealValue ) );
         }
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
