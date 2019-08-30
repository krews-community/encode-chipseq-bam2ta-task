package step
import mu.KotlinLogging
import util.*
import java.nio.file.*
import util.CmdRunner
import java.lang.Exception
import kotlin.math.max

private val log = KotlinLogging.logger {}

fun CmdRunner.bam2ta(bamFile:Path,regex_grep_v_ta:String,disable_tn5_shift:Boolean,mito_chr_name:String,subSample:Int,outputPrefix:String,XcorTa:Boolean,pairedEnd:Boolean,parallelism:Int, outDir:Path) {
    log.info { "Make output Directory" }
    Files.createDirectories(outDir)
    var ta:String
    var subsampled_ta:String
    var tmpFiles = mutableListOf<String>() //Delete temp files at the end
    if(XcorTa)
    {
        ta = bam2ta_se_ta(bamFile, regex_grep_v_ta,
                outDir,outputPrefix)
    } else {
        if(pairedEnd)
        {
            ta = bam2ta_pe(bamFile, regex_grep_v_ta,
                    parallelism, outDir,outputPrefix)
        }else {
            ta = bam2ta_se(bamFile, regex_grep_v_ta,
                    outDir,outputPrefix)
        }
    }

    if(subSample > 0){

        log.info { "subsampling tagalign" }
        if(pairedEnd)
        {
             subsampled_ta = subsample_ta_pe(
                    ta, subSample,mito_chr_name, false, false, outDir,outputPrefix)
        }else {
             subsampled_ta = subsample_ta_se(
                    ta, subSample,mito_chr_name, false, outDir,outputPrefix)
        }
        tmpFiles.add(ta)
    }else {
         subsampled_ta = ta
    }

    if(disable_tn5_shift){
        val shifted_ta = subsampled_ta
    } else {
       //"TN5-shifting TAGALIGN..."
       val  shifted_ta = tn5_shift_ta(subsampled_ta, outDir,outputPrefix)
        tmpFiles.add(subsampled_ta)
    }

}

fun CmdRunner.bam2ta_pe(bamFile:Path,regex_grep_v_ta:String,parallelism:Int, outDir:Path,outputPrefix:String):String {
    val prefix = outDir.resolve(outputPrefix)

    val ta = "${prefix}.tagAlign.gz"

    //intermediate files
    val bedpe = "${prefix}.bedpe.gz"

    val nmsrt_bam = sambamba_name_sort(bamFile, parallelism, outDir)

    var cmd1 = "LC_COLLATE=C bedtools bamtobed -bedpe -mate1 -i ${nmsrt_bam} | "
    cmd1 += "gzip -nc > ${bedpe}"
    this.run(cmd1)

    rm_f(listOf(nmsrt_bam))

    var cmd2 = "zcat -f ${bedpe} | "
    cmd2 += "awk \'BEGIN{{OFS='\\t'}}"
    cmd2 += "{{ chrom=$1; beg=$2; end=$6;"
    cmd2 += "if($2>$5){{beg=$5}} if($3>$6){{end=$3}}"

    cmd2 += " printf \"%s\\t%s\\t%s\\n\","
    cmd2 += "chrom,beg,end "
    cmd2 +="}}\' - |  sort -k1,1 -k2,2n |"
 /*   cmd2 += "awk \'BEGIN{{OFS='\\t'}}"
    cmd2 += "{{printf \"%s\\t%s\\t%s\\tN\\t1000\\t%s\\t%s\\t%s\\t%s\\tN\\t1000\\t%s\\n\","
    cmd2 += "$1,$2,$3,$9,$4,$5,$6,$10}}\' | "*/
    if(regex_grep_v_ta!==null)
    {
        cmd2+= "grep -P -v \'^${regex_grep_v_ta}\\b\' | "
    }
    cmd2 += "gzip -nc > ${ta}"
    this.run(cmd2)
   rm_f(listOf(bedpe))
    return ta
}
fun CmdRunner.bam2ta_se_ta(bamFile:Path,regex_grep_v_ta:String, outDir:Path,outputPrefix:String):String {

    val prefix = outDir.resolve(outputPrefix)
    val ta = "${prefix}.tagAlign.gz"
    var cmd = "bedtools bamtobed -i ${bamFile} | "
    /*  cmd += " cut -f1-3 | "
      cmd +="sort -k1,1 -k2,2n | "*/
    cmd += "awk \'BEGIN{{OFS='\\t'}}"
    cmd += "{{printf \"%s\\t%s\\t%s\\tN\\t1000\\t%s\\n\","
    cmd += "$1,$2,$3,$6}}\' | "
    if (regex_grep_v_ta !== null) {
        cmd += "grep -P -v \'^${regex_grep_v_ta}\\b\' | "
    }
    cmd += "gzip -nc > ${ta}"
    this.run(cmd)
    return ta
}
fun CmdRunner.bam2ta_se(bamFile:Path,regex_grep_v_ta:String, outDir:Path,outputPrefix:String):String {

    val prefix = outDir.resolve(outputPrefix)
    val ta = "${prefix}.tagAlign.gz"
    var cmd = "bedtools bamtobed -i ${bamFile} | "
   cmd += " cut -f1-3 | "
    cmd +="sort -k1,1 -k2,2n | "
 /*  cmd += "awk \'BEGIN{{OFS='\\t'}}"
    cmd += "{{printf \"%s\\t%s\\t%s\\tN\\t1000\\t%s\\n\","
    cmd += "$1,$2,$3,$6}}\' | "*/
    if (regex_grep_v_ta !== null) {
        cmd += "grep -P -v \'^${regex_grep_v_ta}\\b\' | "
    }
    cmd += "gzip -nc > ${ta}"
    this.run(cmd)
    return ta
}
fun CmdRunner.sambamba_name_sort(bam:Path,nth:Int,output:Path):String{

    val prefix= output.resolve(strip_ext_bam(bam.fileName.toString()))
    val nmsrt_bam = "${prefix}.nmsrt.bam"
    val cmd = "sambamba sort -n ${bam} -o ${nmsrt_bam} -t ${nth}"
    this.run(cmd)
    return nmsrt_bam

}
fun CmdRunner.rm_f(tmpFiles: List<String>)
{
    val cmd ="rm -f ${tmpFiles.joinToString(" ")}"
    this.run(cmd)
}
fun CmdRunner.tn5_shift_ta(ta:String,outDir: Path,outputPrefix: String):String{
    val prefix = outDir.resolve(outputPrefix)
    val shifted_ta = "${prefix}.tn5.tagAlign.gz"
    var cmd = "zcat -f ${ta} | "
    cmd += "awk \'BEGIN {{OFS = '\\t'}} {{ if ($6 == \"+\") {{$2 = $2 + 4}} else if ($6 == \"-\") {{$3 = $3 - 5}} print $0}}\' | "
    cmd += "gzip -nc > ${shifted_ta}"
    this.run(cmd)
    return shifted_ta
}

fun CmdRunner.subsample_ta_pe(ta:String, subsample:Int,mito_chr_name:String, non_mito:Boolean,r1_only:Boolean, outDir:Path,outputPrefix:String):String {
    val prefix = outDir.resolve(outputPrefix)
    var nm:String
    var s:String
    var r:String
    if(r1_only)
    {
        r="R1."
    } else {
        r=""

    }
    if(non_mito)
    {
        nm="no_chrM."
    } else {
        nm=""

    }
    if(subsample>0)
    {
        s = human_readable_number(subsample)+"."
    } else {
        s=""
    }
    val ta_subsampled = "${prefix}.${nm}${r}${s}tagAlign.gz"
    val ta_tmp = "${prefix}.tagAlign.tmp"
    var cmd = "bash -c \"zcat -f ${ta} |"
    if(non_mito){
        //# cmd += 'awk \'{{if ($1!="'+mito_chr_name+'") print $0}}\' | '
      //  cmd +="grep -v \'^\'${mito_chr_name}\'\' | "
        cmd += "grep -v \'^${mito_chr_name}\\b\' | "
    }
    cmd += "sed \'N;s/\\n/\\t/\'"
    if(subsample>0){
        cmd += " |  shuf -n ${subsample} --random-source=<(openssl enc -aes-256-ctr -pass pass:$(zcat -f ${ta} | wc -c) -nosalt </dev/zero 2>/dev/null) > ${ta_tmp}\" "
       // cmd += " > ${ta_tmp}\""
    }
    else {
        cmd += " > ${ta_tmp}}\""
    }
    this.run(cmd)
    var cmd1 = "cat ${ta_tmp} | "
    cmd1 += "awk \'BEGIN{{OFS'\\t'}} "
    if(r1_only){
        cmd1 += "{{printf \"%s\\t%s\\t%s\\t%s\\t%s\\t%s\\n\","
        cmd1 += "$1,$2,$3,$4,$5,$6}}\' | "
    }

    else {
        cmd1 += "{{printf \"%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\n\","
        cmd1 += "$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12}}\' | "
    }
    cmd1 += "gzip -nc > ${ta_subsampled}"

    this.run(cmd1)
    rm_f(listOf(ta_tmp))
    return ta_subsampled
}
fun CmdRunner.subsample_ta_se(ta:String, subsample:Int,mito_chr_name:String, non_mito:Boolean, outDir:Path,outputPrefix:String):String{
    val prefix = outDir.resolve(outputPrefix)
    var nm:String
    var s:String
    if(non_mito)
    {
        nm="no_chrM."
    } else {
        nm=""

    }
    if(subsample>0)
    {
        s = human_readable_number(subsample)+"."
    } else {
        s=""
    }

    val ta_subsampled = "${prefix}.${nm}${s}tagAlign.gz"
    var cmd = "bash -c \"zcat -f ${ta} |"
    if(non_mito){

        cmd += "grep -v \'^${mito_chr_name}\\b\' | "
    }

    if(subsample>0){
        cmd += "shuf -n ${subsample} --random-source=<(openssl enc -aes-256-ctr -pass pass:$(zcat -f ${ta} | wc -c) -nosalt </dev/zero 2>/dev/null) | "
        cmd += "gzip -nc > ${ta_subsampled}\""
    }
    else {
        cmd += "gzip -nc > ${ta_subsampled}\""
    }

    this.run(cmd)
    return ta_subsampled
}

