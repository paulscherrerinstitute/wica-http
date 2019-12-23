/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.model.app;

/*- Imported packages --------------------------------------------------------*/
/*- Interface Declaration ----------------------------------------------------*/

import ch.psi.wica.controllers.ControllerStatisticsCollector;

public interface StatisticsCollectable
{

/*- Class Declaration --------------------------------------------------------*/


/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/
/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   ControllerStatisticsCollector getStatistics();
   void resetStatistics();


/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}