import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import step.*
import util.*
import java.nio.file.*
import util.CmdRunner


fun main(args: Array<String>) = Cli().main(args)

class Cli : CliktCommand() {
    private val bamFile: Path by option("-bam", help = "path for raw BAM file.")
        .path().required()
    private val disable_tn5_shift: Boolean by option("--disable-tn5-shift", help = "Disable TN5 shifting for DNase-Seq.").flag()
    private val mito_chr_name: String by option("--mito-chr-name", help = "Mito chromosome name.").default("chrM")
    private val regex_grep_v_ta: String by option("--regex-grep-v-ta", help = "Perl-style regular expression pattern \\\n" +
            "    to remove matching reads from TAGALIGN.").default("chrM")
    private val subSample: Int by option("-subsample", help = "Subsample TAGALIGN. \\\n" +
            "    This affects all downstream analysis.").int().default(0)
    private val pairedEnd: Boolean by option("-pairedEnd", help = "Paired-end BAM.").flag()
    private val parallelism: Int by option("-parallelism", help = "Number of threads to parallelize.").int().default(1)
    private val outputPrefix: String by option("-outputPrefix", help = "output file name prefix; defaults to 'output'").default("output")
    private val outDir by option("-outputDir", help = "path to output Directory")
        .path().required()

    override fun run() {
        val cmdRunner = DefaultCmdRunner()

        cmdRunner.runTask(bamFile,regex_grep_v_ta,disable_tn5_shift,mito_chr_name,subSample,outputPrefix,pairedEnd,parallelism, outDir)
    }
}

/**
 * Runs pre-processing and bwa for raw input files
 *
 * @param bwaInputs bwa Input
 * @param outDir Output Path
 */
fun CmdRunner.runTask(bamFile:Path,regex_grep_v_ta:String,disable_tn5_shift:Boolean,mito_chr_name:String,subSample:Int,outputPrefix:String,pairedEnd:Boolean,parallelism:Int, outDir:Path) {

    bam2ta(bamFile,regex_grep_v_ta,disable_tn5_shift,mito_chr_name,subSample,outputPrefix,pairedEnd,parallelism, outDir)

}