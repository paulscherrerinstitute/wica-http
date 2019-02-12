/*- Package Declaration ------------------------------------------------------*/

package ch.psi.wica.config;

/*- Imported packages --------------------------------------------------------*/

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/*- Interface Declaration ----------------------------------------------------*/
/*- Class Declaration --------------------------------------------------------*/


@Configuration
class CpuMetrics
{

/*- Public attributes --------------------------------------------------------*/
/*- Private attributes -------------------------------------------------------*/

   private final Logger logger = LoggerFactory.getLogger(CpuMetrics.class );

   private final static String METRICS_NAME = "process.cpu.load";

   @Autowired
   private MeterRegistry meterRegistry;


/*- Main ---------------------------------------------------------------------*/
/*- Constructor --------------------------------------------------------------*/
/*- Class methods ------------------------------------------------------------*/
/*- Public methods -----------------------------------------------------------*/

   @PostConstruct
   public void init() {
      Gauge.builder(METRICS_NAME, this, CpuMetrics::getProcessCpuLoad)
            .baseUnit("%")
            .description("CPU Load")
            .register(meterRegistry);
   }

   public Double getProcessCpuLoad() {
      try {
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
         AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

         return Optional.ofNullable(list)
               .map(l -> l.isEmpty() ? null : l)
               .map(List::iterator)
               .map(Iterator::next)
               .map(Attribute.class::cast)
               .map(Attribute::getValue)
               .map(Double.class::cast)
               .orElse(null);

      } catch (Exception ex) {
         return null;
      }
   }

/*- Private methods ----------------------------------------------------------*/
/*- Nested Classes -----------------------------------------------------------*/

}

