import org.junit.jupiter.api.*
import step.*
import testutil.*
import testutil.cmdRunner
import testutil.setupTest
import org.assertj.core.api.Assertions.*

class Bam2taTests {
    @BeforeEach fun setup() = setupTest()
    @AfterEach fun cleanup() = cleanupTest()

     @Test fun `run bam2ta step - se disable tn5 shift `() {

       cmdRunner.bam2ta(BAM,"chrM",true,"chrM",0,"output",false,1, testOutputDir)
       assertThat(testOutputDir.resolve("output.tagAlign.gz")).exists()
    }
    @Test fun `run bam2ta step - se control disable tn5 shift`() {

        cmdRunner.bam2ta(CONTROL,"chrM",true,"chrM",0,"output",false,1, testOutputDir)
        assertThat(testOutputDir.resolve("output.tagAlign.gz")).exists()
    }
    @Test fun `run bam2ta step - se disable tn5 shift subsample`() {

        cmdRunner.bam2ta(BAM,"chrM",true,"chrM",3,"output",false,1, testOutputDir)
        assertThat(testOutputDir.resolve("output.tagAlign.gz")).exists()
        assertThat(testOutputDir.resolve("output.3.tagAlign.gz")).exists()
    }

    @Test fun `run bam2ta step - se tn5 shift subsample`() {

        cmdRunner.bam2ta(BAM,"chrM",false,"chrM",2,"output",false,1, testOutputDir)
        assertThat(testOutputDir.resolve("output.tagAlign.gz")).exists()
        assertThat(testOutputDir.resolve("output.2.tagAlign.gz")).exists()
        assertThat(testOutputDir.resolve("output.tn5.tagAlign.gz")).exists()
    }
     @Test fun `run bam2ta step - pe disable tn5 shift`() {

        cmdRunner.bam2ta(PBAM,"chrM",true,"chrM",0,"output",true,1, testOutputDir)
         assertThat(testOutputDir.resolve("output.tagAlign.gz")).exists()
     }
     @Test fun `run bam2ta step - pe  tn5 shift`() {

        cmdRunner.bam2ta(PBAM,"chrM",false,"chrM",0,"output",true,1, testOutputDir)
         assertThat(testOutputDir.resolve("output.tagAlign.gz")).exists()
         assertThat(testOutputDir.resolve("output.tn5.tagAlign.gz")).exists()
     }
    @Test fun `run bam2ta step - pe  tn5 shift subsample`() {

        cmdRunner.bam2ta(PBAM,"chrM",false,"chrM",2,"output",true,1, testOutputDir)
        assertThat(testOutputDir.resolve("output.2.tagAlign.gz")).exists()
        assertThat(testOutputDir.resolve("output.tagAlign.gz")).exists()
        assertThat(testOutputDir.resolve("output.tn5.tagAlign.gz")).exists()
    }

}