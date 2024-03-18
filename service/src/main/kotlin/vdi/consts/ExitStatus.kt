package vdi.consts


object ExitStatus {

  enum class Import(val code: Int?, val metricFriendlyName: String) {
    Success(0, "success"),
    ValidationFailure(99, "validation_failure"),
    Unknown(null, "unknown");

    companion object {
      fun fromCode(code: Int) = entries.find { it.code == code } ?: Unknown
    }
  }

  enum class InstallData(val code: Int?, val metricFriendlyName: String) {
    Success(0, "success"),
    ValidationFailure(99, "validation_failure"),
    Unknown(null ,"unknown");

    companion object {
      fun fromCode(code: Int) = entries.find { it.code == code } ?: Unknown
    }
  }

  enum class UninstallData(val code: Int?, val metricFriendlyName: String) {
    Success(0, "success"),
    Unknown(null, "unknown");

    companion object {
      fun fromCode(code: Int) = entries.find { it.code == code } ?: Unknown
    }
  }

  enum class InstallMeta(val code: Int?, val metricFriendlyName: String) {
    Success(0, "success"),
    Unknown(null, "unknown");

    companion object {
      fun fromCode(code: Int) = entries.find { it.code == code } ?: Unknown
    }
  }

  enum class CheckCompatibility(val code: Int?, val metricFriendlyName: String) {
    Success(0, "success"),
    Incompatible(1, "incompatible"),
    Unknown(null, "unknown");

    companion object {
      fun fromCode(code: Int) = entries.find { it.code == code } ?: Unknown
    }
  }
}
