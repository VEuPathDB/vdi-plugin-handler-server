package vdi

object Const {

  object ExitCode {
    const val ImportScriptSuccess               = 0
    const val ImportScriptValidationFailure     = 1
    const val ImportScriptTransformationFailure = 2
  }

  object ProcessOpt {

    /**
     * Maximum number of bytes for the "details" field in the import processing
     * multipart post body.
     */
    const val MaxDetailsFieldBytes = 16384uL

    /**
     * What we name the payload posted to the handler server with the import
     * processing multipart post body.
     */
    const val ImportPayloadName = "import-payload.tar.gz"
  }

  object ConfigDefault {
    const val ServerPort = "80"
    const val ServerHost = "0.0.0.0"

    const val ImportScriptPath      = "/opt/veupathdb/import"
    const val DataInstallScriptPath = "/opt/veupathdb/install-data"
    const val MetaInstallScriptPath = "/opt/veupathdb/install-meta"
    const val UninstallScriptPath   = "/opt/veupathdb/uninstall"

    const val ImportScriptMaxDuration      = "1h"
    const val DataInstallScriptMaxDuration = "1h"
    const val MetaInstallScriptMaxDuration = "1h"
    const val UninstallScriptMaxDuration   = "1h"
  }

  object EnvKey {
    const val ImportScriptPath = "IMPORT_SCRIPT_PATH"
    const val ImportScriptMaxDuration = "IMPORT_SCRIPT_MAX_DURATION"

    const val DataInstallScriptPath = "INSTALL_DATA_SCRIPT_PATH"
    const val DataInstallScriptMaxDuration = "INSTALL_DATA_SCRIPT_MAX_DURATION"

    const val MetaInstallScriptPath = "INSTALL_META_SCRIPT_PATH"
    const val MetaInstallScriptMaxDuration = "INSTALL_META_SCRIPT_MAX_DURATION"

    const val UninstallScriptPath = "UNINSTALL_SCRIPT_PATH"
    const val UninstallScriptMaxDuration = "UNINSTALL_SCRIPT_MAX_DURATION"

    const val ServerPort = "SERVER_PORT"
    const val ServerHost = "SERVER_HOST"
  }

  object FieldName {
    const val DataFiles = "dataFiles"
    const val Dependencies = "dependencies"
    const val Description = "description"
    const val Details = "details"
    const val InputFiles = "inputFiles"
    const val Message = "message"
    const val Messages = "messages"
    const val Name = "name"
    const val Owner = "owner"
    const val Payload = "payload"
    const val Projects = "projects"
    const val ResourceIdentifier = "resourceIdentifier"
    const val ResourceVersion = "resourceVersion"
    const val ResourceDisplayName = "resourceDisplayName"
    const val Summary = "summary"
    const val Type = "type"
    const val VDIID = "vdiID"
    const val Version = "version"
  }
}
