package util
import com.google.common.base.CaseFormat


enum class DupMarker {picard,sambamba }

/**
 * Extension function to get enum name in lower-hyphen-format
 */
val Enum<*>.lowerHyphenName: String get() = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, this.name)