package vdi.consts


object ExitStatus {

  enum class Import(val code: Int?) {
    Success(0),
    ValidationFailure(99),
    Unknown(null);

    companion object {
      fun fromCode(code: Int) = values().find { it.code == code } ?: Unknown
    }
  }

  enum class InstallData(val code: Int?) {
    Success(0),
    ValidationFailure(99),
    Unknown(null);

    companion object {
      fun fromCode(code: Int) = values().find { it.code == code } ?: Unknown
    }
  }

  enum class UninstallData(val code: Int?) {
    Success(0),
    Unknown(null);

    companion object {
      fun fromCode(code: Int) = values().find { it.code == code } ?: Unknown
    }
  }

  enum class InstallMeta(val code: Int?) {
    Success(0),
    Unknown(null);

    companion object {
      fun fromCode(code: Int) = values().find { it.code == code } ?: Unknown
    }
  }

  enum class CheckCompatibility(val code: Int?) {
    Success(0),
    Incompatible(1),
    Unknown(null);

    companion object {
      fun fromCode(code: Int) = values().find { it.code == code } ?: Unknown
    }
  }
}
