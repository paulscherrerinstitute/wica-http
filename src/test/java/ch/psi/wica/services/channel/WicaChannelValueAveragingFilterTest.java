/*- Package Declaration ------------------------------------------------------*/
package ch.psi.wica.services.channel;

/*- Imported packages --------------------------------------------------------*/

import ch.psi.wica.model.channel.WicaChannelType;
import ch.psi.wica.model.channel.value.*;
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

   private static Stream<Arguments> getMixedTypeArgs()
   {
      filter = new WicaChannelValueAveragingFilter( 2 );

      return Stream.of( Arguments.of( List.of( WicaChannelValueBuilder.createChannelValueConnectedString( "abc" ),
                                               WicaChannelValueBuilder.createChannelValueConnectedInteger( 1 ),
                                               WicaChannelValueBuilder.createChannelValueConnectedReal( 2.5 ),
                                               WicaChannelValueBuilder.createChannelValueDisconnected(),
                                               WicaChannelValueBuilder.createChannelValueConnectedInteger( 99 ) ),
                                               List.of( WicaChannelValueBuilder.createChannelValueConnectedReal( 1.75 ),
                                               WicaChannelValueBuilder.createChannelValueDisconnected() ) ) );
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
            final int actualIntValue = ((WicaChannelValueConnectedInteger) actualValue).getValue();
            final int expectedIntValue = ((WicaChannelValueConnectedInteger) expectedValue).getValue();
            assertThat( actualIntValue, is( expectedIntValue ) );
         }

         if ( actualValue.getType() == WicaChannelType.REAL )
         {
            final double actualRealValue = ((WicaChannelValueConnectedReal) actualValue).getValue();
            final double expectedRealValue = ((WicaChannelValueConnectedReal) expectedValue).getValue();
            assertThat( actualRealValue, is( expectedRealValue ) );
         }
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}
