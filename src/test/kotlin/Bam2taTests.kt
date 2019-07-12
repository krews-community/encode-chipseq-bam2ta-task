import org.junit.jupiter.api.*
import step.*
import testutil.*
import testutil.cmdRunner
import testutil.setupTest
import util.*
import org.assertj.core.api.Assertions

class Bam2taTests {
    @BeforeEach fun setup() = setupTest()
    @AfterEach fun cleanup() = cleanupTest()

     @Test fun `run bam2ta step - se disable tn5 shift `() {

       cmdRunner.bam2ta(BAM,"chrM",true,"chrM",0,"output",false,1, testOutputDir)
       Assertions.assertThat(testOutputDir.resolve("rep1_align_output.nodup.tagAlign.gz"))
    }
    @Test fun `run bam2ta step - se control disable tn5 shift`() {

        cmdRunner.bam2ta(CONTROL,"chrM",true,"chrM",0,"output",false,1, testOutputDir)
        Assertions.assertThat(testOutputDir.resolve("control1_align_output.nodup.tagAlign.gz"))
    }
    @Test fun `run bam2ta step - se disable tn5 shift subsample`() {

        cmdRunner.bam2ta(BAM,"chrM",true,"chrM",1,"output",false,1, testOutputDir)
        Assertions.assertThat(testOutputDir.resolve("rep1_align_output.nodup.tagAlign.gz"))
        Assertions.assertThat(testOutputDir.resolve("rep1_align_output.nodup.1tagAlign.gz"))
    }

    @Test fun `run bam2ta step - se tn5 shift subsample`() {

        cmdRunner.bam2ta(BAM,"chrM",false,"chrM",1,"output",false,1, testOutputDir)
        Assertions.assertThat(testOutputDir.resolve("rep1_align_output.nodup.tagAlign.gz"))
        Assertions.assertThat(testOutputDir.resolve("rep1_align_output.nodup.1tagAlign.gz"))
        Assertions.assertThat(testOutputDir.resolve("rep1_align_output.nodup.tn5.tagAlign.gz"))
    }
     @Test fun `run bam2ta step - pe disable tn5 shift`() {

        cmdRunner.bam2ta(PBAM,"chrM",true,"chrM",0,"output",true,1, testOutputDir)
         Assertions.assertThat(testOutputDir.resolve("rep1_R1_R2_align_output.nodup.tagAlign.gz"))
     }
     @Test fun `run bam2ta step - pe  tn5 shift`() {

        cmdRunner.bam2ta(PBAM,"chrM",false,"chrM",2,"output",true,1, testOutputDir)
         Assertions.assertThat(testOutputDir.resolve("rep1_R1_R2_align_output.nodup.2tagAlign.gz"))
         Assertions.assertThat(testOutputDir.resolve("rep1_R1_R2_align_output.nodup.tagAlign.gz"))
         Assertions.assertThat(testOutputDir.resolve("rep1_R1_R2_align_output.nodup.tn5.tagAlign.gz"))
     }

}